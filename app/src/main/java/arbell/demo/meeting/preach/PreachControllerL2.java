package arbell.demo.meeting.preach;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import arbell.demo.meeting.DocViewer;
import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;
import arbell.demo.meeting.vote.VoteController;

import static arbell.demo.meeting.Meeting.sPreach;

public class PreachControllerL2 implements View.OnClickListener, Preach.PreachListener {
    public DocViewer mDocViewer;
    private TextView mPreachButton;
    private Preach mPreach;
    private String mLastVoteMsg;

    public PreachControllerL2(DocViewer docViewer) {
        mDocViewer = docViewer;
        mPreachButton = (TextView)docViewer.findViewById(R.id.mode);
        mPreachButton.setOnClickListener(this);
        mPreach = Meeting.sPreach;
        mPreach.setListener(this);
        onUpdate(mPreach.getMsg());
        if(sPreach.getMode() == Preach.PREACH)
            mPreachButton.setText("主讲中");
        else if(sPreach.getMode() == Preach.FOLLOW)
            mPreachButton.setText("跟随中");
    }

    @Override
    public void onClick(View v) {
        switch (mPreach.getMode()) {
            case Preach.SCANING:
                mPreach.checkPreacher(new Preach.PreachListener() {
                    @Override
                    public void onUpdate(String msg) {
                        if(msg != null) {
                            if(mPreach.getMsg() == null) {
                                onUpdate(msg);
                                Toast.makeText(mDocViewer,
                                        "已经有主讲人了",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                mPreach.setMode(Preach.FOLLOW);
                                mPreachButton.setText("跟随中");
                            }
                        }
                        else {
                            mPreach.setMode(Preach.PREACH);
                            mDocViewer.uploadCurrent();
                            mPreachButton.setText("主讲中");
                        }
                    }
                });
                break;
            case Preach.FOLLOW:
            case Preach.IDLE:
                mPreach.setMode(Preach.SCANING);
                mPreachButton.setText("跟随");
                break;
            case Preach.PREACH:
                mPreach.upload(null);
                mPreach.setMode(Preach.SCANING);
                mPreachButton.setText("主讲模式");
                break;
        }
    }

    @Override
    public void onUpdate(String msg) {
        switch (mPreach.getMode()) {
            case Preach.SCANING:
                if(msg != null) {
                    mPreachButton.setText("跟随");
                }
                else {
                    mPreachButton.setText("主讲模式");
                }
                break;
            case Preach.FOLLOW:
                if(msg != null) {
                    int index = msg.indexOf('_');
                    if(index != -1){
                        String voteId = msg.substring(index + 1);
                        popupVote(voteId);
                        break;
                    }

                    index = msg.indexOf(',');
                    if(index == -1) {
                        mDocViewer.finish();
                        break;
                    }
                    String docInfo = msg.substring(index + 1);
                    index = docInfo.indexOf(';');
                    String fileId;
                    if(index != -1) {
                        fileId = docInfo.substring(0, index);
                    }
                    else {
                        fileId = docInfo;
                    }
                    if(!fileId.equals(mDocViewer.fileId)) {
                        mDocViewer.finish();
                        break;
                    }
                    if(index != -1) {
                        String p = docInfo.substring(index + 1);
                        int page = Integer.parseInt(p);
                        mDocViewer.moveToPage(page);
                    }
                }
                else {
                    mPreachButton.setText("主讲模式");
                    mPreach.setMode(Preach.SCANING);
                }
                break;
        }
    }

    private void popupVote(final String id) {
        if(mPreach.getMsg().equals(mLastVoteMsg))
            return;
        mLastVoteMsg = mPreach.getMsg();
        String url = HttpHelper.URL_BASE + "getVoteList?meetingid=" + Meeting.sMeetingID;
        Request request = new Request(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray array = response.optJSONArray("data");
                if(array == null)
                    return;
                for(int i = 0; i < array.length(); i++) {
                    JSONObject vote = array.optJSONObject(i);
                    if(id.equals(vote.optString("id"))) {
                        if(sPreach.getMode() == Preach.FOLLOW)
                            sPreach.setMode(Preach.IDLE);
                        popupVote(vote);
                        break;
                    }
                }
            }
        });
        HttpHelper.sRequestQueue.add(request);
    }

    private void popupVote(JSONObject vote) {
        Dialog dialog = new Dialog(mDocViewer, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (sPreach.getMode() == Preach.IDLE)
                    sPreach.setMode(Preach.FOLLOW);
            }
        });
        new VoteController(dialog, vote);
    }
}
