import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class UploadRequest {
//    private static final String BOUNDARY = "**bound**";
    private static final String BOUNDARY = "----WebKitFormBoundary9fbAsJAovuZyGYjk";

    private String mMember;
    private byte[] data;
//    private String mUrl = "http://222.221.6.114:8066/app/save_sgin";
    private String mUrl = "http://192.168.199.188:8080/app/save_sgin";

    public UploadRequest(String memberid, byte[] image) {

        mMember = memberid;
        data = image;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void upload() {
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

            writeEntity(out, "id", mMember);

            writeEntity(out, "file", "lena", data);

//            writeEntity(out, "submit", "提交");

            writeBodyEnd(out);
            out.close();

            InputStream is = con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            System.out.println(line);
            con.disconnect();
//            System.out.println(new String(out.getBytes()));

        } catch (IOException e) {
            System.err.println("Upload IOException");
            e.printStackTrace();
        }

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