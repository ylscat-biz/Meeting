package arbell.demo.meeting.preach;

import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;

public class PreachControllerL1 implements View.OnClickListener, Preach.PreachListener {
    private Meeting mMeeting;
    private TextView mPreachButton;
    private Preach mPreach;
    private String mLastVoteMsg;

    public PreachControllerL1(Meeting meeting) {
        mMeeting = meeting;
        mPreachButton = (TextView)meeting.findViewById(R.id.mode);
        mPreachButton.setOnClickListener(this);
        mPreach = Meeting.sPreach;
        mPreach.setMode(Preach.SCANING);
        mPreach.setListener(this);
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
                                Toast.makeText(mMeeting,
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
                            mMeeting.uploadCurrent();
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
                    int index = msg.charAt(0) - '0';
                    int msgLen = msg.length();
                    mMeeting.selectTab(index);
                    if(msgLen > 1) {
                        if (index == 2) {
                            if(msg.equals(mLastVoteMsg))
                                break;
                            mLastVoteMsg = msg;
                            try {
                                String voteString = msg.substring(1);
                                int votePos = Integer.parseInt(voteString);
                                JSONObject vote = mMeeting.getVote(votePos);
                                mMeeting.popupVote(vote);
                                mPreach.setMode(Preach.IDLE);
                            } catch (NumberFormatException e) {
                                Toast.makeText(mMeeting, "无法打开投票",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if (index == 1) {
                            index = msg.indexOf(',');
                            if(index > 2) {
                                int tab = Integer.parseInt(msg.substring(1, index));
                                mMeeting.mDocPanel.selectTab(tab);
                            }
                            else if(index == -1) {
                                int tab = Integer.parseInt(msg.substring(1));
                                mMeeting.mDocPanel.selectTab(tab);
                            }

                            if(index != -1) {
                                String file = msg.substring(index + 1);
                                openDoc(file);
                            }
                        }
                    }
                }
                else {
                    mPreachButton.setText("主讲模式");
                    mPreach.setMode(Preach.SCANING);
                }
                break;
        }
    }

    private void openDoc(String file) {
        int index = file.indexOf(';');
        String id = file;
        if(index != -1)
            id = file.substring(0, index);
        mMeeting.mDocPanel.openDoc(id);
    }
}
