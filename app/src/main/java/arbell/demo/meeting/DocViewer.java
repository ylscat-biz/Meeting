package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import arbell.demo.meeting.view.FingerPaintView;
import arbell.demo.meeting.view.LoopingView;

/**
 * Created at 15:30 2015-08-26
 */
public class DocViewer extends Activity implements View.OnClickListener {
    private LoopingView mSlider;

    private Drawable mDrawing, mSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doc_viewer);
        ViewGroup vg = (ViewGroup)findViewById(R.id.slide);
        mSlider = (LoopingView)vg;
        AssetManager am = getAssets();
        try {
            for (int i = 1; i <= 6; i++) {
                InputStream is = am.open(String.format("%d.jpg", i));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
//                ImageView iv = new ImageView(this);
//                iv.setImageBitmap(bitmap);
                FingerPaintView fpv = new FingerPaintView(this, bitmap);
                fpv.setClickable(false);
                vg.addView(fpv);
            }
        }catch (IOException e) {
            //ignore
        }

        mDrawing = getResources().getDrawable(R.drawable.view_draw);
        mSaving = getResources().getDrawable(R.drawable.view_save);
        mDrawing.setBounds(0, 0, mDrawing.getIntrinsicWidth(), mDrawing.getIntrinsicHeight());
        mSaving.setBounds(0, 0, mSaving.getIntrinsicWidth(), mSaving.getIntrinsicHeight());

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.draw).setOnClickListener(this);
        View clear = findViewById(R.id.clear);
        clear.setOnClickListener(this);
        clear.setVisibility(View.INVISIBLE);
        findViewById(R.id.vote).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.draw:
                TextView tv = (TextView)v;
                if(mSlider.isLock) {
                    tv.setText("手绘注释");
                    mSlider.isLock = false;
                    tv.setCompoundDrawables(mDrawing, null, null, null);
                    findViewById(R.id.clear).setVisibility(View.INVISIBLE);
                }
                else {
                    tv.setText("保存");
                    mSlider.isLock = true;
                    tv.setCompoundDrawables(mSaving, null, null, null);
                    findViewById(R.id.clear).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.clear:
                float sx = mSlider.getScrollX();
                int w = mSlider.getWidth();
                int index = (int)(sx/w + 0.5f);
                FingerPaintView fpv = (FingerPaintView)mSlider.getChildAt(index);
                fpv.clear();
                break;
            case R.id.vote:
                showVoteTopicDialog();
                break;
        }
    }

    private void showVoteTopicDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.topic_input);
        dialog.setTitle("投票主题");
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ok:
                        EditText input = (EditText)dialog.findViewById(R.id.input);
                        String topic = input.getText().toString();
                        if(topic.length() == 0) {
                            Toast.makeText(DocViewer.this, "主题不能为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dialog.dismiss();
                        final Dialog d = new Dialog(dialog.getContext());
                        d.setTitle("投票");
                        d.setContentView(R.layout.vote);
                        TextView title = (TextView)d.findViewById(R.id.title);
                        title.setText(topic);
                        d.findViewById(R.id.vote).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                d.dismiss();
                            }
                        });
                        d.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FingerPaintView sign = (FingerPaintView)d.findViewById(R.id.sign);
                                sign.clear();
                            }
                        });

                        d.show();
                        break;
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                }
            }
        };

        dialog.findViewById(R.id.ok).setOnClickListener(listener);
        dialog.findViewById(R.id.cancel).setOnClickListener(listener);
        dialog.show();
    }
}
