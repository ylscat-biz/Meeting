package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

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
}
