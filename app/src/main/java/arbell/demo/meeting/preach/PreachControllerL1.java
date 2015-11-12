package arbell.demo.meeting.preach;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import arbell.demo.meeting.Login;
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

        mPreachButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mPreach.getMsg() == null)
                    return;
                String msg = mPreach.getMsg();
                if(msg.startsWith(Login.sMemberID)) {
                    int index = msg.indexOf('\n');
                    String line = msg.substring(0, index);
                    mPreach.setUploadPrefix(line);
                    mPreach.setMode(Preach.PREACH);
                    mMeeting.uploadCurrent();
                    boolean force = line.charAt(line.length() - 1) == 'F';
                    if (force)
                        mPreachButton.setText("强制主讲中");
                    else
                        mPreachButton.setText("普通主讲中");
                }

            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        switch (mPreach.getMode()) {
            case Preach.SCANING:
                if(mPreach.getMsg() == null) {
                    if(Meeting.isGuest) {
                        Toast.makeText(mMeeting,
                                "列席人员不能主讲!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    promptMode();
                }
                else {
                    mPreach.setMode(Preach.FOLLOW);
                    mPreachButton.setText("跟随中");
                }
                break;
            case Preach.FOLLOW:
            case Preach.IDLE:
                if(mPreach.isForcePreaching()) {
                    Toast.makeText(mMeeting, "强制主讲中，不能退出",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
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

    private void promptMode() {
        final Dialog dialog = new Dialog(mMeeting, android.R.style.
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
                    Toast.makeText(mMeeting,
                            "已经有主讲人了",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    String flag = force ? "F" : "N";
                    mPreach.setUploadPrefix(String.format("%s %s", Login.sMemberID, flag));
                    mPreach.setMode(Preach.PREACH);
                    mMeeting.uploadCurrent();
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
                    String line = lines[1];
                    int index = line.charAt(0) - '0';
                    int msgLen = line.length();
                    mMeeting.selectTab(index);
                    if(msgLen > 1) {
                        if (index == 2) {
                            if(msg.equals(mLastVoteMsg))
                                break;
                            if(Meeting.isGuest)
                                break;
                            mLastVoteMsg = msg;
                            try {
                                String voteString = line.substring(2);

                                JSONObject vote = new JSONObject(voteString);
                                mMeeting.popupVote(vote);
                                mPreach.setMode(Preach.IDLE);
                            } catch (JSONException e) {
                                Toast.makeText(mMeeting, "无法打开投票",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if (index == 1) {
                            String s = line.substring(2);
                            int tab = Integer.parseInt(s);
                            mMeeting.mDocPanel.selectTab(tab);

                            if(lines.length == 3) {
                                openDoc(lines[2]);
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

    private void openDoc(String line) {
        int index = line.indexOf('#');
        if(index == -1) {
            index = line.indexOf(' ');
        }

        String id;
        if(index == -1)
            id = line;
        else
            id = line.substring(0, index);

        mMeeting.mDocPanel.openDoc(id);
    }
}
