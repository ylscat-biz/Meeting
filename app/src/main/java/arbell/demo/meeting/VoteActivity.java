package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import arbell.demo.meeting.preach.Preach;
import arbell.demo.meeting.vote.DialogController;
import arbell.demo.meeting.vote.VoteAdapter;
import arbell.demo.meeting.vote.VoteController;
import arbell.demo.meeting.vote.VoteManager;

import static arbell.demo.meeting.Meeting.sPreach;

/**
 * Created at 04:12 2015-09-07
 */
public class VoteActivity extends Activity implements
        DialogController.VoteCreateListener,
        AdapterView.OnItemClickListener {
    private VoteAdapter mAdapter;
    private String topicId, subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        topicId = intent.getStringExtra(DocViewer.TOPIC_ID);
        subjectId = intent.getStringExtra(DocViewer.SUBJECT_ID);

        setContentView(R.layout.vote_panel);
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(VoteActivity.this);
                dialog.setContentView(R.layout.create_vote);
//                dialog.findViewById(R.id.back).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.add).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.done).setOnClickListener(new DialogController(dialog));
//                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                DialogController dc = new DialogController(dialog, VoteActivity.this);
                dc.setTopic(topicId, subjectId);
            }
        });
        ListView lv = (ListView)findViewById(R.id.vote_list);
        mAdapter = VoteManager.sInstance.getVotes(null, topicId, subjectId);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject vote = (JSONObject)parent.getAdapter().getItem(position);
        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (sPreach.getMode() == Preach.PREACH)
                    DocViewer.sPreachController.mDocViewer.uploadCurrent();
            }
        });
        new VoteController(dialog, vote) {
            @Override
            public void onVote() {
                VoteManager.sInstance.getVotes(mAdapter, topicId, subjectId);
            }
        };
        if(Meeting.sPreach.getMode() == Preach.PREACH) {
            String status = DocViewer.sPreachController.mDocViewer.getCurrentStatusString();
            Meeting.sPreach.upload(status + " " + vote.toString());
        }
    }

    @Override
    public void onVoteCreate() {
        VoteManager.sInstance.getVotes(mAdapter, topicId, subjectId);
    }
}
