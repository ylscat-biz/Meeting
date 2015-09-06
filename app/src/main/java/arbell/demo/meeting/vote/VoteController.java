package arbell.demo.meeting.vote;

import android.app.Dialog;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import arbell.demo.meeting.R;
import arbell.demo.meeting.view.FingerPaintView;

/**
 * Created at 03:18 2015-09-03
 */
public class VoteController implements View.OnClickListener, AdapterView.OnItemClickListener{
        private Dialog mDialog;
        private Vote mVote;

        public VoteController(Dialog dialog, Vote vote) {
            mDialog = dialog;
            mVote = vote;
            if(vote.multiple) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mDialog.getContext(),
                        android.R.layout.simple_list_item_multiple_choice, vote.options);

                ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
                lv.setItemsCanFocus(false);
                lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(this);
            }
            else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mDialog.getContext(),
                        android.R.layout.simple_list_item_single_choice, vote.options);

                ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
                lv.setItemsCanFocus(false);
                lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(this);
            }
            TextView title = (TextView)dialog.findViewById(R.id.title);
            title.setText(vote.title);
            dialog.findViewById(R.id.back).setOnClickListener(this);
            dialog.findViewById(R.id.vote).setOnClickListener(this);
            dialog.findViewById(R.id.clear).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back:
                    mDialog.dismiss();
                    break;
                case R.id.vote:
                    mDialog.dismiss();
                    break;
                case R.id.clear:
                    FingerPaintView fpv = (FingerPaintView)mDialog.findViewById(R.id.sign);
                    fpv.clear();
                    break;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            SparseBooleanArray sba = lv.getCheckedItemPositions();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < sba.size(); i++) {
                if(sba.valueAt(i))
                    sb.append(mVote.options.get(sba.keyAt(i))).append(" ");
            }
            TextView tv = (TextView)mDialog.findViewById(R.id.result);
            tv.setText(sb.toString());
        }
    }
