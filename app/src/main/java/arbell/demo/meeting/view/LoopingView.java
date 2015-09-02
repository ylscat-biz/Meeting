package arbell.demo.meeting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.Scroller;

/**
 * Created on 2015/6/26.
 */
public class LoopingView extends ViewGroup {
    private static final boolean DEBUG = false;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;

    private Scroller mScroller;

    private EdgeEffect mLeftEdge, mRightEdge;
    private boolean isLoopingMode = true;

    private static final int MIN_FLING_VELOCITY = 400;
    private static final int MIN_DISTANCE_FOR_FLING = 25;


    private float mLastMotionX, mLastMotionY, mInitialMotionX;
    private float mOverScrolled;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private int mFlingDistance;
    private int mMinimumVelocity, mMaximumVelocity;

    private PageScrollListener mListener;
    public boolean isLock;

    public LoopingView(Context context) {
        this(context, null);
    }

    public LoopingView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoopingView);
        isLoopingMode = false;
//        a.recycle();

        ViewConfiguration conf = ViewConfiguration.get(context);
        setClickable(true);
        mTouchSlop = conf.getScaledTouchSlop();

        final float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        mMaximumVelocity = conf.getScaledMaximumFlingVelocity();
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

        mScroller = new Scroller(context);
        setLoopingMode(isLoopingMode);
    }

    public void setLoopingMode(boolean looping) {
        if(looping) {
            mLeftEdge = null;
            mRightEdge = null;
        }
        else {
            if(mLeftEdge == null) {
                Context context = getContext();
                mLeftEdge = new EdgeEffect(context);
                mRightEdge = new EdgeEffect(context);
            }
        }
        isLoopingMode = looping;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isLock;
        /*final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                if(mScrollState == SCROLL_STATE_SETTLING) {
                    mScroller.abortAnimation();
                    setScrollState(SCROLL_STATE_DRAGGING);
                    //进入拖动模式时，要让父容器不再拦截触屏事件
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                d("down");
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = ev.getY();
                final float yDiff = Math.abs(y - mLastMotionY);
                d("move");
                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    setScrollState(SCROLL_STATE_DRAGGING);
                    mInitialMotionX = mLastMotionX = dx > 0 ? mLastMotionX + mTouchSlop :
                            mLastMotionX - mTouchSlop;
                    d("drag");
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                d("up");
                return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        return mScrollState == SCROLL_STATE_DRAGGING;*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                if(mScrollState == SCROLL_STATE_SETTLING) {
                    mScroller.abortAnimation();
                    setScrollState(SCROLL_STATE_DRAGGING);
                    //进入拖动模式时，要让父容器不再拦截触屏事件
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                d("down");
                return getChildCount() > 1;
            case MotionEvent.ACTION_MOVE:
                if(mScrollState == SCROLL_STATE_DRAGGING) {
                    final float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    mLastMotionY = y;
                    if(isLoopingMode)
                        scrollBy((int)deltaX, 0);
                    else {
                        int sx = getScrollX();
                        int targetX = sx + (int)deltaX;
                        int max = Math.max(getChildCount() - 1, 0)*getWidth();
                        if(targetX < 0) {
                            if(sx > 0) {
                                mOverScrolled = targetX;
                                setScrollX(0);
                            }
                            else
                                mOverScrolled += deltaX;
                            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                mLeftEdge.onPull(mOverScrolled / getWidth(),
                                        1.f - event.getY() / getHeight());
                            else*/
                                mLeftEdge.onPull(mOverScrolled / getWidth());
                            if (!mRightEdge.isFinished()) {
                                mRightEdge.onRelease();
                            }
                        }
                        else if(targetX > max) {
                            if(sx != max) {
                                mOverScrolled = targetX - max;
                                setScrollX(max);
                            }
                            else
                                mOverScrolled += deltaX;
                            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                mRightEdge.onPull( mOverScrolled / getWidth(),
                                        event.getY() / getHeight());
                            else*/
                                mRightEdge.onPull( mOverScrolled / getWidth());
                            if (!mLeftEdge.isFinished()) {
                                mLeftEdge.onRelease();
                            }
                        } else {
                            setScrollX(targetX);
                        }

                        if (!mLeftEdge.isFinished() || !mRightEdge.isFinished()) {
                            postInvalidate();
                        }
                    }
                }
                else {
                    final float dx = x - mLastMotionX;
                    final float xDiff = Math.abs(dx);
                    final float yDiff = Math.abs(y - mLastMotionY);

                    if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                        mInitialMotionX = mLastMotionX = dx > 0 ? mLastMotionX + mTouchSlop :
                                mLastMotionX - mTouchSlop;
                        //进入拖动模式时，要让父容器不再拦截触屏事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                        d("drg");
                    }
                }

                d("mv");
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) mVelocityTracker.getXVelocity();
                final int totalDelta = (int) (x - mInitialMotionX);
                int target = getTargetPage(initialVelocity, totalDelta);
                smoothScrollTo(target*getWidth());
                if(!isLoopingMode) {
                    mLeftEdge.onRelease();
                    mRightEdge.onRelease();
                }
                d("u");
                break;
            case MotionEvent.ACTION_CANCEL:
                d("c");
                target = getTargetPage(0, 0);
                smoothScrollTo(target*getWidth());
                if(!isLoopingMode) {
                    mLeftEdge.onRelease();
                    mRightEdge.onRelease();
                }
                break;
        }
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        int left = 0;
        int right = width;
        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(left, 0, right, height);
            left = right;
            right += width;
        }
        if(mListener != null) {
            int x = getScrollX();
            int max = left;
            if(x > max - width/2)
                x = x - max;
            mListener.onPageScroll((float)x/width, getChildCount());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int width = getWidth();
        int sx = getScrollX();
        int bound = sx + width;
        int left = child.getLeft();
        int right = child.getRight();
        int count = getChildCount();
        if(left == 0 && count > 1 && sx > (count -1)*width) {
            int c = canvas.save();
            canvas.translate(count * width, 0);
            boolean ret = super.drawChild(canvas, child, drawingTime);
            canvas.restoreToCount(c);
            return ret;
        }

        if(right < sx || left > bound)
            return false;

        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if(isLoopingMode)
            return;

        if (!mLeftEdge.isFinished()) {
            final int restoreCount = canvas.save();
            final int height = getHeight() - getPaddingTop() - getPaddingBottom();

            canvas.rotate(270);
            canvas.translate(-height + getPaddingTop(), 0);
            mLeftEdge.setSize(height, getWidth());
            if (mLeftEdge.draw(canvas)) {
                postInvalidate();
            }
            canvas.restoreToCount(restoreCount);
        }
        if (!mRightEdge.isFinished()) {
            final int restoreCount = canvas.save();
            final int width = getWidth();
            final int height = getHeight() - getPaddingTop() - getPaddingBottom();

            canvas.rotate(90);
            canvas.translate(-getPaddingTop(),
                    -width*Math.max(1, getChildCount()));
            mRightEdge.setSize(height, width);
            if (mRightEdge.draw(canvas)) {
                postInvalidate();
            }
            canvas.restoreToCount(restoreCount);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        int width = getWidth();
        int count = getChildCount();
        int max = width*count;
        if(max != 0) {
            if (x < 0) {
                x += max;
                d("round tail");
            } else if (x > max) {
                x = x%max;
                d("round head");
            }
        }

        super.scrollTo(x, y);
        if(mListener != null) {
            if(x > max - width/2)
                x = x - max;
            mListener.onPageScroll((float)x/width, count);
        }
    }

    public void smoothScrollTo(int x) {
        int sx = getScrollX();
        int delta = x - sx;
        int width = getWidth();
        int duration = width == 0 ? 1000 : 1000*Math.abs(delta)/width;
        mScroller.startScroll(sx, 0, delta, 0, duration);
        setScrollState(SCROLL_STATE_SETTLING);
        computeScroll();
    }

    @Override
    public void computeScroll() {
        if(mScrollState == SCROLL_STATE_SETTLING) {
            if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
                int x = mScroller.getCurrX();
                setScrollX(x);
                invalidate();
            } else {
                setScrollState(SCROLL_STATE_IDLE);
            }
        }
    }

    public void setPageScrollListener(PageScrollListener listener) {
        mListener = listener;
    }

    private void setScrollState(int state) {
        d(String.format("%d -> %d", mScrollState, state));
        if(mScrollState != state) {
            mScrollState = state;
            if(mListener != null) {
                mListener.onStateChange(state);
            }
        }
    }

    private int getTargetPage(int velocity, int delta) {
        int sx = getScrollX();
        int width = getWidth();
        int targetPage;
        if (Math.abs(delta) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            targetPage = velocity > 0 ? sx/width : sx/width + 1;
        } else {
            targetPage = (int) ((float)sx/width + 0.5f);
        }
        if(!isLoopingMode) {
            targetPage = Math.max(0, targetPage);
            targetPage = Math.min(getChildCount() - 1, targetPage);
        }
        return targetPage;
    }

    private void d(String msg) {
        if(DEBUG)
            android.util.Log.d("LoopView", msg);
    }

    public interface PageScrollListener {
        void onPageScroll(float pagePosition, int count);
        void onStateChange(int state);
    }
}
