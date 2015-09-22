package arbell.demo.meeting.doc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import arbell.demo.meeting.R;
import arbell.demo.meeting.network.HttpHelper;

/**
 * 2015-09-21 18:32
 */
public class GetDocUrl implements Runnable{
    private DocPanel.TopicAdapter mAdapter;
    public GetDocUrl(DocPanel.TopicAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void run() {
        ArrayList<DocPanel.Subject> subjects = mAdapter.mSubjects;
        for(DocPanel.Subject sub : subjects) {
            for(DocPanel.Doc doc : sub.mDocs) {
                if(doc.id != null) {
                    String url = HttpHelper.URL_BASE + "getFileUrlById?id="
                            + doc.id;
                    String resp = HttpHelper.get(url);
                    if(resp != null) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            JSONObject data = json.optJSONObject("data");
                            String name = doc.url = data.optString("url");
                            String ext = getSuffix(name);
                            if("pdf".equals(ext))
                                doc.icon = R.drawable.doc_pdf;
                            else if("jpg".equals(ext))
                                doc.icon = R.drawable.doc_pic;
                            else if("png".equals(ext))
                                doc.icon = R.drawable.doc_pic;
                            else if("xls".equals(ext))
                                doc.icon = R.drawable.doc_excel;
                            else if("xlsx".equals(ext))
                                doc.icon = R.drawable.doc_excel;
                            else if("mp4".equals(ext))
                                doc.icon = R.drawable.doc_video;
                        } catch (JSONException e) {
                            Log.e("Meeting", "get url fail for file "
                                    + doc.name + "/" + doc.id);
                        }
                    }
                }
            }
        }

        HttpHelper.sHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public static String getSuffix(String name) {
        int index = name.lastIndexOf('.');
        if(index == -1)
            return null;
        return name.substring(index + 1);
    }
}
