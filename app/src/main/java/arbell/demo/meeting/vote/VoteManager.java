package arbell.demo.meeting.vote;

import android.view.LayoutInflater;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

/**
 * 2015-09-22 10:08
 */
public class VoteManager {
    public static VoteManager sInstance;

    private LayoutInflater mInflater;

    public VoteManager(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public VoteAdapter getVotes(VoteAdapter adapter, String topicId, String subjectId) {
        if(adapter == null)
            adapter = new VoteAdapter(mInflater);
        String url = HttpHelper.URL_BASE + "getVoteList?meetingid=" + Meeting.sMeetingID;
        if(topicId != null)
            url += "&topicid="+topicId;
        if(subjectId != null)
            url += "&itemid="+subjectId;
        final VoteAdapter voteAdapter = adapter;
        Request request = new Request(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray array = response.optJSONArray("data");
                voteAdapter.setVotes(array);
                voteAdapter.notifyDataSetChanged();
            }
        });
        HttpHelper.sRequestQueue.add(request);

        return adapter;
    }
}
