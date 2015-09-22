package arbell.demo.meeting.doc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.artifex.mupdfdemo.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import arbell.demo.meeting.DocViewer;
import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

/**
 * 2015-09-21 15:08
 */
public class DocPanel {
    private Activity mActivity;
    private View mPanel;

    private View mSelectedTopic;
    private HashMap<View, TopicAdapter> mTopicMap = new HashMap<>();
    private View.OnClickListener mTopicControl;
    private LinearLayout mTabPanel;
    private ListView mTopicContent;
    private TextView mTitle;

    public DocPanel(Activity activity, View panel) {
        mActivity = activity;
        mPanel = panel;
        mTabPanel = (LinearLayout)mPanel.findViewById(R.id.meeting_topic);
        mTitle = (TextView)mPanel.findViewById(R.id.topic_title);
        mTopicContent = (ListView)mPanel.findViewById(R.id.topic_content);
        mTopicControl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v != mSelectedTopic) {
                    if(mSelectedTopic != null) {
                        mSelectedTopic.setSelected(false);
                        mTopicContent.setAdapter(mTopicMap.get(mSelectedTopic));
                    }
                    v.setSelected(true);
                    TopicAdapter adapter = mTopicMap.get(v);
                    mTopicContent.setAdapter(adapter);
                    mTitle.setText(adapter.mTitle);
                    mSelectedTopic = v;
                }
            }
        };
        Request publicDocs = new Request(Request.Method.GET,
                HttpHelper.URL_BASE + "getMeetingById?id=" + Meeting.sMeetingID,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject data = response.optJSONObject("data");
                        JSONArray files = null;
                        try {
                            files = new JSONArray(data.optString("file_json"));
                        } catch (JSONException e) {
                            Log.e("Meeting", "file_json parse error", e);
                        }
                        if(files != null && files.length() > 0) {
                            TopicAdapter adapter = new TopicAdapter();
                            adapter.mTitle = "公共资料";
                            int len = files.length();
                            Subject subject = null;
                            for(int i = 0; i < len; i++) {
                                if(i%6 == 0) {
                                    subject = new Subject();
                                    adapter.mSubjects.add(subject);
                                }
                                JSONObject file = files.optJSONObject(i);
                                Doc doc = new Doc();
                                doc.name = file.optString("title");
                                doc.id = file.optString("id");
                                subject.mDocs.add(doc);
                            }
                            addTab(0, "公共资料", adapter);
                            AsyncTask.execute(new GetDocUrl(adapter));
                            if(mSelectedTopic == null) {
                                mTopicControl.onClick(mTabPanel.getChildAt(0));
                            }
                        }
                    }
                });
        Request topics = new Request(Request.Method.GET,
                HttpHelper.URL_BASE + "getMeetingTopicById?id=" + Meeting.sMeetingID,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray array = response.optJSONArray("data");
                        if(array == null)
                            return;
                        for(int i = 0; i < array.length(); i++) {
                            JSONObject json = array.optJSONObject(i);
                            JSONArray files = null;
                            try {
                                files = new JSONArray(json.optString("file_json"));
                            } catch (JSONException e) {
                                Log.e("Meeting", "file_json parse error", e);
                            }
                            TopicAdapter adapter = new TopicAdapter();
                            adapter.mTitle = json.optString("meeting_topic_title");
                            adapter.id = json.optString("id");
                            if(files != null && files.length() > 0) {
                                Subject subject = new Subject();
                                adapter.mSubjects.add(subject);
                                for(int j = 0; j < files.length(); j++) {
                                    JSONObject file = files.optJSONObject(j);
                                    Doc doc = new Doc();
                                    doc.name = file.optString("title");
                                    doc.id = file.optString("id");
                                    subject.mDocs.add(doc);
                                }
                            }
                            JSONArray subjects = json.optJSONArray("itemList");
                            if(subjects != null && subjects.length() > 0) {
                                for(int j = 0; j < subjects.length(); j++) {
                                    JSONObject subject = subjects.optJSONObject(j);
                                    Subject s = new Subject();
                                    adapter.mSubjects.add(s);
                                    s.id = subject.optString("id");
                                    s.title = subject.optString("title");
                                    try {
                                        files = new JSONArray(subject.optString("file_json"));
                                    } catch (JSONException e) {
                                        Log.e("Meeting", "file_json parse error", e);
                                    }
                                    if(files != null && files.length() > 0) {
                                        for(int k = 0; k < files.length(); k++) {
                                            JSONObject file = files.optJSONObject(k);
                                            Doc doc = new Doc();
                                            doc.name = file.optString("title");
                                            doc.id = file.optString("id");
                                            s.mDocs.add(doc);
                                        }
                                    }
                                }
                            }
                            addTab(-1, "议题" + (i + 1), adapter);
                            AsyncTask.execute(new GetDocUrl(adapter));
                        }
                        if(mSelectedTopic == null && mTabPanel.getChildCount() > 0) {
                            mTopicControl.onClick(mTabPanel.getChildAt(0));
                        }
                    }
                });
        HttpHelper.sRequestQueue.add(publicDocs);
        HttpHelper.sRequestQueue.add(topics);
    }

    private void addTab(int index, String name, TopicAdapter adapter) {
        TextView tv = (TextView)mActivity.getLayoutInflater().
                inflate(R.layout.topic_button, mTabPanel, false);
        tv.setText(name);
        if(index == 0) {
            mTabPanel.addView(tv, 0);
        }
        else {
            mTabPanel.addView(tv);
        }
        mTopicMap.put(tv, adapter);
        tv.setOnClickListener(mTopicControl);
    }

    /*private void setupDocs() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Meeting.this, DocViewer.class);
                Integer tag = (Integer)v.getTag();
                switch (tag) {
                    case R.drawable.doc_word:
                        intent.putExtra(DocViewer.FILE, "word");
                        break;
                    case R.drawable.doc_ppt:
                        intent.putExtra(DocViewer.FILE, "ppt");
                        break;
                    case R.drawable.doc_pdf:
                        intent.putExtra(DocViewer.FILE, "pdf");
                        break;
                    case R.drawable.doc_pic:
                        intent.putExtra(DocViewer.FILE, "jpg");
                        break;
                }
                startActivity(intent);
            }
        };

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout ll = (LinearLayout)findViewById(R.id.public_docs);

        for(int i = 0; i < 12; i ++) {
            int index = (int)(Math.random()*icons.length);
            ViewGroup doc = addDoc(ll, inflater,
                    icons[index], "公共资料" + (i + 1));
            doc.setOnClickListener(listener);
            doc.setTag(icons[index]);
        }
        final ListView topicContent = (ListView)mPanel.findViewById(R.id.topic_content);
        View.OnClickListener topicControl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v != mSelectedTopic) {
                    if(mSelectedTopic != null) {
                        mSelectedTopic.setSelected(false);
                        topicContent.setAdapter(mTopicMap.get(mSelectedTopic));
                    }
                    v.setSelected(true);
                    topicContent.setAdapter(mTopicMap.get(v));
                    mSelectedTopic = v;
                }
            }
        };

        ll = (LinearLayout)mPanel.findViewById(R.id.meeting_topic);
        for(int i = 0; i < 4; i++) {
            TextView tv = (TextView)inflater.inflate(R.layout.topic_button, ll, false);
            tv.setText("议题" + (i + 1));
            ll.addView(tv);
            TopicAdapter adapter = new TopicAdapter();
            setupAdapter(adapter);
            mTopicMap.put(tv, adapter);
            tv.setOnClickListener(topicControl);
        }
        topicControl.onClick(ll.getChildAt(0));
    }*/

    private ViewGroup addDoc(ViewGroup container, LayoutInflater inflater,
                             int icon, String text) {
        ViewGroup doc = (ViewGroup)inflater.inflate(R.layout.doc_item, container, false);
        ImageView iv = (ImageView)doc.getChildAt(0);
        iv.setImageResource(icon);
        TextView tv = (TextView)doc.getChildAt(1);
        tv.setText(text);
        tv.setSelected(true);
        container.addView(doc);
        return doc;
    }

    /*private void setupAdapter(TopicAdapter adapter) {
        int subjectCount = (int)(Math.random()*5) + 1;
        for(int i = 0; i < subjectCount; i++) {
            Subject subject = new Subject();
            subject.title = "讨论问题" + (i + 1);
            int docCount = (int)(Math.random()*2) + 2;
            for(int j = 1; j <= docCount; j++) {
                int index = (int)(Math.random()*icons.length);
                subject.mDocs.put("文档" + j, icons[index]);
            }
            adapter.mSubjects.add(subject);
        }
    }*/

    public class TopicAdapter extends BaseAdapter implements View.OnClickListener {
        public String mTitle, id;
        public ArrayList<Subject> mSubjects = new ArrayList<>();

        @Override
        public int getCount() {
            return mSubjects.size();
        }

        @Override
        public Object getItem(int position) {
            return mSubjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.subject_item, parent, false);
            }
            Subject subject = mSubjects.get(position);
            ArrayList<Doc> docs = subject.mDocs;
            TextView tv = (TextView)convertView.findViewById(R.id.subject);
            tv.setText(subject.title);
            LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.docs);
            int i = 0;
            for(Doc doc : docs) {
                int icon = doc.icon;
                if(icon == 0)
                    icon = R.drawable.doc;
                ViewGroup docView;
                if(i < ll.getChildCount()) {
                    docView = (ViewGroup)ll.getChildAt(i);
                    if(docView.getVisibility() != View.VISIBLE)
                        docView.setVisibility(View.VISIBLE);
                    ImageView iv = (ImageView)docView.getChildAt(0);
                    iv.setImageResource(icon);
                    tv = (TextView)docView.getChildAt(1);
                    tv.setText(doc.name);
                } else {
                    docView = addDoc(ll, inflater, icon, doc.name);
                    docView.setOnClickListener(this);
                }
                docView.setTag(doc);
                i++;
            }

            if(ll.getChildCount() > i) {
                for(; i < ll.getChildCount(); i++) {
                    ll.getChildAt(i).setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        @Override
        public void onClick(View v) {
            Doc doc = (Doc)v.getTag();
            if(doc == null || doc.id == null || doc.url == null)
                return;

            TopicAdapter adapter = mTopicMap.get(mSelectedTopic);
            String topicId = adapter.id;
            String subjectId = null;
            int pos = mTopicContent.getPositionForView(v);
            if(pos != ListView.INVALID_POSITION) {
                Subject subject = (Subject)adapter.getItem(pos);
                subjectId = subject.id;
            }
            if(doc.file == null) {
                String url = doc.url;
                int index = url.lastIndexOf('.');
                if(index == -1) {
                    return;
                }
                String name = doc.id + url.substring(index);
                File file = new File(mActivity.getExternalCacheDir(), name);
                if(file.exists()) {
                    doc.file = file;
                    openDoc(file, topicId, subjectId);
                }
                else {
                    downloadFile(file, doc, topicId, subjectId);
                }
            }
            else
                openDoc(doc.file, topicId, subjectId);
            /*Intent intent = new Intent(mActivity, DocViewer.class);
            Integer tag = (Integer)v.getTag();
            switch (tag) {
                case R.drawable.doc_word:
                    intent.putExtra(DocViewer.FILE, "word");
                    break;
                case R.drawable.doc_ppt:
                    intent.putExtra(DocViewer.FILE, "ppt");
                    break;
                case R.drawable.doc_pdf:
                    intent.putExtra(DocViewer.FILE, "pdf");
                    break;
                case R.drawable.doc_pic:
                    intent.putExtra(DocViewer.FILE, "jpg");
                    break;
            }
            startActivity(intent);*/
        }

        public void openDoc(File file, String topicId, String subjectId) {
            Intent intent = new Intent(mActivity, DocViewer.class);
            intent.putExtra(DocViewer.FILE, file.getPath());
            if(topicId != null)
                intent.putExtra(DocViewer.TOPIC_ID, topicId);
            if(subjectId != null)
                intent.putExtra(DocViewer.SUBJECT_ID, subjectId);
            mActivity.startActivity(intent);
        }

        private void downloadFile(File file,  final Doc doc, final String topicId,
                                  final String subjectId) {
            final ProgressDialog dialog = new ProgressDialog(mActivity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.show();
            DownloadTask task = new DownloadTask(new DownloadTask.DownloadListener() {
                @Override
                public void begin(int totalSize) {
                    dialog.setProgress(0);
                }

                @Override
                public void update(int progress, int total) {
                    int p = progress*100/total;
                    dialog.setProgress(p);
                }

                @Override
                public void complete(File file) {
                    dialog.dismiss();
                    if(file != null) {
                        openDoc(file, topicId, subjectId);
                        doc.file = file;
                    }
                }
            });
            task.execute(doc.url, file.getAbsolutePath());
        }
    }

    public class Subject {
        public String title;
        public String id;
        public ArrayList<Doc> mDocs = new ArrayList<>();
    }

    public class Doc {
        public String name;
        public String id;
        public int icon;
        public String url;
        public File file;
    }
}
