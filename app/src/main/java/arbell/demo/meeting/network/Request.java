package arbell.demo.meeting.network;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import arbell.demo.meeting.Application;
import arbell.demo.meeting.R;

/**
 * Created by sony on 2015/9/12.
 */
public class Request extends JsonRequest<JSONObject> {
    private static Response.ErrorListener sErrorListener =
            new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(Application.sContext, R.string.network_fail,
                    Toast.LENGTH_SHORT).show();
        }
    };

    public Request(int method, String url, String requestBody,
                   Response.Listener<JSONObject> listener) {
        super(method, url, requestBody, listener, sErrorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
            Log.d("Net", String.format("%s:\n\t\t%s", getUrl(), jsonString));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }
}
