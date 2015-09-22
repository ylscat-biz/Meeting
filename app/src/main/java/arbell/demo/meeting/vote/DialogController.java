package arbell.demo.meeting.vote;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

/**
 * Created at 03:18 2015-09-03
 */
public class DialogController implements View.OnClickListener {
    private Dialog mDialog;
    private LinearLayout options;
    private VoteCreateListener listener;

    private String topicId, subjectId;

    public DialogController(Dialog dialog, VoteCreateListener listener) {
        mDialog = dialog;
        options = (LinearLayout) mDialog.findViewById(R.id.options);
        this.listener = listener;
        Context context = dialog.getContext();
        EditText et = new EditText(context);
        options.addView(et);
        et.setText("同意");
        et = new EditText(context);
        options.addView(et);
        et.setText("不同意");
        et = new EditText(context);
        options.addView(et);
        et.setText("弃权");
        dialog.findViewById(R.id.back).setOnClickListener(this);
        dialog.findViewById(R.id.add).setOnClickListener(this);
        dialog.findViewById(R.id.done).setOnClickListener(this);
    }

    public void setTopic(String topic, String subject) {
        topicId = topic;
        subjectId = subject;
    }

    @Override
    public void onClick(View v) {
        Context context = mDialog.getContext();
        switch (v.getId()) {
            case R.id.back:
                mDialog.dismiss();
                break;
            case R.id.add:
                if (checkEmpty()) {
                    Toast.makeText(context, "不能有空选项", Toast.LENGTH_SHORT).show();
                    return;
                }
                EditText et = new EditText(context);
                options.addView(et);
                et.requestFocus();
                break;
            case R.id.done:
                Vote vote = createVote();
                if (vote.title.length() == 0) {
                    Toast.makeText(context, "主题不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (vote.options.size() == 0) {
                    Toast.makeText(context, "还没有选项", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDialog.dismiss();

                StringBuilder sb = new StringBuilder("meetingid");
                sb.append('=').append(Meeting.sMeetingID);
                sb.append("&title=").append(vote.title);
                if(topicId != null)
                    sb.append("&topicid=").append(topicId);
                if(subjectId != null)
                    sb.append("&itemid=").append(subjectId);
                Request request = new Request(Request.Method.POST,
                        HttpHelper.URL_BASE + "createVote", sb.toString(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if(listener != null)
                                    listener.onVoteCreate();
                            }
                        });
                HttpHelper.sRequestQueue.add(request);

                break;
        }
    }

    private boolean checkEmpty() {
        for (int i = 0; i < options.getChildCount(); i++) {
            EditText et = (EditText) options.getChildAt(i);
            String item = et.getText().toString().trim();
            if (item.length() == 0)
                return true;
        }

        return false;
    }

    private Vote createVote() {
        Vote vote = new Vote();
        EditText edit = (EditText) mDialog.findViewById(R.id.title);
        vote.title = edit.getText().toString();

        for (int i = 0; i < options.getChildCount(); i++) {
            EditText et = (EditText) options.getChildAt(i);
            String item = et.getText().toString().trim();
            if (item.length() == 0)
                continue;
            vote.options.add(item);
        }

        Switch sw = (Switch) mDialog.findViewById(R.id.multiple);
        vote.multiple = sw.isChecked();
        return vote;
    }

    public interface VoteCreateListener {
        void onVoteCreate();
    }
}
