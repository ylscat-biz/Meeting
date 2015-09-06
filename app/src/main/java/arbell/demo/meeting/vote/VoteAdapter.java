package arbell.demo.meeting.vote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class VoteAdapter extends BaseAdapter {
    public ArrayList<Vote> mVotes = new ArrayList<Vote>();
    private LayoutInflater mInflater;

    public VoteAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }


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
        if (convertView == null) {
            convertView = mInflater.inflate(
                    android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tv = (TextView) convertView;
        tv.setText((position + 1) + "   " + mVotes.get(position).title);
        return convertView;
    }
}

