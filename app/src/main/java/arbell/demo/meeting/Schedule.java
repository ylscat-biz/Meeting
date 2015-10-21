package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

public class Schedule extends Activity implements View.OnClickListener {
    private View mSelected;
    private ListView mListView;

    private HashMap<View, Adapter> mAdapters = new HashMap<>();

    private Launcher mLauncher = new Launcher();

    class Launcher implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int pos = mListView.getPositionForView(v);
            if(pos == ListView.INVALID_POSITION)
                return;
            JSONObject meeting = (JSONObject)mListView.getAdapter().getItem(pos);
            if(meeting != null) {
                launch(meeting);
            }
        }

        public void launch(JSONObject meeting) {
            Intent intent = new Intent(Schedule.this, Meeting.class);
            intent.putExtra(Meeting.TITLE, meeting.optString("name"));
            intent.putExtra(Meeting.ID, meeting.optString("id"));
            intent.putExtra(Meeting.TIME, meeting.optString("open_time"));
            intent.putExtra(Meeting.ADDRESS, meeting.optString("address"));
            intent.putExtra(Meeting.HOST, meeting.optString("teamer"));
            intent.putExtra(Meeting.TOPIC, meeting.optString("meeting_topic"));
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);
        mListView = (ListView)findViewById(R.id.list);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Adapter holding = new Adapter();
        final Adapter future = new Adapter();
        final Adapter closed = new Adapter();
        Request request = new Request(Request.Method.POST,
                HttpHelper.URL_BASE + "getMeeting",
                "memberid=" + Login.sMemberID,
                new Response.Listener<JSONObject>() {
                    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray meetings = response.optJSONArray("data");
                        if(meetings == null || meetings.length() == 0)
                            return;
                        Date now = Calendar.getInstance().getTime();
                        for(int i = 0; i < meetings.length(); i++) {
                            JSONObject m = meetings.optJSONObject(i);
                            if("有效".equals(m.optString("meeting_status"))) {
                                String time = m.optString("open_time");
                                if(compare(time, now) <= 0)
                                    holding.mMeetings.add(m);
                                else
                                    future.mMeetings.add(m);
                            }
                            else {
                                closed.mMeetings.add(m);
                            }
                        }

                        View tab = findViewById(R.id.holding);
                        int count = holding.getCount();
                        TextView tv = (TextView)((ViewGroup)tab).getChildAt(1);
                        tv.setText(String.valueOf(count));
                        if(count > 0) {
                            holding.notifyDataSetChanged();
                            if(count == 1)
                                mLauncher.launch(holding.mMeetings.get(0));
                        }

                        tab = findViewById(R.id.future);
                        count = future.getCount();
                        tv = (TextView)((ViewGroup)tab).getChildAt(1);
                        tv.setText(String.valueOf(count));
                        if(count > 0)
                            future.notifyDataSetChanged();

                        tab = findViewById(R.id.closed);
                        count = closed.getCount();
                        tv = (TextView)((ViewGroup)tab).getChildAt(1);
                        tv.setText(String.valueOf(count));
                        if(count > 0)
                            closed.notifyDataSetChanged();
                    }

                    private int compare(String time, Date now) {
                        try {
                            Date d =format.parse(time);
                            return d.compareTo(now);
                        } catch (ParseException e) {
                            return 1;
                        }
                    }
                });
        HttpHelper.sRequestQueue.add(request);


        View tab = findViewById(R.id.holding);
        tab.setOnClickListener(this);
        mAdapters.put(tab, holding);

        tab = findViewById(R.id.future);
        tab.setOnClickListener(this);
        mAdapters.put(tab, future);

        tab = findViewById(R.id.closed);
        tab.setOnClickListener(this);
        mAdapters.put(tab, closed);

        tab.setVisibility(View.GONE);
        onClick(findViewById(R.id.holding));
    }

    @Override
    public void onClick(View v) {
        if(v != mSelected) {
            if(mSelected != null) {
                mSelected.setSelected(false);
            }
            v.setSelected(true);
            mSelected = v;
            mListView.setAdapter(mAdapters.get(v));
        }
    }

    class Adapter extends BaseAdapter {
        private LayoutInflater mInflater = getLayoutInflater();
        private ArrayList<JSONObject> mMeetings = new ArrayList<>();

        @Override
        public int getCount() {
            return mMeetings.size();
        }

        @Override
        public Object getItem(int position) {
            return mMeetings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.meeting_item, parent, false);
                convertView.findViewById(R.id.enter).setOnClickListener(mLauncher);
            }
            JSONObject json = mMeetings.get(position);
            TextView tv = (TextView)convertView.findViewById(R.id.title);
            tv.setText(json.optString("name"));
            tv = (TextView)convertView.findViewById(R.id.host);
            tv.setText(json.optString("teamer"));
            tv = (TextView)convertView.findViewById(R.id.address);
            tv.setText(json.optString("address"));
            tv = (TextView)convertView.findViewById(R.id.begin_time);
            tv.setText(json.optString("open_time"));

            return convertView;
        }
    }
}
