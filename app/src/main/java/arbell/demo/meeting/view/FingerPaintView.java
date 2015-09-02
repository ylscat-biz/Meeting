package arbell.demo.meeting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaintView extends View {


    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private BitmapDrawable mBg;

    public FingerPaintView(Context c, Bitmap bitmap) {
        super(c);
        mBg = new BitmapDrawable(c.getResources(), bitmap);

        init();
    }

    public FingerPaintView(Context context) {
        super(context);
        init();
    }

    public FingerPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mBitmap != null)
            mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if(mBg != null) {
            int bh = mBg.getIntrinsicHeight();
            int bw = mBg.getIntrinsicWidth();
            if (w * bh > bw * h) {
                int nw = h / bh * bw;
                int left = (w - nw) / 2;
                mBg.setBounds(left, 0, left + nw, h);
            } else {
                int nh = w / bw * bh;
                int top = (h - nh) / 2;
                mBg.setBounds(0, top, w, top + nh);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBg != null)
            mBg.draw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        mBitmap.eraseColor(0);
        invalidate();
    }
}