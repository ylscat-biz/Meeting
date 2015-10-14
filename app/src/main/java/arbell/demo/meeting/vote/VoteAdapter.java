package arbell.demo.meeting.vote;

import android.graphics.drawable.ClipDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import arbell.demo.meeting.R;

public class VoteAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private JSONArray mVotes;
    private LinkedHashMap<ClipDrawable, Integer> mVoteCount = new LinkedHashMap<>();
    private ArrayList<JSONObject> mTempList = new ArrayList<>();

    public VoteAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void setVotes(JSONArray array) {
        mVotes = array;
    }

    @Override
    public int getCount() {
        return mVotes == null ? 0 : mVotes.length();
    }

    @Override
    public Object getItem(int position) {
        if(mVotes == null)
            return null;
        return mVotes.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.vote_list_item, parent, false);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.seq);
        tv.setText(String.valueOf(position + 1));
        tv = (TextView) convertView.findViewById(R.id.title);
        JSONObject json = mVotes.optJSONObject(position);
        tv.setText(json.optString("title"));
        ArrayList<JSONObject> options = sortOptions(json.optJSONArray("itemlist"));
        LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.vote_option);
        LinkedHashMap<ClipDrawable, Integer> voteCount = mVoteCount;
        int total = 0, i = 0;
        for(JSONObject option : options) {
            int count = ll.getChildCount();
            View item;
            if(i == count) {
                item = mInflater.inflate(R.layout.vote_option, ll, false);
                ll.addView(item);
            }
            else {
                item = ll.getChildAt(i);
                if(item.getVisibility() != View.VISIBLE)
                    item.setVisibility(View.VISIBLE);
            }

            tv = (TextView)item.findViewById(R.id.name);
            tv.setText(option.optString("title"));
            tv = (TextView)item.findViewById(R.id.count);
            int voteNum = option.optInt("vote_num");
            total += voteNum;
            tv.setText(String.valueOf(voteNum));
            ClipDrawable d = (ClipDrawable)item.findViewById(R.id.progress).getBackground();
            voteCount.put(d, voteNum);
            i++;
        }

        int count = ll.getChildCount();
        for(i = options.size(); i < count; i++) {
            ll.getChildAt(i).setVisibility(View.GONE);
        }
        options.clear();

        for(ClipDrawable d : voteCount.keySet()) {
            if(total == 0)
                d.setLevel(0);
            else {
                int num = voteCount.get(d);
                d.setLevel(num*10000/total);
            }
        }
        voteCount.clear();
        convertView.setBackgroundColor((position&1) == 0 ? 0 : 0x33000000);
        return convertView;
    }

    private ArrayList<JSONObject> sortOptions(JSONArray array) {
        ArrayList<JSONObject> list = mTempList;
        for(int i = 0; i < array.length(); i++) {
            list.add(array.optJSONObject(i));
        }

        Collections.sort(list, sOptionCompare);
        return list;
    }

    private static Comparator<JSONObject> sOptionCompare = new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject j1, JSONObject j2) {
            String t1 = j1.optString("title");
            String t2 = j2.optString("title");
            return t1.compareTo(t2);
        }
    };
}

