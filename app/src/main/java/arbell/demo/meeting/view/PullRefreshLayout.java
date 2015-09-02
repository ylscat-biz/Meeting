package arbell.demo.meeting.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import arbell.demo.meeting.R;

/**
 * Created on 2015/7/11.
 */
public class PullRefreshLayout extends ViewGroup {
    private static final float DRAG_RATE = .5f;

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_SHOW_HEADER = 1;
    public static final int STATUS_SHOW_FOOTER = 2;

    private View mHeader, mFooter;
    private View mTarget;
    private TextView mHeaderLabel;
    private ImageView mHeaderSymbol, mHeaderIcon;

    private String mLabel;
    private Drawable mIcon;
    private boolean mRotating;

    private boolean mIsBeingDragged;
    private float mInitialDownY, mLastMotionY;
    private int mTouchSlop;
    private Scroller mScroller;

    private int mStatus = STATUS_NORMAL;
    private boolean mInRefreshingRegion;

    private PullListener mListener;
    private ObjectAnimator mRotateIconAnim;

    public PullRefreshLayout(Context context) {
        this(context, null);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHeader = createLoadingLabel(context);
        mFooter = createLoadingLabel(context);
        addView(mHeader);
        addView(mFooter);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context, new DecelerateInterpolator(2f));

        mHeaderLabel = (TextView)mHeader.findViewById(android.R.id.text1);
        mHeaderSymbol = (ImageView)mHeader.findViewById(android.R.id.icon);
        mHeaderIcon = (ImageView)mHeader.findViewById(android.R.id.icon1);

        setupRotationAnim();
    }

    private View createLoadingLabel(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);
        int padding = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5, getResources().getDisplayMetrics());
        frameLayout.setPadding(0, padding*3, 0, 0);

        int[] attrs = new int[] { android.R.style.TextAppearance};
        TypedArray a = context.obtainStyledAttributes(attrs);
        int appearance = a.getResourceId(0,
                android.R.style.TextAppearance_DeviceDefault);
        a.recycle();
        TextView tv = new TextView(context);
        tv.setTextAppearance(context, appearance);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setSingleLine(true);
        tv.setId(android.R.id.text1);
        tv.setText("Blablabla");

        frameLayout.addView(tv, new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));
        ImageView iv = new ImageView(context);
        iv.setImageResource(R.drawable.refresh);
        iv.setId(android.R.id.icon);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL);
        lp.leftMargin = padding*6;
        frameLayout.addView(iv, lp);
        iv = new ImageView(context);
        iv.setId(android.R.id.icon1);
        iv.setVisibility(GONE);
        frameLayout.addView(iv, lp);
        return frameLayout;
    }

    private void setupRotationAnim() {
        mRotateIconAnim = ObjectAnimator.ofFloat(mHeaderSymbol, "rotation", 0, 359);
        mRotateIconAnim.setInterpolator(new LinearInterpolator());
        mRotateIconAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mRotateIconAnim.setDuration(500);
    }

    public View getHeader() {
        return mHeader;
    }

    public View getFooter() {
        return mFooter;
    }

    public void showLoading() {
        setStatus(STATUS_SHOW_HEADER);
        mLabel = null;
        mIcon = null;
        mHeaderLabel.setText(R.string.loading);
        setRotatingIcon(true);
        updateHeader();
    }

    public void finishLoading() {
        setStatus(STATUS_NORMAL);
        mHeaderLabel.setText(R.string.load_finished);
        setRotatingIcon(false);
    }

    public void showHeader(String label, Drawable icon) {
        mLabel = label;
        mIcon = icon;
        setRotatingIcon(false);
        setStatus(STATUS_SHOW_HEADER);
        updateHeader();
    }

    public void setRotatingIcon(boolean rotate) {
        mRotating = rotate;
        if(mRotateIconAnim.isRunning() ^ rotate) {
            if(rotate) {
                if(!mIsBeingDragged)
                    mRotateIconAnim.start();
            }
            else {
                mRotateIconAnim.cancel();
            }
        }
    }

    private void setDragState(boolean isDragged) {
        if(isDragged ^ mIsBeingDragged) {
            mIsBeingDragged = isDragged;
            updateHeader();
        }
    }

    private void updateHeader() {
        if(mIsBeingDragged) {
            mInRefreshingRegion = getScrollY() < -mHeader.getHeight();
            if(mInRefreshingRegion) {
                mHeaderLabel.setText(R.string.release_to_refresh);
            }
            else {
                mHeaderLabel.setText(R.string.pull_to_refresh);
            }
            if(mRotateIconAnim.isRunning()) {
                mRotateIconAnim.cancel();
            }
            if(mHeaderSymbol.getVisibility() == GONE) {
                mHeaderSymbol.setVisibility(VISIBLE);
            }
            if(mHeaderIcon.getVisibility() == VISIBLE) {
                mHeaderIcon.setVisibility(GONE);
            }
        }
        else {
            if(mLabel != null) {
                mHeaderLabel.setText(mLabel);
            }
            if(mIcon != null) {
                mHeaderIcon.setImageDrawable(mIcon);
                mHeaderIcon.setVisibility(VISIBLE);
                mHeaderSymbol.setVisibility(GONE);
            }
            else {
                if(mRotating && mStatus == STATUS_SHOW_HEADER) {
                    if(!mRotateIconAnim.isRunning())
                        mRotateIconAnim.start();
                }
                mHeaderIcon.setVisibility(GONE);
                mHeaderSymbol.setVisibility(VISIBLE);
            }
        }
    }

    public void setListener(PullListener listener) {
        mListener = listener;
    }

    public void setStatus (int status) {
        if(status != mStatus) {
            mStatus = status;
            if(!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            animateToPosition();
        }
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if(mTarget == null && child != mHeader && child != mFooter) {
            mTarget = child;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }

        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childRight = width - getPaddingRight();
        final int childBottom = height - getPaddingBottom();
        child.layout(childLeft, childTop, childRight, childBottom);
        mHeader.layout(childLeft, -mHeader.getMeasuredHeight(), childRight, 0);
        mFooter.layout(childLeft, childBottom, childRight, mFooter.getMeasuredHeight());
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            return;
        }
        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        mTarget.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        mHeader.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mFooter.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mTarget == null || canChildScrollUp()) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(mScroller.isFinished()) {
                    mInitialDownY = ev.getY();
                }
                else {
                    setDragState(true);
                    mInitialDownY = ev.getY();
                    mLastMotionY = mInitialDownY;
                    mScroller.abortAnimation();
                }

                break;

            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();

                final float yDiff = y - mInitialDownY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mLastMotionY = y;
                    setDragState(true);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setDragState(false);
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mTarget == null || canChildScrollUp()) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE: {
                final float y = ev.getY();
                final float deltaY = y - mLastMotionY;
                final float newScrollY = getScrollY() - deltaY*DRAG_RATE;
                mLastMotionY = y;
                if (mIsBeingDragged) {
                    if (newScrollY > 0) {
                        if(getScrollY() != 0) {
                            setScrollY(0);
                        }
                        return false;
                    }
                    else if(newScrollY < -mHeader.getHeight()) {
                        if(!mInRefreshingRegion) {
                            mHeaderLabel.setText(R.string.release_to_refresh);
                            mInRefreshingRegion = true;
                        }
                    }
                    else {
                        if(mInRefreshingRegion) {
                            mHeaderLabel.setText(R.string.pull_to_refresh);
                            mInRefreshingRegion = false;
                        }
                    }

                    setScrollY((int)newScrollY);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                setDragState(false);
                if (mInRefreshingRegion) {
                    mInRefreshingRegion = false;

                    if(mListener != null) {
                        mListener.onPullToRefresh(this, true);
                    }
                }

                animateToPosition();
                return false;
            }
        }

        return true;
    }

    public boolean canChildScrollUp() {
        if(mTarget != null) {
            return mTarget.canScrollVertically(-1);
        }
        return false;
    }

    public boolean canChildScrollDown() {
        if(mTarget != null) {
            return mTarget.canScrollVertically(1);
        }
        return false;
    }

    private void animateToPosition() {
        if(!mScroller.isFinished()) {
            int finalY = 0;
            switch (mStatus) {
                case STATUS_NORMAL:
                    finalY = 0;
                    break;
                case STATUS_SHOW_FOOTER:
                    finalY = mFooter.getHeight();
                    break;
                case STATUS_SHOW_HEADER:
                    finalY = -mHeader.getHeight();
                    break;
            }
            if(mScroller.getFinalY() == finalY)
                return;
        }

        int scrollY = getScrollY();
        int dy = 0;
        switch (mStatus) {
            case STATUS_NORMAL:
                dy = -scrollY;
                break;
            case STATUS_SHOW_HEADER:
                dy = -mHeader.getHeight() - scrollY;
                break;
            case STATUS_SHOW_FOOTER:
                dy = mFooter.getHeight() - scrollY;
                break;
        }
        int height = mHeader.getHeight();
        int duration = (int)(Math.log(Math.abs(dy)/5d + 1)*200);
        duration = Math.min(duration, 1000);
        mScroller.startScroll(0, scrollY, 0, dy, duration);
        invalidate();
        /*if(mStatus != STATUS_NORMAL) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setStatus(STATUS_NORMAL);
                }
            }, 1500);
        }*/
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            setScrollY(y);
            invalidate();
        }
        int scrollY = getScrollY();
        if(scrollY < 0 && !mRotateIconAnim.isRunning()) {
            mHeader.findViewById(android.R.id.icon).setRotation(scrollY);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int scrollY = getScrollY();
        if(scrollY <= 0 && child == mFooter) {
            return false;
        }
        else if(scrollY >= 0 && child == mHeader) {
            return false;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    public interface PullListener {
        void onPullToRefresh(PullRefreshLayout layout, boolean header);
    }
}
