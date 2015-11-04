package arbell.demo.meeting.network;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UploadRequest {
//    private static final String BOUNDARY = "**bound**";
    private static final String BOUNDARY = "----WebKitFormBoundary9fbAsJAovuZyGYjk";

    private LinkedHashMap<String, String> mParams =new LinkedHashMap<>();
    private Bitmap mBitmap;
    private String mUrl;
    private String mFileName = "pic.jpg";

    public UploadRequest(String url, LinkedHashMap<String, String> params,
                         Bitmap bitmap) {
        this(url, params, bitmap, null);
    }

    public UploadRequest(String url, LinkedHashMap<String, String> params,
                         Bitmap bitmap, String fileName) {
        mUrl = url;
        mParams.putAll(params);
        mBitmap = bitmap;
        if(fileName != null)
            mFileName = fileName;
    }

    public String upload() {
        try {
            URL url = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
//            headers.put("Content-Length", "92207");
            for (String headerName : headers.keySet()) {
                con.addRequestProperty(headerName, headers.get(headerName));
            }

            OutputStream out = con.getOutputStream();
//            ByteOutputStream out = new ByteOutputStream();
            for(String key : mParams.keySet())
                writeEntity(out, key, mParams.get(key));

            Bitmap.CompressFormat format;
            String type;
            if(mFileName.endsWith("png")) {
                type = "image/png";
                format = Bitmap.CompressFormat.PNG;
            }
            else {
                type = "image/jpeg";
                format = Bitmap.CompressFormat.JPEG;
            }
            writeEntityHead(out, "file", mFileName, type);
            mBitmap.compress(format, 80, out);
            writeEntityEnd(out);

//            writeEntity(out, "submit", "提交");

            writeBodyEnd(out);
            out.close();

            InputStream is = con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            Log.d("Net", "upload done: " + line);
            con.disconnect();
            return line;
//            System.out.println(new String(out.getBytes()));

        } catch (IOException e) {
            Log.e("Upload", "Upload IOException", e);
        }
        return null;
    }

    private void writeEntityHead(OutputStream out, String name, String filename,
                                 String contentType)
            throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"");
        sb.append(name);
        sb.append("\"");
        if (filename != null) {
            sb.append("; filename=\"");
            sb.append(filename);
            sb.append("\"");
        }
        if (contentType != null) {
            sb.append("\r\nContent-Type: ");
            sb.append(contentType);
        }
        sb.append("\r\n\r\n");
        out.write(sb.toString().getBytes());
    }

    private void writeEntityEnd(OutputStream out) throws IOException {
        out.write('\r');
        out.write('\n');
    }

    private void writeEntity(OutputStream out, String name, String value)
            throws IOException {
        writeEntityHead(out, name, null, null);
        out.write(value.getBytes());
        writeEntityEnd(out);
    }

    private void writeEntity(OutputStream out, String name, String filename, byte[] data)
            throws IOException {
        String type, suffix;

//        type = "image/png";
//        suffix = ".png";
        type = "image/jpeg";
        suffix = ".jpg";

        writeEntityHead(out, name, filename + suffix, type);
        out.write(data);
        writeEntityEnd(out);
    }

    private void writeBodyEnd(OutputStream out) throws IOException {
        String end = "--" + BOUNDARY + "--";
        out.write(end.getBytes());
    }
}