package arbell.demo.meeting.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import arbell.demo.meeting.R;

/**
 * Created at 2015/10/27.
 *
 * @author YinLanShan
 */
public class LocalAnnotationAdapter extends BaseAdapter {
    public static final String DIR_NAME = "annotation";

    private LayoutInflater mInflater;
    private LruCache<String, BitmapDrawable> mCache = new LruCache<>(3);
    private ArrayList<File> mData = new ArrayList<>();
    private File mDir;

    public LocalAnnotationAdapter(LayoutInflater inflater) {
        mInflater = inflater;
        File cache = mInflater.getContext().getExternalCacheDir();
        mDir = new File(cache, DIR_NAME);
    }

    public void refresh() {
        mData.clear();
        if(mDir.exists()) {
            File[] files = mDir.listFiles();
            Collections.addAll(mData, files);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
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
        File file = mData.get(position);
        String name = file.getName();
        TextView tv = (TextView) convertView;
        tv.setText(name);
        BitmapDrawable bd = mCache.get(name);
        if (bd != null) {
            tv.setCompoundDrawables(null, null, null, bd);
        } else {
            bd = new BitmapDrawable(
                    mInflater.getContext().getResources(),
                    file.getAbsolutePath());
            bd.setBounds(0, 0,
                    bd.getIntrinsicWidth(),
                    bd.getIntrinsicHeight());
            mCache.put(name, bd);
            tv.setCompoundDrawables(null, null, null, bd);
        }
        return convertView;
    }

    public static void saveInLocal(Context c, Bitmap b, String name) {
        File dir = new File(c.getExternalCacheDir(), DIR_NAME);
        if(!dir.exists())
            dir.mkdir();
        File file = new File(dir, name);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
        }
        catch (IOException e) {
            Log.e("SaveLocal", "Save local annotation failed", e);
        }
    }
}
