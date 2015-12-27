package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFPageView;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.PageView;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

import arbell.demo.meeting.annotation.LocalAnnotationAdapter;
import arbell.demo.meeting.doc.ExcelReader;
import arbell.demo.meeting.doc.ExcelTabController;
import arbell.demo.meeting.doc.GetDocUrl;
import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.UploadRequest;
import arbell.demo.meeting.preach.Preach;
import arbell.demo.meeting.preach.PreachControllerL2;
import arbell.demo.meeting.view.ExcelView;

import static arbell.demo.meeting.Meeting.sPreach;

/**
 * Created at 15:30 2015-08-26
 */
public class DocViewer extends Activity implements View.OnClickListener {
    public static final String FILE = "file";
    public static final String FILE_ID = "fileId";
    public static final String TOPIC_ID = "topic";
    public static final String SUBJECT_ID = "subject";
    public static final String TOPIC_INDEX = "topicIndex";

    private MuPDFReaderView mSlider;

    private Drawable mDrawing, mSaving;
    private MuPDFCore mCore;
    private View mProgress;
    private SeekBar mSeekBar;
    private TextView mPageText;
    private final int MAX_RANGE = 10000;

    private AlphaAnimation mHidingAnim;
    private View mPageBar;

    public String topicId, subjectId, fileId, topicIndex = "0";

    private VideoView mVideoView;

    public static PreachControllerL2 sPreachController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        topicId = intent.getStringExtra(TOPIC_ID);
        subjectId = intent.getStringExtra(SUBJECT_ID);
        fileId = intent.getStringExtra(FILE_ID);
        String filePath = intent.getStringExtra(FILE);
        topicIndex = intent.getStringExtra(TOPIC_INDEX);

        setContentView(R.layout.doc_viewer);
        sPreachController = new PreachControllerL2(this);
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
                        if(sPreach.getMode() == Preach.PREACH)
                            uploadCurrent();
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

        if(filePath != null) {
            File file = new File(filePath);
//            if(!copydoc(file))
                readDoc(file);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(mVideoView != null)
//            mVideoView.pause();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        sPreach.setListener(sPreachController);
        sPreachController.onUpdate(sPreach.getMsg());
        sPreach.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sPreach.isScanningCache()) {
            sPreach.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCore != null) {
            mCore.onDestroy();
        }
        if(sPreach.getMode() == Preach.PREACH) {
            String msg = String.format("1 %s", topicIndex);
            sPreach.upload(msg);
        }
        sPreachController = null;
    }

    @Override
    public void onBackPressed() {
        if(sPreach.getMode() == Preach.FOLLOW) {
            Toast.makeText(this, "跟随中，无法操作",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(sPreach.getMode() == Preach.FOLLOW) {
            Toast.makeText(this, "跟随中，无法操作",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(Meeting.isGuest && v.getId() != R.id.back) {
            Toast.makeText(this, "列席人员，不能投票，不能手绘注释!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.draw:
                TextView tv = (TextView)v;
                if(mSlider.getMode() != MuPDFReaderView.Mode.Viewing) {
                    tv.setText("手绘注释");
                    tv.setCompoundDrawables(mDrawing, null, null, null);
                    findViewById(R.id.clear).setVisibility(View.INVISIBLE);
                    PageView pv = (PageView)mSlider.getDisplayedView();
                    if(pv.getDrawing() != null)
                        save((MuPDFPageView) pv);
                    else
                        mSlider.setMode(MuPDFReaderView.Mode.Viewing);
                } else {
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
                if(topicId != null)
                    intent.putExtra(TOPIC_ID, topicId);
                if(subjectId != null)
                    intent.putExtra(SUBJECT_ID, subjectId);
                startActivity(intent);
                break;
        }
    }

    private void save(final MuPDFPageView view) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Bitmap bitmap = Bitmap.createBitmap(
                        view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
                view.draw(new Canvas(bitmap));
                publishProgress();
                if(Meeting.sPreach.getMode() == Preach.PREACH) {
                    LinkedHashMap<String, String> p = new LinkedHashMap<>();
                    p.put("id", Meeting.sMeetingID);
                    p.put("memberid", Login.sMemberID);
                    p.put("membername", Login.sMemberName);
                    UploadRequest req = new UploadRequest(HttpHelper.URL_BASE + "save_record",
                            p, bitmap);
                    return req.upload();
                }
                else {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String name = format.format(Calendar.getInstance().getTime());
                    LocalAnnotationAdapter.saveInLocal(DocViewer.this, bitmap, name);
                    return "OK";
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                view.saveDraw();
                mSlider.setMode(MuPDFReaderView.Mode.Viewing);
            }

            @Override
            protected void onPostExecute(String resp) {
                if(resp == null)
                    Log.e("Meeting", "分享注释失败");

            }
        }.execute();
    }

    /*private boolean copydoc(final File doc) {
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
    }*/

    private void readDoc(File doc) {
        String name = doc.getName();
        String suffix = GetDocUrl.getSuffix(name);
        if("jpg".equals(suffix) || "jpeg".equals(suffix) || "png".equals(suffix)) {
            ImageView iv = new ImageView(this);
            Bitmap bm = BitmapFactory.decodeFile(doc.getPath());
            iv.setImageBitmap(bm);
            ViewGroup vg = (ViewGroup)findViewById(R.id.container);
            vg.addView(iv, 0);
            mPageBar.setVisibility(View.GONE);
            findViewById(R.id.draw).setVisibility(View.INVISIBLE);
            return;
        }
        if("pdf".equals(suffix)) {
            mSlider = new MuPDFReaderView(this) {
                @Override
                protected void onMoveToChild(int i) {
                    MuPDFCore core = mCore;
                    int count = core.countPages();
                    if (count > 1)
                        mSeekBar.setProgress(i * MAX_RANGE / (count - 1));
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
            ViewGroup vg = (ViewGroup) findViewById(R.id.container);
            vg.addView(mSlider, 0);
            try {
                mCore = new MuPDFCore(this, doc.getPath());
                mSlider.setAdapter(new MuPDFPageAdapter(this, null, mCore));
            } catch (Exception e) {
                Toast.makeText(this, R.string.cannot_open_document,
                        Toast.LENGTH_SHORT).show();
            }
            mProgress.setVisibility(View.GONE);
        }
        else if("xls".equals(suffix) || "xlsx".equals(suffix)) {
            ViewGroup vg = (ViewGroup)findViewById(R.id.container);
            final View excelGroup = getLayoutInflater().inflate(R.layout.excel_view, vg, false);
            final ExcelView excel = (ExcelView)excelGroup.findViewById(R.id.excel);
            vg.addView(excelGroup, 0);
            final ExcelView.GestureController controller = new ExcelView.GestureController(excel);
            excel.setOnTouchListener(controller);
            mPageBar.setVisibility(View.GONE);
            findViewById(R.id.draw).setVisibility(View.INVISIBLE);



            ExcelReader reader = new ExcelReader() {
                @Override
                protected void onPreExecute() {
                    mProgress.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(Workbook workbook) {
                    mProgress.setVisibility(View.GONE);
                    if(workbook == null) {
                        Toast.makeText(DocViewer.this, R.string.cannot_open_document,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    float density = getResources().getDisplayMetrics().density;
                    LinearLayout ll = (LinearLayout)excelGroup.findViewById(R.id.sheet_panel);
                    ExcelTabController tabController = new ExcelTabController(excel);
                    View firstTab = null;
                    for(int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        Sheet sheet = workbook.getSheetAt(i);
                        ExcelView.SheetData data = new ExcelView.SheetData(
                                sheet, density);
                        TextView tab = new TextView(DocViewer.this);
                        ll.addView(tab);
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                                tab.getLayoutParams();
                        lp.setMargins(0, 0, (int)(3*density), 0);
                        if(i == 0)
                            firstTab = tab;
                        tab.setText(sheet.getSheetName());
                        tabController.addTab(tab, data);
                        tab.setOnClickListener(tabController);
                    }
                    tabController.onClick(firstTab);
                }
            };
            reader.execute(doc);
        }
        else if("mp4".equals(suffix) || "mkv".equals(suffix)) {
            mVideoView = new VideoView(this);
            String url;
            try {
                BufferedReader br = new BufferedReader(new FileReader(doc));
                url = br.readLine();
                br.close();
            } catch (IOException e) {
                Log.e("DocViewer", "Read doc fail", e);
                return;
            }
            //Video file
            Uri uri = Uri.parse(url);

            //Create media controller
            MediaController controller = new MediaController(this);
            mVideoView.setMediaController(controller);
            mVideoView.setVideoURI(uri);
            mVideoView.start();

            ViewGroup vg = (ViewGroup)findViewById(R.id.container);
            vg.addView(mVideoView, 0);
            mPageBar.setVisibility(View.GONE);
            findViewById(R.id.draw).setVisibility(View.INVISIBLE);
        }
    }

    private void showButtons() {
        mPageBar.clearAnimation();
        mPageBar.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
//        mPageBar.setVisibility(View.GONE);
        mPageBar.startAnimation(mHidingAnim);
    }

    public void uploadCurrent() {
        sPreach.upload(getCurrentStatusString());
    }

    public String getCurrentStatusString() {
        String msg = String.format("1 %s\n%s", topicIndex, fileId);
        if(mSlider != null) {
            msg += "#"+mSlider.getDisplayedViewIndex();
        }
        return msg;
    }

    public void moveToPage(int page) {
        if(mSlider == null)
            return;
        int index = mSlider.getDisplayedViewIndex();
        if(page == index - 1)
            mSlider.moveToPrevious();
        else if(page == index + 1)
            mSlider.moveToNext();
        else
            mSlider.setDisplayedViewIndex(page);
    }

    public void setEnable(boolean enable) {
        if(mSlider == null)
            return;
        mSlider.setEnabled(enable);
        mSlider.setClickable(enable);
    }
}
