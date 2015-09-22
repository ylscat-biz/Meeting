package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import arbell.demo.meeting.doc.DocPanel;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;
import arbell.demo.meeting.vote.DialogController;
import arbell.demo.meeting.vote.Vote;
import arbell.demo.meeting.vote.VoteAdapter;
import arbell.demo.meeting.vote.VoteController;
import arbell.demo.meeting.vote.VoteManager;

public class Meeting extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        DialogController.VoteCreateListener {
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String ADDRESS = "address";
    public static final String HOST = "host";
    public static final String TIME = "time";
    public static final String TOPIC = "topic";

    private View mSelected;
    private HashMap<View, View> mTabMap = new HashMap<>();
    private VoteAdapter mVoteAdapter;

    public static String sMeetingID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signIn();

        setContentView(R.layout.meeting);
        sMeetingID = getIntent().getStringExtra(ID);
        String title = getIntent().getStringExtra(TITLE);
        TextView tv = (TextView)findViewById(R.id.title);
        tv.setText(title);

        View tab = findViewById(R.id.info);
        View first = tab;
        tab.setOnClickListener(this);
        View panel = findViewById(R.id.info_panel);
        panel.setVisibility(View.GONE);
        setupInfo(panel);
        mTabMap.put(tab, panel);

        tab = findViewById(R.id.docs);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.doc_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);
        new DocPanel(this, panel);

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

        if(VoteManager.sInstance == null) {
            VoteManager.sInstance = new VoteManager(getLayoutInflater());
        }
        mVoteAdapter = new VoteAdapter(getLayoutInflater());
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
        lv.setAdapter(mVoteAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VoteManager.sInstance.getVotes(mVoteAdapter, null, null);
    }

    private void setupInfo(View panel) {
        Intent intent = getIntent();
        TextView tv = (TextView)panel.findViewById(R.id.title);
        tv.setText(intent.getStringExtra(TITLE));
        tv = (TextView)panel.findViewById(R.id.time);
        tv.setText(intent.getStringExtra(TIME));
        tv = (TextView)panel.findViewById(R.id.address);
        tv.setText(intent.getStringExtra(ADDRESS));
        tv = (TextView)panel.findViewById(R.id.host);
        tv.setText(intent.getStringExtra(HOST));
        tv = (TextView)panel.findViewById(R.id.topic);
        tv.setText(intent.getStringExtra(TOPIC));
        View button = panel.findViewById(R.id.refresh);
        View progress = panel.findViewById(R.id.refresh_bar);
        tv = (TextView)panel.findViewById(R.id.signed_list);
        Refresh refresh = new Refresh(button, progress, tv);
        button.setOnClickListener(refresh);
        refresh.onClick(button);
    }

    private void signIn() {
        String params = String.format("memberid=%s&meetingid=%s",
                Login.sMemberID, getIntent().getStringExtra(ID));
        Request request = new Request(Request.Method.POST,
                HttpHelper.URL_BASE + "sign",
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.optBoolean("success"))
                            Toast.makeText(Meeting.this,
                                    response.optString("msg"),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
        HttpHelper.sRequestQueue.add(request);
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
        JSONObject vote = (JSONObject)parent.getAdapter().getItem(position);
        Dialog dialog = new Dialog(Meeting.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new VoteController(dialog, vote) {
            @Override
            public void onVote() {
                VoteManager.sInstance.getVotes(mVoteAdapter, null, null);
            }
        };
    }

    @Override
    public void onVoteCreate() {
        VoteManager.sInstance.getVotes(mVoteAdapter, null, null);
    }

    class Refresh implements View.OnClickListener ,
            Response.Listener<JSONObject>, Response.ErrorListener {
        private View mButton, mProgress;
        private TextView mList;
        private Request mRequest;

        public Refresh(View button, View progress, TextView list) {
            mButton = button;
            mProgress = progress;
            mList = list;
            mRequest = new Request(Request.Method.GET,
                    HttpHelper.URL_BASE + "getSignMember?id=" + sMeetingID,
                    null, this, this);
        }

        @Override
        public void onClick(View v) {
            mButton.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            HttpHelper.sRequestQueue.add(mRequest);
        }

        @Override
        public void onResponse(JSONObject response) {
            JSONArray list = response.optJSONArray("data");
            StringBuilder sb = new StringBuilder();
            if(list != null && list.length() > 0) {
                for(int i = 0; i < list.length(); i++) {
                    String member = list.optJSONObject(i).optString("id");
                    sb.append(member);
                    sb.append(' ');
                }
            }

            mList.setText(sb.toString());
            mButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Request.sErrorListener.onErrorResponse(error);
            mButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
