package arbell.demo.meeting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFPageView;
import com.artifex.mupdfdemo.MuPDFReaderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import arbell.demo.meeting.view.FingerPaintView;

/**
 * Created at 15:30 2015-08-26
 */
public class DocViewer extends Activity implements View.OnClickListener {
    public static final String FILE = "file";

    private MuPDFReaderView mSlider;

    private Drawable mDrawing, mSaving;
    private MuPDFCore mCore;
    private View mProgress;
    private SeekBar mSeekBar;
    private TextView mPageText;
    private final int MAX_RANGE = 10000;

    private AlphaAnimation mHidingAnim;
    private View mPageBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doc_viewer);
        mProgress = findViewById(R.id.progress);
        mSeekBar = (SeekBar)findViewById(R.id.seek);
        mSeekBar.setMax(MAX_RANGE);
        mPageText = (TextView)findViewById(R.id.page);

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

        mPageBar = findViewById(R.id.page_bar);
        mHidingAnim = new AlphaAnimation(1, 0);
        mHidingAnim.setDuration(500);
        mHidingAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPageBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mCore != null) {
                    int count = mCore.countPages();
                    if(count > 1) {
                        mPageText.setText(String.format("%d / %d",
                                progress * count / (MAX_RANGE + 1) + 1,
                                count));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mCore != null) {
                    int count = mCore.countPages();
                    if(count <= 1)
                        return;
                    int index = seekBar.getProgress()*count/(MAX_RANGE + 1);
                    mSlider.setDisplayedViewIndex(index);
                }
            }
        });
        String fileName = getIntent().getStringExtra(FILE);
        if(fileName != null) {
            File file = new File(getFilesDir(), fileName);
            if(!copydoc(file))
                readDoc(file);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCore != null) {
            mCore.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.draw:
                TextView tv = (TextView)v;
                if(mSlider.getMode() != MuPDFReaderView.Mode.Viewing) {
                    tv.setText("手绘注释");
                    mSlider.setMode(MuPDFReaderView.Mode.Viewing);
                    tv.setCompoundDrawables(mDrawing, null, null, null);
                    findViewById(R.id.clear).setVisibility(View.INVISIBLE);
                }
                else {
                    tv.setText("保存");
                    mSlider.setMode(MuPDFReaderView.Mode.Drawing);
                    tv.setCompoundDrawables(mSaving, null, null, null);
                    findViewById(R.id.clear).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.clear:
                MuPDFPageView page = (MuPDFPageView)mSlider.getDisplayedView();
                page.cancelDraw();
                break;
            case R.id.vote:
//                showVoteTopicDialog();
                Intent intent = new Intent(this, VoteActivity.class);
                startActivity(intent);
                break;
        }
    }

    private boolean copydoc(final File doc) {
        if(!doc.exists()) {
            mProgress.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, File>() {
                @Override
                protected File doInBackground(Void... params) {
                    AssetManager am = getAssets();
                    byte[] buf = new byte[4096];
                    try{
                        InputStream is = am.open(doc.getName());
                        FileOutputStream os = new FileOutputStream(doc);
                        for(int len = is.read(buf); len > 0; len = is.read(buf))
                            os.write(buf, 0, len);
                        is.close();
                        os.close();
                        return doc;
                    }
                    catch (IOException e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(File file) {
                    if(file != null) {
                        readDoc(file);
                    }
                    else {
                        Toast.makeText(DocViewer.this,
                                "文件复制失败", Toast.LENGTH_SHORT).show();
                    }
                    mProgress.setVisibility(View.GONE);
                }
            }.execute();
            return true;
        }
        else
            return false;
    }

    private void readDoc(File doc) {
        if(doc.getName().equals("jpg")) {
            ImageView iv = new ImageView(this);
            Bitmap bm = BitmapFactory.decodeFile(doc.getPath());
            iv.setImageBitmap(bm);
            ViewGroup vg = (ViewGroup)findViewById(R.id.container);
            vg.addView(iv, 0);
            mPageBar.setVisibility(View.GONE);
            return;
        }
        mSlider = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                MuPDFCore core = mCore;
                int count = core.countPages();
                if(count > 1)
                    mSeekBar.setProgress(i*MAX_RANGE/(count - 1));
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (mPageBar.getVisibility() != VISIBLE) {
                    showButtons();
                } else {
                    hideButtons();
                }
            }

            @Override
            protected void onDocMotion() {
                hideButtons();
            }
        };
        ViewGroup vg = (ViewGroup)findViewById(R.id.container);
        vg.addView(mSlider, 0);
        try {
            mCore = new MuPDFCore(this, doc.getPath());
            mSlider.setAdapter(new MuPDFPageAdapter(this, null, mCore));
        }
        catch (Exception e){
            Toast.makeText(this, R.string.cannot_open_document,
                    Toast.LENGTH_SHORT).show();
        }
        mProgress.setVisibility(View.GONE);
    }

    private void showButtons() {
        mPageBar.clearAnimation();
        mPageBar.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
//        mPageBar.setVisibility(View.GONE);
        mPageBar.startAnimation(mHidingAnim);
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
