import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Author: Yin
 * Create on: 15-9-20 22:25
 */
public class Test {
    private static final String BASE_URL = "http://222.221.6.114:8066/app/";
//    private static final String BASE_URL = "http://192.168.199.204:8080/app/login";

    public static void main(String ... args) throws IOException {
//        testLogin();
//        testList();
//        testGetMeeting();
//        testGetMeetingTopics();
//        testDownload();
//        testCreateVote();
//        testVote();
//        testVoteList();
//        testSign();
//        testGetSigned();

//        testCache();
//        testUploadSign();
//        setCache("2");
        setCache(vote);
        getCache();
    }
    
    private static void testLogin() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", "az");
        params.put("password", "123123");
        String resp = Network.post(BASE_URL + "login", params);
        System.out.println(resp);
    }

    private static void testList() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        String resp = Network.post(BASE_URL + "getMeeting", params);
        System.out.println(resp);
    }

    private static void testGetMeeting() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", "20150920174757597d9pklhg9");
        String resp = Network.get(BASE_URL + "getMeetingById", params);
        System.out.println(resp);
    }

    private static void testGetMeetingTopics() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", "20150920174757597d9pklhg9");
        String resp = Network.get(BASE_URL + "getMeetingTopicById", params);
        System.out.println(resp);
    }

    private static void testCreateVote() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "20150920174757597d9pklhg9");
        params.put("topicid", "20150920174814643gz92mmzb");
        params.put("title", "测试创建投票1");
        String resp = Network.post("http://192.168.199.204:8080/app/createVote", params);
        System.out.println(resp);
    }

    private static void testVote() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "20150920174757597d9pklhg9");
        params.put("voteid", "20150921010126216cnm6krsa");
        params.put("itemid", "20150921010126285f7053tcy");
        params.put("memberid", "201508201120450448h9huep5");
//        String resp = Network.post("http://192.168.199.204:8080/app/vote", params);
        String resp = Network.post(BASE_URL + "vote", params);
        System.out.println(resp);
    }

    private static void testVoteList() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "20150920174757597d9pklhg9");
        params.put("topicid", "20150920174814643gz92mmzb");
//        params.put("title", "测试创建投票1");
//        String resp = Network.get("http://192.168.199.204:8080/app/getVoteList", params);
        String resp = Network.get(BASE_URL + "getVoteList", params);
        System.out.println(resp);
    }

    private static void testDownload() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", "20150920175141211nzq8m07r");
        String resp = Network.get(BASE_URL + "getFileUrlById", params);
        System.out.println(resp);
    }

    private static void testSign() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "20150920174757597d9pklhg9");
        params.put("memberid", "201508201120450448h9huep5");
        String resp = Network.post(BASE_URL + "sign", params);
        System.out.println(resp);
    }

    private static void testGetSigned() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", "20150920174757597d9pklhg9");
        String resp = Network.get(BASE_URL + "getSignMember", params);
        System.out.println(resp);
    }

    private static void testCache() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "20150920174757597d9pklhg9");
        params.put("msg", "begin");
        String resp = Network.get(BASE_URL + "setCache", params);
        System.out.println("set cache:" + resp);

        params.clear();
        params.put("meetingid", "20150920174757597d9pklhg9");
        resp = Network.get(BASE_URL + "getCache", params);
        System.out.println("get cache:" + resp);

        params.clear();
        params.put("meetingid", "20150920174757597d9pklhg9");
//        params.put("msg", "");
        resp = Network.get(BASE_URL + "setCache", params);
        System.out.println("clear cache:" + resp);

        params.clear();
        params.put("meetingid", "20150920174757597d9pklhg9");
        resp = Network.get(BASE_URL + "getCache", params);
        System.out.println("get cache:" + resp);

        params.clear();
        params.put("meetingid", "123");
        resp = Network.get(BASE_URL + "getCache", params);
        System.out.println("get cache without id:" + resp);
    }

    private static void testUploadSign() throws IOException {
        ByteOutputStream bos = new ByteOutputStream();
        FileInputStream fis = new FileInputStream("D:/lena.jpg");
        byte[] buf = new byte[4096];
        for(int len = fis.read(buf); len > 0; len = fis.read(buf))
            bos.write(buf, 0, len);
        fis.close();

        UploadRequest upload = new UploadRequest("201508201120450448h9huep5", bos.getBytes());
        upload.upload();
    }

    private static void setCache(String s) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "201509212134534241nzm76ly");
        if(s != null)
        params.put("msg", s);
        String resp = Network.post(BASE_URL + "setCache", params);
        System.out.println("set cache:" + resp);
    }

    private static void getCache() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("meetingid", "201509212134534241nzm76ly");

        String resp = Network.post(BASE_URL + "getCache", params);
        System.out.println("cache:" + resp);
        try {
            JSONObject json = new JSONObject(resp);
            if(json.has("data")) {
                String s = json.optString("data");
                JSONObject data = new JSONObject(s);
                System.out.println("vote:\n" + data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String vote = "{\n" +
            "      \"id\": \"20150920221548280snd3aon0\",\n" +
            "      \"title\": \"绿化条件是否达标进行投票\",\n" +
            "      \"meeting_id\": \"20150920174757597d9pklhg9\",\n" +
            "      \"topic_id\": \"20150920174814643gz92mmzb\",\n" +
            "      \"topic_item_id\": \"\",\n" +
            "      \"itemlist\": [\n" +
            "        {\n" +
            "          \"id\": \"20150920221549594xpeggcsl\",\n" +
            "          \"title\": \"同意\",\n" +
            "          \"vote_num\": 0,\n" +
            "          \"vote_id\": \"20150920221548280snd3aon0\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"20150920221550099uk2axogg\",\n" +
            "          \"title\": \"不同意\",\n" +
            "          \"vote_num\": 0,\n" +
            "          \"vote_id\": \"20150920221548280snd3aon0\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"20150920221550126i5xyf32u\",\n" +
            "          \"title\": \"弃权\",\n" +
            "          \"vote_num\": 0,\n" +
            "          \"vote_id\": \"20150920221548280snd3aon0\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }";
}
