package arbell.demo.meeting.preach;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arbell.demo.meeting.DocViewer;
import arbell.demo.meeting.Login;
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

        if(mPreach.getMode() == Preach.PREACH) {
            String upload = mPreach.getUploadPrefix();
            String label = "普通主讲中";
            if(upload != null) {
                int len = upload.length();
                char c = upload.charAt(len - 1);
                if(c == 'F')
                    label = "强制主讲中";
            }
            mPreachButton.setText(label);
            mDocViewer.setEnable(false);
        }
        else if(mPreach.getMode() == Preach.FOLLOW)
            mPreachButton.setText("跟随中");

        mPreach.setListener(this);
        onUpdate(mPreach.getMsg());
    }

    @Override
    public void onClick(View v) {
        switch (mPreach.getMode()) {
            case Preach.SCANING:
                if(mPreach.getMsg() == null) {
                    promptMode();
                }
                else {
                    mPreach.setMode(Preach.FOLLOW);
                    mPreachButton.setText("跟随中");
                    mDocViewer.setEnable(false);
                }
                break;
            case Preach.FOLLOW:
            case Preach.IDLE:
                if(mPreach.isForcePreaching()) {
                    Toast.makeText(mDocViewer, "强制主讲中，不能退出",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mPreach.setMode(Preach.SCANING);
                mPreachButton.setText("跟随");
                mDocViewer.setEnable(true);
                break;
            case Preach.PREACH:
                mPreach.upload(null);
                mPreach.setMode(Preach.SCANING);
                mPreachButton.setText("主讲模式");
                mDocViewer.setEnable(true);
                break;
        }
    }

    private void promptMode() {
        final Dialog dialog = new Dialog(mDocViewer, android.R.style.
                Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.preach_mode_prompt);
        dialog.findViewById(R.id.normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreach(false);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.force).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreach(true);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void startPreach(final boolean force) {
        mPreach.checkPreacher(new Preach.PreachListener() {
            @Override
            public void onUpdate(String msg) {
                if(msg != null) {
                    Toast.makeText(mDocViewer,
                            "已经有主讲人了",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    String flag = force ? "F" : "N";
                    mPreach.setUploadPrefix(String.format("%s %s", Login.sMemberID, flag));
                    mPreach.setMode(Preach.PREACH);
                    mDocViewer.uploadCurrent();
                    mDocViewer.setEnable(true);
                    if (force)
                        mPreachButton.setText("强制主讲中");
                    else
                        mPreachButton.setText("普通主讲中");
                }
            }
        });
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
                    String[] lines = msg.split("\n");
                    if(lines.length < 3) {
                        mDocViewer.finish();
                        break;
                    }

                    String line = lines[2];
                    int voteMark = line.indexOf(' ');
                    if(voteMark != - 1) {
                        if(mLastVoteMsg != null && mLastVoteMsg.equals(mPreach.getMsg()))
                            break;
                        String vote = line.substring(voteMark + 1);
                        try {
                            JSONObject json = new JSONObject(vote);
                            popupVote(json);
                            mLastVoteMsg = msg;
                            mPreach.setMode(Preach.IDLE);
                        } catch (JSONException e) {
                            Toast.makeText(mDocViewer, "无法打开投票",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }

                    int pageMark = line.indexOf('#');
                    String fileId;
                    int page = -1;
                    if(pageMark != -1) {
                        fileId = line.substring(0, pageMark);
                        String pageStr = line.substring(pageMark + 1);
                        page = Integer.parseInt(pageStr);
                    }
                    else {
                        fileId = line;
                    }

                    if(!fileId.equals(mDocViewer.fileId)) {
                        mDocViewer.finish();
                        break;
                    }
                    if(page != -1) {
                        mDocViewer.moveToPage(page);
                    }
                }
                else {
                    mPreachButton.setText("主讲模式");
                    mPreach.setMode(Preach.SCANING);
                    mDocViewer.setEnable(true);
                }
                break;
        }
    }

    private void popupVote(final String id) {
        if(mLastVoteMsg != null && mLastVoteMsg.equals(mPreach.getMsg()))
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
