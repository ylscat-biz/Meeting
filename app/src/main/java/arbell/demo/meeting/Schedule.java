package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by BYLS on 2015/8/6.
 */
public class Schedule extends Activity implements View.OnClickListener {
    private View mSelected;
    private ListView mListView;

    private HashMap<View, SimpleAdapter> mAdapters = new HashMap<View, SimpleAdapter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);
        mListView = (ListView)findViewById(R.id.list);
        final View.OnClickListener launcher = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                Intent intent = new Intent(Schedule.this, Meeting.class);
                if(tag != null)
                    intent.putExtra(Meeting.TITLE, tag.toString());
                startActivity(intent);
            }
        };

        final String TITLE = "title", HOST = "host", ADDRESS = "address",
                BEGIN = "begin", END= "end", BUTTON = "button";
        String[] from = new String[]{TITLE, HOST, ADDRESS, BEGIN, END, BUTTON};
        int[] to = new int[]{R.id.title, R.id.host, R.id.address, R.id.begin_time,
                R.id.end_time, R.id.enter};
        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view.getId() == R.id.enter) {
                    view.setTag(textRepresentation);
                    view.setOnClickListener(launcher);
                    return true;
                }
                return false;
            }
        };

        ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Calendar c = Calendar.getInstance();

        for(int i = 0; i < 3; i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            int index = i + 1;
            item.put(TITLE, "会议" + index);
            item.put(HOST, "主持人" + (i+1));
            item.put(ADDRESS, "会议" + (i + 1));
            c.set(Calendar.DAY_OF_MONTH, index);
            item.put(BEGIN, String.format("%1$tF 9:00", c));
            item.put(END, String.format("%1$tF 15:00", c));
            item.put(BUTTON, item.get(TITLE));
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.meeting_item,
                from, to);
        adapter.setViewBinder(binder);
        View tab = findViewById(R.id.holding);
        tab.setOnClickListener(this);
        TextView tv = (TextView)((ViewGroup)tab).getChildAt(1);
        tv.setText(String.valueOf(data.size()));
        mAdapters.put(tab, adapter);

        data = new ArrayList<Map<String, String>>();
        for(int i = 3; i < 9; i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            int index = i + 1;
            item.put(TITLE, "会议" + index);
            item.put(HOST, "主持人" + (i+1));
            item.put(ADDRESS, "会议" + (i + 1));
            c.set(Calendar.DAY_OF_MONTH, index);
            item.put(BEGIN, String.format("%1$tF 9:00", c));
            item.put(END, String.format("%1$tF 15:00", c));
            item.put(BUTTON, item.get(TITLE));
            data.add(item);
        }
        adapter = new SimpleAdapter(this, data, R.layout.meeting_item,
                from, to);
        adapter.setViewBinder(binder);
        tab = findViewById(R.id.future);
        tab.setOnClickListener(this);
        tv = (TextView)((ViewGroup)tab).getChildAt(1);
        tv.setText(String.valueOf(data.size()));
        mAdapters.put(tab, adapter);

        data = new ArrayList<Map<String, String>>();

        c.add(Calendar.MONTH, -1);
        for(int i = 0; i < 20; i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            int index = i + 1;
            item.put(TITLE, "会议" + index);
            item.put(HOST, "主持人" + (i+1));
            item.put(ADDRESS, "会议" + (i + 1));
            c.set(Calendar.DAY_OF_MONTH, index);
            item.put(BEGIN, String.format("%1$tF 9:00", c));
            item.put(END, String.format("%1$tF 15:00", c));
            data.add(item);
        }
        adapter = new SimpleAdapter(this, data, R.layout.meeting_item,
                from, to);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view.getId() == R.id.enter) {
                    view.setEnabled(false);
                    return true;
                }
                return false;
            }
        });
        tab = findViewById(R.id.closed);
        tab.setOnClickListener(this);
        tv = (TextView)((ViewGroup)tab).getChildAt(1);
        tv.setText(String.valueOf(data.size()));
        mAdapters.put(tab, adapter);

        findViewById(R.id.closed).setOnClickListener(this);
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
}
