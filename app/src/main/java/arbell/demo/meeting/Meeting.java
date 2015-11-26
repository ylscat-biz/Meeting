package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import arbell.demo.meeting.annotation.AnnotationController;
import arbell.demo.meeting.doc.DocPanel;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;
import arbell.demo.meeting.preach.Preach;
import arbell.demo.meeting.preach.PreachControllerL1;
import arbell.demo.meeting.vote.DialogController;
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
    private ArrayList<View> mTabs = new ArrayList<>();
    private VoteAdapter mVoteAdapter;
    private AnnotationController mAnnCtr;

    public DocPanel mDocPanel;

    public static String sMeetingID;
    public static Preach sPreach;
    public static boolean isGuest;
    public PreachControllerL1 mPreachController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signIn();

        setContentView(R.layout.meeting);
        sMeetingID = getIntent().getStringExtra(ID);
        if(sMeetingID == null && savedInstanceState != null) {
            sMeetingID = savedInstanceState.getString("meetingId");
        }
        if(sPreach != null)
            sPreach.stop();
        sPreach = new Preach(sMeetingID);
        isGuest = false;
        mPreachController = new PreachControllerL1(this);
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
        mTabs.add(tab);

        tab = findViewById(R.id.docs);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.doc_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);
        mTabs.add(tab);
        mDocPanel = new DocPanel(this, panel);

        tab = findViewById(R.id.vote);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.vote_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);
        mTabs.add(tab);

        tab = findViewById(R.id.annotation);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.annotation_panel);
        panel.setVisibility(View.GONE);
        mAnnCtr = new AnnotationController(panel);
        mTabMap.put(tab, panel);
        mTabs.add(tab);

        onClick(first);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptExit();
            }
        });

        if(VoteManager.sInstance == null) {
            VoteManager.sInstance = new VoteManager(getLayoutInflater());
        }
        mVoteAdapter = new VoteAdapter(getLayoutInflater());
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sPreach.getMode() == Preach.FOLLOW) {
                    Toast.makeText(Meeting.this, "跟随中，无法操作",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Meeting.isGuest) {
                    Toast.makeText(Meeting.this,
                            "列席人员不能创建投票!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
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
        //刷新投票列表
        VoteManager.sInstance.getVotes(mVoteAdapter, null, null);
        sPreach.setListener(mPreachController);
        mPreachController.onUpdate(sPreach.getMsg());
        sPreach.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sPreach == null)
            return;
        if(sPreach.isScanningCache()) {
            sPreach.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sPreach == null)
            return;
        if(sPreach.getMode() == Preach.PREACH)
            sPreach.upload(null);
        sPreach.stop();
        sPreach = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("meetingId", sMeetingID);
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
        FlowLayout layout = (FlowLayout)panel.findViewById(R.id.attendant);
        TextView guest = (TextView)panel.findViewById(R.id.guests);
        Refresh refresh = new Refresh(button, progress, layout, guest);
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
            int mode = sPreach.getMode();
            if(mode == Preach.FOLLOW) {
                Toast.makeText(this, "跟随中，无法操作",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            selectTab(v);
            if(mode == Preach.PREACH) {
                uploadCurrent();
            }
        }
    }

    public void selectTab(int index) {
        View tab = mTabs.get(index);
        if(tab != null && tab != mSelected)
            selectTab(tab);
    }

    private void selectTab(View v) {
        if(mSelected != null) {
            mSelected.setSelected(false);
            mTabMap.get(mSelected).setVisibility(View.GONE);
        }
        v.setSelected(true);
        mTabMap.get(v).setVisibility(View.VISIBLE);
        mSelected = v;
        if(v.getId() == R.id.annotation) {
            mAnnCtr.refresh();
        }
    }

    public void uploadCurrent() {
        int index = mTabs.indexOf(mSelected);
        String msg = String.valueOf(index);
        if(index == 1) {
            int docTab = mDocPanel.getTabIndex();
            if(docTab != -1)
                msg += " " + docTab;
        }
        sPreach.upload(msg);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(sPreach.getMode() == Preach.FOLLOW) {
            Toast.makeText(this, "跟随中，无法操作",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject vote = (JSONObject)parent.getAdapter().getItem(position);
        popupVote(vote);
        if(sPreach.getMode() == Preach.PREACH) {
            sPreach.upload("2 " + vote.toString());
        }
    }

    public void popupVote(JSONObject vote) {
        if(Meeting.isGuest) {
            Toast.makeText(Meeting.this,
                    "列席人员不能投票!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Dialog dialog = new Dialog(Meeting.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(sPreach.getMode() == Preach.PREACH)
                    sPreach.upload("2");
                else {
                    sPreach.resume();
                }
            }
        });
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

    public JSONObject getVote(int position) {
        if(position < mVoteAdapter.getCount())
            return (JSONObject)mVoteAdapter.getItem(position);
        else {
            VoteManager.sInstance.getVotes(mVoteAdapter, null, null);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        promptExit();
    }

    private void promptExit() {
        final Dialog dialog = new Dialog(Meeting.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.prompt);
        dialog.setCanceledOnTouchOutside(false);
        TextView tv = (TextView)dialog.findViewById(R.id.content);
        tv.setText("确定退出会议吗?");
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                cleanAndExit();
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void cleanAndExit() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();
        dialog.setTitle("删除缓存资料...");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File dir = getExternalCacheDir();
                if(dir != null) {
                    File[] files = dir.listFiles();
                    if(files != null) {
                        deleteDir(dir);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();
                finish();
            }

            private void deleteDir(File dir) {
                File[] files = dir.listFiles();
                if(files != null) {
                    for(File f : files) {
                        if(f.isFile())
                            f.delete();
                        else if(f.isDirectory())
                            deleteDir(f);
                    }
                }
                dir.delete();
            }
        }.execute();
    }

    class Refresh implements View.OnClickListener ,
            Response.Listener<JSONObject>, Response.ErrorListener {
        private View mButton, mProgress;
        private FlowLayout mList;
        private TextView mGuests;
        private Request mRequest;

        private LinkedHashMap<String, TextView> mMembers = new LinkedHashMap<>();
        private LinkedHashSet<String> mSigned = new LinkedHashSet<>();

        public Refresh(View button, View progress, FlowLayout list, TextView guests) {
            mButton = button;
            mProgress = progress;
            mList = list;
            mGuests = guests;
            mRequest = new Request(Request.Method.GET,
                    HttpHelper.URL_BASE + "getSignMember?id=" + sMeetingID,
                    null, this, this);
            Request members = new Request(Request.Method.GET,
                    HttpHelper.URL_BASE + "getlxmember?meetingid=" + sMeetingID,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONArray list = response.optJSONArray("data");
                    if(list == null)
                        return;

                    ArrayList<String> guests = new ArrayList<>();
                    //reverse the name list
                    String memberId = Login.sMemberID;
                    for(int i = list.length() - 1; i >= 0; i--) {
                        JSONObject json = list.optJSONObject(i);
                        String name = json.optString("name");
                        String id = json.optString("id");
                        boolean isGuest = "yes".equals(json.optString("lx"));
                        if(isGuest) {
                            guests.add(name);
                            if(memberId.equals(id))
                                Meeting.isGuest = true;
                        } else {
                            TextView tv = new TextView(Meeting.this);
                            tv.setTextColor(Color.GRAY);
                            tv.setText(name);
                            mMembers.put(name, tv);
                            mList.addView(tv);
                            FlowLayout.LayoutParams lp = (FlowLayout.LayoutParams)
                                    tv.getLayoutParams();
                            lp.setMargins(10, 10, 10, 10);
                        }
                    }
                    if(guests.size() == 0) {
                        mGuests.setText("无");
                    }
                    else {
                        StringBuilder sb = new StringBuilder();
                        for(String name : guests) {
                            sb.append(name).append(' ');
                        }
                        mGuests.setText(sb.substring(0, sb.length() - 1));
                    }

                    refreshList(mSigned);
                }
            });
            HttpHelper.sRequestQueue.add(members);
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
            LinkedHashSet<String> signed = mSigned;
            if(list != null && list.length() > 0) {
                for(int i = 0; i < list.length(); i++) {
                    String member = list.optJSONObject(i).optString("id");
                    signed.add(member);
                }
            }

            refreshList(signed);
            mButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Request.sErrorListener.onErrorResponse(error);
            mButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }

        private void refreshList(LinkedHashSet<String> signed) {
            if(mMembers.size() == 0)
                return;

            for(String name : signed) {
                TextView tv = mMembers.get(name);
                if(tv != null)
                    tv.setTextColor(Color.BLACK);
            }
        }
    }
}
