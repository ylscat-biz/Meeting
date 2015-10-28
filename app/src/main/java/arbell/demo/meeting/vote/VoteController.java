package arbell.demo.meeting.vote;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import arbell.demo.meeting.Login;
import arbell.demo.meeting.Meeting;
import arbell.demo.meeting.R;
import arbell.demo.meeting.doc.DownloadTask;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;
import arbell.demo.meeting.view.FingerPaintView;

/**
 * Created at 03:18 2015-09-03
 */
public class VoteController implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Dialog mDialog;
    private JSONObject mVote;
    private Bitmap mSign;
    FingerPaintView mSignView;

    public VoteController(Dialog dialog, JSONObject vote) {
        mDialog = dialog;
        mVote = vote;
        ArrayList<String> options = new ArrayList<>();
        JSONArray array = vote.optJSONArray("itemlist");
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            options.add(item.optString("title"));
        }

//            if(vote.multiple) {
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mDialog.getContext(),
//                        android.R.layout.simple_list_item_multiple_choice, vote.options);
//
//                ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
//                lv.setItemsCanFocus(false);
//                lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//                lv.setAdapter(adapter);
//                lv.setOnItemClickListener(this);
//            }
//            else {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mDialog.getContext(),
                android.R.layout.simple_list_item_single_choice, options);

        ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
        lv.setItemsCanFocus(false);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
//            }
        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(vote.optString("title"));
        dialog.findViewById(R.id.back).setOnClickListener(this);
        dialog.findViewById(R.id.vote).setOnClickListener(this);
        dialog.findViewById(R.id.clear).setOnClickListener(this);

        mSignView = (FingerPaintView) mDialog.findViewById(R.id.sign);
        setupSign();
    }

    private void setupSign() {
        String path = Login.sSign;
        if(path == null)
            return;
        File file = checkFile(path);
        if(file.exists()) {
            mSign =  BitmapFactory.decodeFile(file.getAbsolutePath());
            FingerPaintView fpv = (FingerPaintView) mDialog.findViewById(R.id.sign);
            fpv.set(mSign);
        }
        else {
            String server = HttpHelper.getServer(mDialog.getContext());
            String url;
            if(path.startsWith("/")) {
                url = server + path;
            }
            else {
                url = server + "/" + path;
            }
            new DownloadTask(new DownloadTask.DownloadListener() {
                @Override
                public void begin(int totalSize) {}

                @Override
                public void update(int progress, int total) {}

                @Override
                public void complete(File file) {
                    mSign =  BitmapFactory.decodeFile(file.getAbsolutePath());
                    FingerPaintView fpv = (FingerPaintView) mDialog.findViewById(R.id.sign);
                    fpv.set(mSign);
                }
            }).execute(url, file.getAbsolutePath());
        }
    }

    private File checkFile(String path) {
        String name = path;
        int index = path.lastIndexOf('/');
        if(index != -1)
            name = path.substring(index + 1);
        Context context = mDialog.getContext();
        File cache = context.getExternalCacheDir();
        return new File(cache, name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                mDialog.dismiss();
                onVote();
                break;
            case R.id.vote:
                vote();
                break;
            case R.id.clear:
                TextView tv = (TextView)v;
                if(mSignView.isInTouchMode()) {
                    tv.setText("重置签名");
                    mSignView.setInTouchMode(false);
                }
                else {
                    tv.setText("保存签名");
                    mSignView.setInTouchMode(true);
                    mSignView.clear();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            ListView lv = (ListView)parent;
//            SparseBooleanArray sba = lv.getCheckedItemPositions();
//            StringBuilder sb = new StringBuilder();
//            for(int i = 0; i < sba.size(); i++) {
//                if(sba.valueAt(i))
//                    sb.append(mVote.options.get(sba.keyAt(i))).append(" ");
//            }
//            TextView tv = (TextView)mDialog.findViewById(R.id.result);
//            tv.setText(sb.toString());
    }

    private void vote() {
        ListView lv = (ListView) mDialog.findViewById(R.id.vote_list);
        SparseBooleanArray sba = lv.getCheckedItemPositions();
        int checkPosition = -1;
        for(int i = 0; i < sba.size(); i++) {
            if (sba.valueAt(i)) {
                checkPosition = sba.keyAt(i);
                break;
            }
        }

        if(checkPosition == -1) {
            Toast.makeText(mDialog.getContext(), "没有选择投票项",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject checked = mVote.optJSONArray("itemlist").optJSONObject(checkPosition);
        StringBuilder sb = new StringBuilder();
        sb.append("meetingid=").append(Meeting.sMeetingID);
        sb.append("&memberid=").append(Login.sMemberID);
        sb.append("&voteid=").append(mVote.optString("id"));
        sb.append("&itemid=").append(checked.optString("id"));

        Request request = new Request(Request.Method.POST,
                HttpHelper.URL_BASE + "vote",
                sb.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.optBoolean("success")) {
                            Toast.makeText(mDialog.getContext(),
                                    response.optString("msg"),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                            onVote();
                    }
                });
        HttpHelper.sRequestQueue.add(request);
        mDialog.dismiss();
    }

    public void onVote() {

    }
}
