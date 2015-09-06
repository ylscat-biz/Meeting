package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arbell.demo.meeting.vote.DialogController;
import arbell.demo.meeting.vote.Vote;
import arbell.demo.meeting.vote.VoteController;

/**
 * Created at 04:12 2015-09-07
 */
public class VoteActivity extends Activity implements DialogController.VoteCreateListener, AdapterView.OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                new DialogController(dialog, VoteActivity.this);
            }
        });
        ListView lv = (ListView)findViewById(R.id.vote_list);
        lv.setAdapter(Meeting.sVoteAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Vote vote = (Vote)parent.getAdapter().getItem(position);
        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.vote);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new VoteController(dialog, vote);
    }

    @Override
    public void onVoteCreate(Vote vote) {
        Meeting.sVoteAdapter.mVotes.add(vote);
        Meeting.sVoteAdapter.notifyDataSetChanged();
    }
}
