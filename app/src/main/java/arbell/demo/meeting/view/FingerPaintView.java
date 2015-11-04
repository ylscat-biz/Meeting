package arbell.demo.meeting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaintView extends View {


    private Bitmap mBitmap, mPreset;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private boolean inTouchMode;

    public FingerPaintView(Context context) {
        super(context);
        init(context);
    }

    public FingerPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                context.getResources().getDisplayMetrics()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mBitmap != null)
            mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if(w > 0 && h > 0 && mPreset != null) {
            apply(mPreset);
            mPreset = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
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
        if(!inTouchMode)
            return false;
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

    public void set(Bitmap b) {
        if(mBitmap == null) {
            mPreset = b;
        }
        else {
            mBitmap.eraseColor(0);
            apply(b);
            invalidate();
        }
    }

    private void apply(Bitmap b) {
        if(b == null)
            return;
        int w = b.getWidth(), h = b.getHeight();
        int bw = mBitmap.getWidth(), bh = mBitmap.getHeight();
        Rect rect = new Rect(0, 0, bw, bh);
        if(w < bw) {
            rect.left = (bw - w)/2;
            rect.right = rect.left + w;
        }

        if(h < bh) {
            rect.top = (bh - h)/2;
            rect.bottom = rect.top + w;
        }

        mCanvas.drawBitmap(b, null, rect, null);
    }

    public void setInTouchMode(boolean mode) {
        inTouchMode = mode;
    }

    public boolean isInTouchMode() {
        return inTouchMode;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}