package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Meeting extends Activity implements View.OnClickListener {
    public static final String TITLE = "title";

    private View mSelected;
    private HashMap<View, View> mTabMap = new HashMap<View, View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting);
        String title = getIntent().getStringExtra(TITLE);
        TextView tv = (TextView)findViewById(R.id.title);
//        tv.setText(title);

        View tab = findViewById(R.id.info);
        View first = tab;
        tab.setOnClickListener(this);
        View panel = findViewById(R.id.info_panel);
        panel.setVisibility(View.GONE);
        mTabMap.put(tab, panel);

        tab = findViewById(R.id.docs);
        tab.setOnClickListener(this);
        panel = findViewById(R.id.doc_panel);
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

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Meeting.this, DocViewer.class);
                startActivity(intent);
            }
        };
        findViewById(R.id.doc_item1).setOnClickListener(listener);
        findViewById(R.id.doc_item2).setOnClickListener(listener);
        findViewById(R.id.doc_item3).setOnClickListener(listener);
        findViewById(R.id.doc_item4).setOnClickListener(listener);
        findViewById(R.id.doc_item5).setOnClickListener(listener);
        findViewById(R.id.doc_item6).setOnClickListener(listener);
        findViewById(R.id.doc_item7).setOnClickListener(listener);
        findViewById(R.id.doc_item8).setOnClickListener(listener);
//        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Dialog dialog = new Dialog(Meeting.this);
//                dialog.setContentView(R.layout.create_vote);
//                dialog.findViewById(R.id.back).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.add).setOnClickListener(new DialogController(dialog));
//                dialog.findViewById(R.id.done).setOnClickListener(new DialogController(dialog));
//                dialog.setCancelable(false);
//                dialog.show();
//            }
//        });
//        createDefaultVotes();
//        ListView lv = (ListView)findViewById(R.id.vote_list);
//        lv.setAdapter(mVoteAdapter);
//        lv.setOnItemClickListener(this);
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
/*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Vote vote = (Vote)parent.getAdapter().getItem(position);
        Dialog dialog = new Dialog(Meeting.this);
        dialog.setContentView(R.layout.vote);
        dialog.findViewById(R.id.back).setOnClickListener(new VoteController(dialog, vote));
        dialog.setCancelable(false);
        dialog.show();
    }


    private void createDefaultVotes() {
        Vote vote = new Vote();
        vote.title = "评选新秀";
        vote.options.add("罗熙杰");
        vote.options.add("侯磊");
        vote.options.add("何大为");
        vote.multiple = false;
        mVoteAdapter.mVotes.add(vote);

        vote = new Vote();
        vote.title = "添置物品";
        vote.options.add("零食");
        vote.options.add("文具");
        vote.options.add("药品");
        vote.multiple = true;
        mVoteAdapter.mVotes.add(vote);
    }

    class VoteAdapter extends BaseAdapter {
        ArrayList<Vote> mVotes = new ArrayList<Vote>();

        @Override
        public int getCount() {
            return mVotes.size();
        }

        @Override
        public Object getItem(int position) {
            return mVotes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getLayoutInflater().inflate(
                        android.R.layout.simple_list_item_1, parent, false);
            }

            TextView tv = (TextView)convertView;
            tv.setText((position + 1) + "   " + mVotes.get(position).title);
            return convertView;
        }
    }

    class Vote {
        String title;
        ArrayList<String> options = new ArrayList<String>();
        boolean multiple;
    }

    class DialogController implements View.OnClickListener {
        private Dialog mDialog;
        private LinearLayout options;

        public DialogController(Dialog dialog) {
            mDialog = dialog;
            options = (LinearLayout)mDialog.findViewById(R.id.options);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back:
                    mDialog.dismiss();
                    break;
                case R.id.add:
                    if(checkEmpty()) {
                        Toast.makeText(Meeting.this, "不能有空选项", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    EditText et = new EditText(Meeting.this);
                    options.addView(et);
                    et.requestFocus();
                    break;
                case R.id.done:
                    Vote vote = createVote();
                    if(vote.title.length() == 0) {
                        Toast.makeText(Meeting.this, "主题不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(vote.options.size() == 0) {
                        Toast.makeText(Meeting.this, "还没有选项", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mDialog.dismiss();
                    mVoteAdapter.mVotes.add(vote);
                    mVoteAdapter.notifyDataSetChanged();
                    break;
            }
        }

        private boolean checkEmpty() {
            for(int i = 0; i < options.getChildCount(); i++) {
                EditText et = (EditText)options.getChildAt(i);
                String item = et.getText().toString().trim();
                if(item.length() == 0)
                    return true;
            }

            return false;
        }

        private Vote createVote() {
            Vote vote = new Vote();
            EditText edit = (EditText)mDialog.findViewById(R.id.title);
            vote.title = edit.getText().toString();

            for(int i = 0; i < options.getChildCount(); i++) {
                EditText et = (EditText)options.getChildAt(i);
                String item = et.getText().toString().trim();
                if(item.length() == 0)
                    continue;
                vote.options.add(item);
            }

            Switch sw = (Switch)mDialog.findViewById(R.id.multiple);
            vote.multiple = sw.isChecked();
            return vote;
        }
    }

    class VoteController implements View.OnClickListener, AdapterView.OnItemClickListener{
        private Dialog mDialog;
        private Vote mVote;

        public VoteController(Dialog dialog, Vote vote) {
            mDialog = dialog;
            mVote = vote;
            if(vote.multiple) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Meeting.this,
                        android.R.layout.simple_list_item_multiple_choice, vote.options);

                ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
                lv.setItemsCanFocus(false);
                lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(this);
            }
            else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Meeting.this,
                        android.R.layout.simple_list_item_single_choice, vote.options);

                ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
                lv.setItemsCanFocus(false);
                lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(this);
            }
            TextView title = (TextView)dialog.findViewById(R.id.title);
            title.setText(vote.title);
            mDialog.findViewById(R.id.vote).setOnClickListener(this);
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
    }*/
}
