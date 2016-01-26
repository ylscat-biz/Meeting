package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import java.io.File;

import arbell.demo.meeting.doc.DownloadTask;
import arbell.demo.meeting.doc.DownloadTask2;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

public class Login extends Activity implements View.OnClickListener {
    public static String sMemberID;
    public static String sMemberName;
    public static String sSign;

    public static final String SP_NAME = "meeting";
    private static final String NAME = "name";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(savedInstanceState != null) {
            sMemberID = savedInstanceState.getString("memberId");
            sMemberName = savedInstanceState.getString("sMemberName");
            sSign = savedInstanceState.getString("sSign");
        }

        findViewById(R.id.ok).setOnClickListener(this);
        EditText editText = (EditText)findViewById(R.id.password);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(findViewById(R.id.ok));
                return true;
            }
        });

        findViewById(R.id.settings).setOnClickListener(this);

        SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String name = sp.getString("name", null);
        if(name != null) {
            EditText et = (EditText)findViewById(R.id.name);
            et.setText(name);
        }

        Request request = new Request(Request.Method.GET,
                HttpHelper.URL_BASE + "getVersion",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.optBoolean("success")) {
                            Toast.makeText(Login.this,
                                    response.optString("msg"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject data = response.optJSONObject("data");
                        if(data != null) {
                            checkVersion(data);
                        }
                    }
                });
        HttpHelper.sRequestQueue.add(request);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("memberId", sMemberID);
        outState.putString("sMemberName", sMemberName);
        outState.putString("sSign", sSign);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                login();
                break;
            case R.id.settings:
                showSettingsDialog();
                break;
        }
    }

    private void login() {
        EditText et = (EditText)findViewById(R.id.name);
        String name = et.getText().toString();
        if(name.length() == 0) {
            Toast.makeText(this, R.string.name_is_required, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        et = (EditText)findViewById(R.id.password);
        String pwd = et.getText().toString();
        if(pwd.length() == 0) {
            Toast.makeText(this, R.string.name_is_required, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String param = String.format("username=%s&password=%s", name, pwd);
        Request request = new Request(Request.Method.POST,
                HttpHelper.URL_BASE + "login",
                param,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.optBoolean("success")) {
                            Toast.makeText(Login.this,
                                    response.optString("msg"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject data = response.optJSONObject("data");
                        if(data != null) {
                            sMemberID = data.optString("id");
                            String name = sMemberName = data.optString("name");
                            getSharedPreferences(SP_NAME, MODE_PRIVATE).edit()
                                    .putString(NAME, name).apply();
                            if(data.has("sign_pic"))
                                sSign = data.optString("sign_pic");
                            else
                                sSign = null;
                        }
                        Intent intent = new Intent(Login.this, Schedule.class);
                        startActivity(intent);
                        finish();
                    }
                });
        HttpHelper.sRequestQueue.add(request);
    }

    private void showSettingsDialog(){
        final Dialog dialog = new Dialog(this, android.R.style.
                Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.settings);
        final EditText editText = (EditText)dialog.findViewById(R.id.input);
        editText.setText(HttpHelper.getServer(this));
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString().trim();
                if (s.length() == 0) {
                    Toast.makeText(Login.this, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                HttpHelper.setServer(Login.this, s);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void checkVersion(JSONObject json) {
        SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        int current = sp.getInt("current_ver", -1);
        if(current == -1 || current < BuildConfig.VERSION_CODE) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("current_ver", BuildConfig.VERSION_CODE);
            String time = sp.getString("pending_time", json.optString("add_time"));
            editor.putString("ver_time", time);
            editor.apply();
            cleanDownloadFile();
        }
        else {
            String now = json.optString("add_time");
            String pre = sp.getString("ver_time", now);
            if(!now.equals(pre)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("pending_time", now);
                editor.apply();
                promptUpgrade(json);
            }
        }
    }

    private void promptUpgrade(final JSONObject json) {
        final Dialog dialog = new Dialog(this, android.R.style.
                Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.prompt_upgrade);
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = json.optString("file_name");
                String path = json.optString("path");
                String url = HttpHelper.URL_SERVER + "/" + path + "/" + fileName;
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        fileName);
                downloadUpgrade(url, file);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void downloadUpgrade(String url, File file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.show();


        DownloadTask.DownloadListener downloadListener =
                new DownloadTask.DownloadListener() {
                    @Override
                    public void begin(int totalSize) {
                        dialog.setProgress(0);
                    }

                    @Override
                    public void update(int progress, int total) {
                        int p = progress * 100 / total;
                        dialog.setProgress(p);
                    }

                    @Override
                    public void complete(File file) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                        startActivity(intent);
                    }
                };
        final DownloadTask2 task = new DownloadTask2(downloadListener,
                url, file);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel();
            }
        });

        task.execute();
    }

    private void cleanDownloadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if(dir == null)
                    return;
                for(File d : dir.listFiles()) {
                    d.delete();
                }
            }
        }).start();
    }
}
