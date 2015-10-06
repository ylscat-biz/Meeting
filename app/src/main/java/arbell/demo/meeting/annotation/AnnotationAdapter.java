package arbell.demo.meeting.annotation;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

public class AnnotationAdapter extends BaseAdapter implements
        Response.Listener<JSONObject> {
    private LayoutInflater mInflater;
    private JSONArray mData;
    private LruCache<String, BitmapDrawable> mCache = new LruCache<>(3);
    private Request mRequest = new Request(Request.Method.GET,
            HttpHelper.URL_BASE + "get_record?id=" + Meeting.sMeetingID,
            null, this);

    public AnnotationAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void refresh() {
        HttpHelper.sRequestQueue.add(mRequest);
    }

    @Override
    public void onResponse(JSONObject response) {
        mData = response.optJSONArray("data");
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mData == null)
            return 0;
        else
            return mData.length();
    }

    @Override
    public Object getItem(int position) {
        return mData.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.annotation_item,
                    parent, false);
        }
        JSONObject json = mData.optJSONObject(position);
        String name = json.optString("member_name");
        String time = json.optString("add_time");
        final String id = json.optString("id");
        TextView tv = (TextView) convertView;
        tv.setText(name + " " + time);
        BitmapDrawable bd = mCache.get(id);
        if (bd != null) {
            tv.setCompoundDrawables(null, null, null, bd);
        } else {
            String url = json.optString("file_url");
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            BitmapDrawable d = new BitmapDrawable(
                                    mInflater.getContext().getResources(),
                                    response);
                            d.setBounds(0, 0, response.getWidth(),
                                    response.getHeight());
                            mCache.put(id, d);
                            notifyDataSetChanged();
                        }
                    }, -1, -1, Bitmap.Config.RGB_565, null);
            HttpHelper.sRequestQueue.add(request);
        }
        return convertView;
    }
}
