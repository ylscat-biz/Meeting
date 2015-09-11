package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import arbell.demo.meeting.vote.DialogController;
import arbell.demo.meeting.vote.Vote;
import arbell.demo.meeting.vote.VoteAdapter;
import arbell.demo.meeting.vote.VoteController;

public class Meeting extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        DialogController.VoteCreateListener {
    public static final String TITLE = "title";

    private View mSelected;
    private HashMap<View, View> mTabMap = new HashMap<>();
    private View mSelectedTopic;
    private HashMap<View, ListAdapter> mTopicMap = new HashMap<>();
    public static VoteAdapter sVoteAdapter;

    int[] icons = {R.drawable.doc_word, /*R.drawable.doc_excel,*/
            R.drawable.doc_ppt, R.drawable.doc_pdf,
            R.drawable.doc_pic, /*R.drawable.doc_video*/};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting);
        String title = getIntent().getStringExtra(TITLE);
        TextView tv = (TextView)findViewById(R.id.title);
//        tv.setText(title);

        View tab = findViewById(R.id.info);
        View first = tab;
        tab.setOnClickListener(this);
        View panel = findViewById(R.id.info_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);

        tab = findViewById(R.id.docs);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.doc_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);

        tab = findViewById(R.id.vote);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.vote_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);

        tab = findViewById(R.id.annotation);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.annotation_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);

        onClick(first);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupDocs();

        if(sVoteAdapter == null) {
            sVoteAdapter = new VoteAdapter(getLayoutInflater());
            createDefaultVotes();
        }
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(Meeting.this);
                dialog.setContentView(R.layout.create_vote);
//                dialog.findViewById(R.id.back).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.add).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.done).setOnClickListener(new DialogController(dialog));
//                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                new DialogController(dialog, Meeting.this);
            }
        });
        ListView lv = (ListView)findViewById(R.id.vote_list);
        lv.setAdapter(sVoteAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v != mSelected) {
            if(mSelected != null) {
                mSelected.setSelected(false);
                mTabMap.get(mSelected).setVisibility(View.GONE);
            }
            v.setSelected(true);
            mTabMap.get(v).setVisibility(View.VISIBLE);
            mSelected = v;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Vote vote = (Vote)parent.getAdapter().getItem(position);
        Dialog dialog = new Dialog(Meeting.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new VoteController(dialog, vote);
    }

    private void setupDocs() {
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
        final ListView topicContent = (ListView)findViewById(R.id.topic_content);
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

        ll = (LinearLayout)findViewById(R.id.meeting_topic);
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
    }

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

    private void setupAdapter(TopicAdapter adapter) {
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
    }

    private void createDefaultVotes() {
        Vote vote = new Vote();
        vote.title = "评选新秀";
        vote.options.add("罗熙杰");
        vote.options.add("侯磊");
        vote.options.add("何大为");
        vote.multiple = false;
        sVoteAdapter.mVotes.add(vote);

        vote = new Vote();
        vote.title = "添置物品";
        vote.options.add("零食");
        vote.options.add("文具");
        vote.options.add("药品");
        vote.multiple = true;
        sVoteAdapter.mVotes.add(vote);
    }

    @Override
    public void onVoteCreate(Vote vote) {
        sVoteAdapter.mVotes.add(vote);
        sVoteAdapter.notifyDataSetChanged();
    }

    class TopicAdapter extends BaseAdapter implements View.OnClickListener {
        private ArrayList<Subject> mSubjects = new ArrayList<>();

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
            LayoutInflater inflater = getLayoutInflater();
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.subject_item, parent, false);
            }
            Subject subject = mSubjects.get(position);
            LinkedHashMap<String, Integer> docs = subject.mDocs;
            TextView tv = (TextView)convertView.findViewById(R.id.subject);
            tv.setText(subject.title);
            LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.docs);
            int i = 0;
            for(String name : docs.keySet()) {
                int icon = docs.get(name);
                ViewGroup doc;
                if(i < ll.getChildCount()) {
                    doc = (ViewGroup)ll.getChildAt(i);
                    if(doc.getVisibility() != View.VISIBLE)
                        doc.setVisibility(View.VISIBLE);
                    ImageView iv = (ImageView)doc.getChildAt(0);
                    iv.setImageResource(icon);
                    tv = (TextView)doc.getChildAt(1);
                    tv.setText(name);
                } else {
                    doc = addDoc(ll, inflater, icon, name);
                    doc.setOnClickListener(this);
                }
                doc.setTag(icon);
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
    }

    class Subject {
        public String title;
        public LinkedHashMap<String, Integer> mDocs = new LinkedHashMap<>();
    }
}