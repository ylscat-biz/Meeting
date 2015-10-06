package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
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
    }

    @Override
    public void onClick(View v) {
        login();
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
                            sMemberName = data.optString("name");
                        }
                        Intent intent = new Intent(Login.this, Schedule.class);
                        startActivity(intent);
                        finish();
                    }
                });
        HttpHelper.sRequestQueue.add(request);
    }
}
