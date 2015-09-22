package arbell.demo.meeting.doc;

import android.os.AsyncTask;

import com.android.volley.DefaultRetryPolicy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author YinLanshan
 *         Created at 10:46 on 2015/1/5.
 */
public class DownloadTask extends AsyncTask<String, Integer, File> {
    private static final String TAG = "download";
    private HttpURLConnection mConnection;
    private DownloadListener mListener;

    public DownloadTask(DownloadListener listener) {
        mListener = listener;
    }

    @Override
    protected File doInBackground(String... params) {
        android.util.Log.d(TAG, "Download \nfrom:" + params[0] +"\nto:" + params[1]);
        HttpURLConnection con = null;
        try {
            URL url = new URL(params[0]);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setConnectTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS);
            con.setReadTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS);
            con.connect();
            if(con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                android.util.Log.e(TAG, "connection failed code="+con.getResponseCode());
                return null;
            }
            mConnection = con;
            int totalLength = con.getContentLength();
            publishProgress(0, totalLength);
            final int STRIDE = totalLength/100*2;
            InputStream is = con.getInputStream();
            File downloadFile = new File(params[1]);
            FileOutputStream fos = new FileOutputStream(downloadFile);
            final int BUF_LEN = 1024*4;
            byte[] buffer = new byte[BUF_LEN];
            int length = is.read(buffer);
            int progress = 0, preProgress = 0;
            while (length > 0) {
                if(isCancelled()) {
                    android.util.Log.d(TAG, "Download canceled");
                    break;
                }
                fos.write(buffer, 0, length);
                progress += length;
                length = is.read(buffer);
                if(preProgress + STRIDE < progress) {
                    publishProgress(progress, totalLength);
                    preProgress = progress;
                }
            }
            fos.close();
            return downloadFile;
        } catch (IOException e) {
            android.util.Log.e(TAG, "Downloading error", e);
        }
        finally {
            if(con != null)
                con.disconnect();
            mConnection = null;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(mListener == null)
            return;
        if(values[0] == 0)
            mListener.begin(values[1]);
        else
            mListener.update(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(File file) {
        if(mListener != null)
            mListener.complete(file);
    }

    public void cancel() {
        cancel(true);
        if(mConnection != null)
            mConnection.disconnect();
    }

    public interface DownloadListener {
        void begin(int totalSize);
        void update(int progress, int total);
        void complete(File file);
    }
}
