package com.github.ytjojo.easyscrolllayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.orhanobut.logger.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


public class EasyScrollLayout extends FrameLayout {

    private static String TAG = "TAG";

    /**
     * @hide
     */
    @IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL, ORIENTATION_BOTH, ORIENTATION_INVALID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public static final int ORIENTATION_HORIZONTAL = 0x1;
    public static final int ORIENTATION_VERTICAL = 0x2;
    public static final int ORIENTATION_BOTH = 0x3;
    public static final int ORIENTATION_INVALID = 0x0;

    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;
    public final static int GRAVITY_OUT_INVALID = -1;
    public final static int GRAVITY_OUT_LEFT = 0;
    public final static int GRAVITY_OUT_TOP = 1;
    public final static int GRAVITY_OUT_RIGHT = 2;
    public final static int GRAVITY_INNER_TOP = 3;


    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMaximumVelocity, mMinimumVelocity;

    private int mLastMotionY;
    private int mLastMotionX;
    // Down时纪录的Y坐标
    private int mFirstMotionY;
    private int mFirstMotionX;
    // 是否是下拉


    private boolean mDragging;
    public static final int INITSTATE = -16;
    private int mState = INITSTATE;
    private MotionEvent mLastMoveEvent;
    private float mInnerTopParallaxMult = 0.5f;

    //    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;
    private int mChildYOffset;
    ContentChildHolder mContentChildHolder;
    View mInnerTopView;
    View mOutTopView;
    View mOutLeftView;
    View mOutRightView;
    int mMinVerticalScrollRange;
    int mMaxVerticalScrollRange;
    private int mOrientation = ORIENTATION_INVALID;

    public EasyScrollLayout(Context context) {
        this(context, null);
    }

    public EasyScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();

        mVelocityTracker = VelocityTracker.obtain();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyScrollLayout);
        isSnap = a.getBoolean(R.styleable.EasyScrollLayout_isSnap, false);
        mInnerTopParallaxMult = a.getFloat(R.styleable.EasyScrollLayout_parallaxMultiplier, 0f);
        a.recycle();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                Logger.e(".........." + child.getClass().getName());
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
        mRefreshHeaderIndicator = new RefreshHeaderIndicator();
    }

    HorizontalScrollHandlar mHorizontalScrollHandlar;

    public void setOrientation(@EasyScrollLayout.OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        mOrientation = ORIENTATION_INVALID;
        if (mContentChildHolder == null) {
            mContentChildHolder = new ContentChildHolder();
        }
        mContentChildHolder.onMeasure(this);
        int maxArea = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity != GRAVITY_OUT_INVALID) {
                    final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                    maxArea = childArea > maxArea ? childArea : maxArea;
                    switch (lp.mLayoutOutGravity) {
                        case GRAVITY_INNER_TOP:
                            mInnerTopView = child;
                            mInnerTopParallaxMult = lp.mParallaxMultiplier;
                            break;
                        case GRAVITY_OUT_LEFT:
                            if (lp.mWidthRatioOfParent != 0f && lp.mWidthRatioOfParent <= 1f) {

                                int widthSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * lp.mWidthRatioOfParent);
                                int childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                                int childHeightSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                                measureChild(child, childWidthSpec, childHeightSpec);
                            }
                            break;
                        case GRAVITY_OUT_RIGHT:
                            if (lp.mWidthRatioOfParent != 0f && lp.mWidthRatioOfParent <= 1f) {
                                int widthSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * lp.mWidthRatioOfParent);
                                int childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                                int childHeightSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                                measureChild(child, childWidthSpec, childHeightSpec);
                            }
                            break;
                    }
                }
            }
        }
        if (mInnerTopView != null && mContentChildHolder != null) {
            int heghtSize = MeasureSpec.getSize(heightMeasureSpec) - mInnerTopView.getMinimumHeight();
            int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
            measureChild(mContentChildHolder.mDirectChild, widthMeasureSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mOrientation = ORIENTATION_INVALID;
        layoutChildren(l, t, r, b, false);
        if (mState == INITSTATE) {
            mState = OnScollListener.STATE_EXPAND;
            if (mOnScollListener != null) {
                mOnScollListener.onStateChanged(mState);
                mOnScollListener.onScroll(0, getScrollY(), getScrollY() > 0 || mMinVerticalScrollRange == 0 ? mMaxVerticalScrollRange : mMinVerticalScrollRange);
            }
        }
        if (mOutLeftView != null || mOutRightView != null) {
            if (mHorizontalScrollHandlar == null) {
                mHorizontalScrollHandlar = new HorizontalScrollHandlar();
            }
        }
        if (mHorizontalScrollHandlar != null) {
            mHorizontalScrollHandlar.setViews((ViewGroup) mContentChildHolder.mDirectChild, mOutLeftView, mOutRightView);
            mHorizontalScrollHandlar.setTopViews(mInnerTopView, mOutTopView);
            mHorizontalScrollHandlar.onLayout();
        }
        if (mContentChildHolder.canNestedScrollVetical()) {
            mOrientation |= ORIENTATION_VERTICAL;
        }
        if (getScrollY() != 0) {
            final int scrollY = getScrollY();
            if (mOutLeftView != null) {
                final int top = mOutLeftView.getTop();
                ViewCompat.offsetTopAndBottom(mOutLeftView, scrollY - top);
            }
            if (mOutRightView != null) {
                final int top = mOutRightView.getTop();
                ViewCompat.offsetTopAndBottom(mOutRightView, scrollY - top);
            }
        }
    }

    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity != GRAVITY_OUT_INVALID) {
                    layoutChildOut(child, lp, left, top, right, bottom);
                    continue;
                }
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                        break;
                }
                if (child == mContentChildHolder.mDirectChild && mInnerTopView != null) {
                    childTop += mInnerTopView.getMeasuredHeight();
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                lp.mTopWhenLayout = child.getTop();
            }
        }
    }

    private void layoutChildOut(View child, LayoutParams lp, int left, int top, int right, int bottom) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft = 0;
        int childTop = 0;

        switch (lp.mLayoutOutGravity) {
            case GRAVITY_OUT_TOP:
                childLeft = 0;
                childTop = -height;
                mOutTopView = child;
                mRefreshHeaderIndicator.setOutTopView(mOutTopView);
                mMinVerticalScrollRange = (int) (-height * (1f + lp.mOverScrollRatio));
                lp.mMinScrollY = mMinVerticalScrollRange;
                lp.mMaxScrollY = 0;
                mOrientation |= ORIENTATION_VERTICAL;

                break;
            case GRAVITY_OUT_LEFT:
                childLeft = -width;
                childTop = 0;
                mOutLeftView = child;

                mOrientation |= ORIENTATION_HORIZONTAL;
                break;
            case GRAVITY_OUT_RIGHT:
                childLeft = right;
                childTop = 0;
                mOutRightView = child;

                mOrientation |= ORIENTATION_HORIZONTAL;
                break;

            case GRAVITY_INNER_TOP:
                childLeft = 0;
                childTop = 0;
                lp.mMinScrollY = 0;
                lp.mMaxScrollY = height - child.getMinimumHeight();
                mMaxVerticalScrollRange = Math.max(mInnerTopView.getMeasuredHeight() - mInnerTopView.getMinimumHeight(), mMaxVerticalScrollRange);
                mOrientation |= ORIENTATION_VERTICAL;
                break;

        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        lp.mTopWhenLayout = child.getTop();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    RefreshHeaderIndicator mRefreshHeaderIndicator;

    public void setOutTopViewEnable(boolean enable) {
        if (mOutTopView != null) {
            LayoutParams lp = (LayoutParams) mOutTopView.getLayoutParams();
            if (lp.mEnable == enable) {
                return;
            }
            lp.mEnable = enable;
            mRefreshHeaderIndicator.setCanLoad(lp.mEnable);

        }
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    private void sendCancelEvent() {
        // The ScrollChecker will update position and lead to send cancel event when mLastMoveEvent is null.
        // fix #104, #80, #92
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, mFirstMotionX, last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }


    private MotionEvent createCancel() {
        final long time = SystemClock.uptimeMillis();
        final MotionEvent ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, mLastMotionY, 0);
        ev.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        return ev;
    }


    private final static int INVALID_ID = -1;
    private int mActivePointerId = INVALID_ID;
    private int mSecondaryPointerId = INVALID_ID;
    private float mPrimaryLastX = -1;
    private float mPrimaryLastY = -1;
    private float mSecondaryLastX = -1;
    private float mSecondaryLastY = -1;

    boolean mIsUnableToDrag;
    boolean isVerticalScroll = true;
    boolean isHorizontalScroll = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }
        super.onTouchEvent(event);
        return true;
    }

    Point mLastEventPoint = new Point();
    boolean mIgnoreTouchEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return dispatchTouchEventSupper(event);
        }
        int action = MotionEventCompat.getActionMasked(event);
        if (action != MotionEvent.ACTION_DOWN && mIgnoreTouchEvent) {
            return true;
        }
        if (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag) {
            return dispatchTouchEventSupper(event);
        }
        final MotionEvent vtev = MotionEvent.obtain(event);
        boolean isHandlar = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mNestedYOffset = 0;
                mChildYOffset = 0;
                mVelocityTracker.clear();
                mLastMotionY = (int) event.getY();
                mLastMotionX = (int) event.getX();
                mFirstMotionY = (int) event.getY();
                mFirstMotionX = (int) event.getX();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mPrimaryLastY = event.getY();
                dispatchTouchEventSupper(event);
                isHandlar = true;
                isVerticalScroll = false;
                isHorizontalScroll = false;
                mIgnoreTouchEvent = false;
                if (isFlingToNestScroll) {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    isFlingToNestScroll = false;
                }
                mIsUnableToDrag = false;
                final int rawX = (int) event.getRawX();
                final int rawY = (int) event.getRawY();
                mContentChildHolder.mVerticalScrollCheckHandlar.onDownInit(rawX, rawY);
                if (mHorizontalScrollHandlar != null) {
                    mHorizontalScrollHandlar.onDownEvent(mFirstMotionX, mFirstMotionY, EasyScrollLayout.this);
                }
                mLastEventPoint.set(mFirstMotionX, mFirstMotionY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastMoveEvent != null) {
                    mLastMoveEvent.recycle();
                }
                mLastMoveEvent = MotionEvent.obtain(event);
                int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    isHandlar = dispatchTouchEventSupper(event);
                    break;
                }
                int x = (int) event.getX(activePointerIndex);
                int y = (int) event.getY(activePointerIndex);
                if (!mDragging) {
                    int dy = y - mFirstMotionY;
                    int dx = x - mFirstMotionX;
                    final float xDiff = Math.abs(dx);
                    final float yDiff = Math.abs(dy);
                    Logger.e("mdrage dx " + xDiff + "dy" + yDiff);

                    if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff && (mOrientation & ORIENTATION_HORIZONTAL) == ORIENTATION_HORIZONTAL) {
                        isHorizontalScroll = true;
                        mDragging = true;
                        dispatchHorizontalScroll(vtev, dx);
                        mLastMotionY = y;
                        mLastMotionX = x;
                    } else if (yDiff > mTouchSlop && yDiff > xDiff && (mOrientation & ORIENTATION_VERTICAL) == ORIENTATION_VERTICAL) {
                        if (mHorizontalScrollHandlar != null && mHorizontalScrollHandlar.isHorizontallyScrolled()) {
                            if (!mHorizontalScrollHandlar.mIsDownInOuterViews) {
                                sendCancelEvent();
                                mIgnoreTouchEvent = true;
                                break;
                            } else {
                                mIsUnableToDrag = true;
                                dispatchTouchEventSupper(event);
                                break;
                            }
                        }
                        isVerticalScroll = true;
                        mDragging = true;
                        dispatchVerticalScroll(vtev, dy);
                        mLastMotionY = y;
                        mLastMotionX = x;
                    } else {
                        if (yDiff > mTouchSlop) {
                            if (mHorizontalScrollHandlar != null && mHorizontalScrollHandlar.isHorizontallyScrolled()) {
                                if (!mHorizontalScrollHandlar.mIsDownInOuterViews) {
                                    sendCancelEvent();
                                    mIgnoreTouchEvent = true;
                                    break;
                                } else {
                                    mIsUnableToDrag = true;
                                    dispatchTouchEventSupper(event);
                                    break;
                                }
                            }
                        }
                        mLastEventPoint.set(x, y);
                        dispatchTouchEventSupper(event);
                        if (mDragging) {
                            if (isVerticalScroll) {
                                vtev.offsetLocation(mFirstMotionX - x, dy > 0 ? dy - mTouchSlop : mTouchSlop - dy);
                                dispatchTouchEventSupper(vtev);
                            }
                            mLastMotionY = y;
                            mLastMotionX = x;
                        }
                    }
                } else {
                    if (isVerticalScroll) {
                        int dy = y - mLastMotionY;
                        dispatchVerticalScroll(vtev, dy);
                    }
                    if (isHorizontalScroll) {
                        int dx = x - mLastMotionX;
                        dispatchHorizontalScroll(vtev, dx);
                    }
                    mLastMotionY = y;
                    mLastMotionX = x;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(event);
                mLastMotionY = (int) MotionEventCompat.getY(event, index);
                mLastMotionX = (int) MotionEventCompat.getX(event, index);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                mFirstMotionY = mLastMotionY;
                mFirstMotionX = mLastMotionX;
                mLastEventPoint.set(mLastMotionX, mLastMotionY);
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                cancelWithAnim();
                isHandlar = dispatchTouchEventSupper(event);
                reset();
                break;
            case MotionEvent.ACTION_UP:
                isHandlar = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId);
                int velocityX = (int) VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId);
                final boolean isDownSlide = (event.getY() - mFirstMotionY) > 0;
                if (mDragging && isVerticalScroll && (canFling() || mContentChildHolder.canFling())) {
                    if (canFling()) {
                        sendCancelEvent();
                        dispatchFling(velocityY, isDownSlide);

                    } else if (mContentChildHolder.canFling()) {
                        sendCancelEvent();
                        mContentChildHolder.dispatchFling(Math.abs(velocityY) > mMinimumVelocity ? velocityY : 0, isDownSlide);
                    } else {
                        isHandlar = dispatchTouchEventSupper(event);
                    }
                } else if (mDragging && isHorizontalScroll && mHorizontalScrollHandlar.canFling()) {
                    sendCancelEvent();
                    mHorizontalScrollHandlar.dispatchFling(Math.abs(velocityX) > mMinimumVelocity ? velocityX : 0);
                } else {
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        flingToNestScroll(velocityY);
                    }
                    if (mDragging && Math.abs(velocityY) < mMinimumVelocity) {
                        sendCancelEvent();
                    } else {
                        isHandlar = dispatchTouchEventSupper(event);
                    }
                }
                reset();
                break;
        }
        mVelocityTracker.addMovement(event);
        return isHandlar;
    }

    public boolean canFling() {
        return getScrollY() >= mMinVerticalScrollRange && getScrollY() < mMaxVerticalScrollRange && getScrollY() != 0;
    }

    public void dispatchFling(int velocityY, boolean isDownSlide) {
        isFlingToNestScroll = false;
        if (getScrollY() < 0) {
            LayoutParams lp = (LayoutParams) mOutTopView.getLayoutParams();
            if (mRefreshHeaderIndicator.isComplete()) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, 0)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    mRefreshHeaderIndicator.onStopScroll(getScrollY());
                }
            }else if(mRefreshHeaderIndicator.isLoading()){
//                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, -mOutTopView.getMeasuredHeight(), -mOutTopView.getMeasuredHeight())) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                if (getScrollY() <= -mOutTopView.getMeasuredHeight()) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, -mOutTopView.getMeasuredHeight(), -mOutTopView.getMeasuredHeight())) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }else{
                    if (velocityY != 0) {
                        if (velocityY > 0) {
                            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, -mOutTopView.getMeasuredHeight(), getScrollY());
                        } else {
                            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, getScrollY(), 0);
                        }
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
            } else if (mRefreshHeaderIndicator.isPrepare()) {
                if (getScrollY() <= -mOutTopView.getMeasuredHeight() * lp.mTrigeerExpandRatio) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, -mOutTopView.getMeasuredHeight(), -mOutTopView.getMeasuredHeight())) {
                        ViewCompat.postInvalidateOnAnimation(this);
                        mRefreshHeaderIndicator.dispatchReleaseBeforeRefresh();
                    } else {
                        mRefreshHeaderIndicator.dispatchReleaseBeforeRefresh();
                        mRefreshHeaderIndicator.onStopScroll(getScrollY());
                    }
                } else {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, 0)) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    } else {
                        mRefreshHeaderIndicator.onStopScroll(getScrollY());
                    }
                }
            }

        } else {
            mRefreshHeaderIndicator.onStopScroll(getScrollY());
            // 手指离开之后，根据加速度进行滑动
            if (Math.abs(velocityY) > mMinimumVelocity) {
                fling(velocityY);

            } else {
                if (isSnap) {
                    int currentY = getScrollY();
                    // 下拉

                    if (isDownSlide) {
                        if (currentY < mMaxVerticalScrollRange) {
                            mScroller.startScroll(0, currentY, 0, -currentY);
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    } else {
                        if (currentY > 0) {
                            mScroller.startScroll(0, currentY, 0, 0
                                    - currentY);
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    }
                }

            }
        }
    }

    public void toggle() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if (currentY >= mMaxVerticalScrollRange / 2) {
            mScroller.startScroll(0, currentY, 0, -currentY);
        } else {
            mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                    - currentY);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void close() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if (currentY == mMaxVerticalScrollRange) {
            return;
        }
        mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                - currentY);
        ViewCompat.postInvalidateOnAnimation(this);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mHorizontalScrollHandlar != null) {
            return mHorizontalScrollHandlar.isHorizontallyScrolled();
        }
        return super.onInterceptTouchEvent(ev);
    }


    /**
     * 默认是打开状态，也就是getScrollY为0
     */
    public void expand() {

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if (currentY == 0) {
            return;
        }
        mScroller.startScroll(0, currentY, 0, -currentY);
        ViewCompat.postInvalidateOnAnimation(this);

    }

    public void cancelWithAnim() {
        if (!isSnap) {
            return;
        }
        int currentY = getScrollY();
        if (currentY == 0 || currentY == mMaxVerticalScrollRange) {
            return;
        }
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        if (currentY >= mMaxVerticalScrollRange / 2) {
            mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                    - currentY);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            mScroller.startScroll(0, currentY, 0, -currentY);
            ViewCompat.postInvalidateOnAnimation(this);

        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void dispatchVerticalScroll(MotionEvent event, int dy) {
//        Logger.e("parentScrollY    " + getScrollY());
        int activePointerIndex = event.findPointerIndex(mActivePointerId);
        float curY = event.getY(activePointerIndex);
        parentPreScroll(0, dy, mScrollConsumed);
        int preScrollConsumed = mScrollConsumed[1];
        if (preScrollConsumed != 0) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        mNestedYOffset += mScrollConsumed[1];
        int unconsumedY = (dy - mScrollConsumed[1]);
        childScroll(event, 0, unconsumedY, mScrollConsumed);
        mChildYOffset += mScrollConsumed[1];
        unconsumedY = unconsumedY - mScrollConsumed[1];

        int totaldy = (int) (curY - mFirstMotionY);
        int childConsumed = mScrollConsumed[1];
        if (Math.abs(dy) < Math.abs(childConsumed) || dy * childConsumed < 0) {
            Logger.e("出错了 dy " + dy + " child " + childConsumed + "   preScrollConsumed" + preScrollConsumed);
        }
        Logger.e(mNestedYOffset + "mChildYOffset  " + mChildYOffset + "totaldy  " + totaldy + " dy " + dy + " child " + childConsumed);
    }

    private void dispatchHorizontalScroll(MotionEvent event, int dx) {
        int activePointerIndex = event.findPointerIndex(mActivePointerId);
        float curX = event.getX(activePointerIndex);
        mHorizontalScrollHandlar.scrollConsumed(dx, mScrollConsumed);
        int preScrollConsumed = mScrollConsumed[0];
        int unconsumedX = (dx - mScrollConsumed[0]);
        childScroll(event, unconsumedX, 0, mScrollConsumed);
        unconsumedX = unconsumedX - mScrollConsumed[0];
        int totaldX = (int) (curX - mFirstMotionX);
        int childConsumed = mScrollConsumed[0];
        if (Math.abs(dx) < Math.abs(childConsumed) || dx * childConsumed < 0) {
            Logger.e("出错了 dy " + dx + " child " + childConsumed + "   preScrollConsumed" + preScrollConsumed);
        }
    }

    private void parentPreScroll(int dx, int dy, int[] consumed) {
        consumed[1] = consumed[0] = 0;
        if (!canPreScroll(dy)) {
            mContentChildHolder.preScrollConsumed(dy, consumed);
            return;
        }
        int lastScrolly = getScrollY();
        scrollBy(0, -dy);
        int consumedDy = consumed[1] = lastScrolly - getScrollY();
        Logger.e(dy + "parentPreScroll " + consumedDy);
        if (dy - consumedDy != 0) {
            consumed[1] = 0;
            mContentChildHolder.preScrollConsumed(dy - consumedDy, consumed);
            consumed[1] = consumedDy + consumed[1];
        }

    }

    private void childScroll(MotionEvent event, int dx, int dy, int[] consumed) {
        if (dy == 0 && dx == 0) {
            consumed[1] = consumed[0] = 0;
            return;
        }
        int activePointerIndex = event.findPointerIndex(mActivePointerId);
        if (dx != 0) {
            float curY = event.getY(activePointerIndex);
            event.offsetLocation(0, mFirstMotionY - curY);
            dispatchTouchEventSupper(event);
        } else if (dy != 0) {
            float curX = event.getX(activePointerIndex);
            float curY = event.getY(activePointerIndex);
            event.offsetLocation(mFirstMotionX - curX, 0);
            dispatchTouchEventSupper(event);
        }


    }

    private boolean canPreScroll(int dy) {

        if (dy > 0 && mContentChildHolder.reachChildTop()) {
            return true;
        } else if (dy < 0) {
            return true;
        }
        return false;

    }

    private void reset() {
        mVelocityTracker.clear();
        mActivePointerId = INVALID_ID;
        mDragging = false;
        mIgnoreTouchEvent = false;
        if (mLastMoveEvent != null) {
            mLastMoveEvent.recycle();
            mLastMoveEvent = null;
        }
        mScrollConsumed[0] = mScrollConsumed[1] = 0;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        int index = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        Log.e(TAG, pointerIndex + "pointerIndex" + pointerId + " = id    up" + index + "count =" + MotionEventCompat.getPointerCount(ev));
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }


    @Override
    public boolean canScrollVertically(int direction) {
        final int offset = getScrollY();
        if (direction > 0) {
            return offset < mMaxVerticalScrollRange;
        } else {
            return offset > mMinVerticalScrollRange;
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (mHorizontalScrollHandlar == null) {
            return true;
        }
        return mHorizontalScrollHandlar.canScrollHorizontally(direction);
    }

    boolean isSnap;

    public void fling(int velocityY) {
        if (getScrollY() >= 0 && getScrollY() <= mMaxVerticalScrollRange) {
            if (velocityY > 0) {
                if (getScrollY() == 0) {
                    return;
                }
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, isSnap ? 0 : getScrollY());
            } else {
                if (getScrollY() == mMaxVerticalScrollRange) {
                    return;
                }
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, isSnap ? mMaxVerticalScrollRange : -2 * mMaxVerticalScrollRange, mMaxVerticalScrollRange);

            }
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void flingNoSnap(int velocityY) {
        isFlingToNestScroll = false;
        if (velocityY > 0) {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, getScrollY());
        } else {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, getScrollY(), mMaxVerticalScrollRange);

        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    boolean isFlingToNestScroll;

    private void flingToNestScroll(int velocityY) {
        isFlingToNestScroll = false;
        if ((mOrientation & ORIENTATION_VERTICAL) != ORIENTATION_VERTICAL) {
            return;
        }
        if (getScrollY() <= 0) {
            return;
        }
        if (velocityY < 0 && !mContentChildHolder.canNestedFlingToBottom()) {
            return;
        }
        if (mContentChildHolder.reachChildBottom() || mContentChildHolder.reachChildTop()) {
            return;
        }
        isFlingToNestScroll = true;
        int initialY = velocityY < 0 ? Integer.MAX_VALUE : 0;
        mScroller.fling(0, initialY, 0, velocityY,
                0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void scrollTo(int x, int y) {
        final int min = Math.min(mMinVerticalScrollRange, mMaxVerticalScrollRange);
        final int max = Math.max(mMinVerticalScrollRange, mMaxVerticalScrollRange);
        if (y < min) {
            y = min;
        }
        if (y > max) {
            y = max;
        }
        final int lastScrolly = getScrollY();
        if (!mRefreshHeaderIndicator.getCanLoad()) {
            final int limitScrollY = mRefreshHeaderIndicator.getLimitScrollY();
            if (lastScrolly >= limitScrollY && y < limitScrollY) {
                y = limitScrollY;
            }
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
            mRefreshHeaderIndicator.onScrollChanged(lastScrolly,y);
            final int scrollY = getScrollY();
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mIgnoreScroll) {
                    final int top = child.getTop() - lp.mTopWhenLayout;
                    ViewCompat.offsetTopAndBottom(child, scrollY - top);
                }
            }
            if ( mInnerTopView != null) {
                if(getScrollY() >= 0){
                    float offsetRatio = ((float) getScrollY()) / mMaxVerticalScrollRange;
                    if (mOnScollListener != null) {
                        if (offsetRatio == 0) {
                            mState = OnScollListener.STATE_EXPAND;
                            mOnScollListener.onStateChanged(OnScollListener.STATE_EXPAND);

                        } else if (offsetRatio == 1) {
                            mState = OnScollListener.STATE_COLLAPSED;
                            mOnScollListener.onStateChanged(OnScollListener.STATE_COLLAPSED);
                        }
                        mOnScollListener.onScroll(offsetRatio, getScrollY(), max);
                    }
                    if (mInnerTopParallaxMult != 0) {
                        int totalOffset = (int) ((mInnerTopView.getMeasuredHeight() - mInnerTopView.getMinimumHeight()) * mInnerTopParallaxMult);
                        float verticalOffset = totalOffset * offsetRatio;
                        ViewCompat.setTranslationY(mInnerTopView, (int) verticalOffset);
                    }
                }else if(lastScrolly > 0 && y <0){
                    if (mOnScollListener != null) {
                        mState = OnScollListener.STATE_EXPAND;
                        mOnScollListener.onStateChanged(OnScollListener.STATE_EXPAND);
                    }
                    if (mInnerTopParallaxMult != 0) {
                        ViewCompat.setTranslationY(mInnerTopView, 0);
                    }
                }

            }

        }

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int curY = mScroller.getCurrY();
            if (isFlingToNestScroll) {
                if (mContentChildHolder.reachChildTop()) {
                    isFlingToNestScroll = false;
                    final float velocityY = mScroller.getCurrVelocity();
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        mScroller.abortAnimation();
                        fling((int) velocityY);
                    }
                } else if (mContentChildHolder.reachChildBottom()) {
                    isFlingToNestScroll = false;
                    final float velocityY = mScroller.getCurrVelocity();
                    if (Math.abs(velocityY) >= mMinimumVelocity && velocityY > 0) {
                        mScroller.abortAnimation();
                        mContentChildHolder.fling((int) -velocityY);
                    }
                }
            } else {
                scrollTo(0, curY);
                if (mScroller.isFinished()) {
                    mRefreshHeaderIndicator.onStopScroll(getScrollY());
                }
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setParallaxMult(float parallaxMult) {
        this.mInnerTopParallaxMult = parallaxMult;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        Logger.e(disallowIntercept + "disallowIntercept");
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (mIsUnableToDrag) {
            return;
        }
        if (!mDragging && !isVerticalScroll && !isHorizontalScroll) {
            final int dy = Math.abs(mLastEventPoint.y - mFirstMotionY);
            final int dx = Math.abs(mLastEventPoint.x - mFirstMotionX);
            Logger.e(dx + "dx  dy" + dy);
            if (dy - mTouchSlop > 0 && dx - mTouchSlop > 0) {
                if (dy - mTouchSlop > dx - mTouchSlop && (mOrientation & ORIENTATION_HORIZONTAL) == ORIENTATION_HORIZONTAL) {
                    isHorizontalScroll = true;
                    mDragging = true;
                } else if ((mOrientation & ORIENTATION_VERTICAL) == ORIENTATION_VERTICAL) {
                    isVerticalScroll = true;
                    mDragging = true;
                }
            } else {
                if (dx - mTouchSlop > 0 && (mOrientation & ORIENTATION_HORIZONTAL) == ORIENTATION_HORIZONTAL) {
                    isHorizontalScroll = true;
                    mDragging = true;
                } else if (dy - mTouchSlop > 0 && (mOrientation & ORIENTATION_VERTICAL) == ORIENTATION_VERTICAL) {
                    isVerticalScroll = true;
                    mDragging = true;
                }
            }
        }

    }

    ShadowDrawable mShadowDrawable;
    GradientDrawable mGradientDrawable;

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

        boolean result = super.drawChild(canvas, child, drawingTime);

        if (child == mOutLeftView) {
            if (mGradientDrawable == null) {
                mGradientDrawable = new GradientDrawable();
                mGradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

                mGradientDrawable.setColors(new int[]{0x34000000, 0x11000000, 0x00000000});

            }
            mGradientDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
            mGradientDrawable.setBounds(child.getRight() - 60, child.getTop(), child.getRight(), child.getBottom());
            mGradientDrawable.draw(canvas);
        }
        if (child == mOutRightView) {
            if (mShadowDrawable == null) {
                mShadowDrawable = new ShadowDrawable(getContext(), Gravity.LEFT);
            }
            mShadowDrawable.setmShadowGravity(Gravity.LEFT);
            mShadowDrawable.setBounds(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            mShadowDrawable.draw(canvas);
//            Logger.e(child.getLeft() + "getLeft " + child.getX());
        }
        return result;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (mShadowDrawable != null && mShadowDrawable.isVisible() != visible) {
            mShadowDrawable.setVisible(visible, false);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mShadowDrawable || who == mShadowDrawable;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        float mOverScrollRatio = 0.7f;
        int mMinScrollY;
        int mMaxScrollY;
        int mLayoutOutGravity = GRAVITY_OUT_INVALID;
        float mParallaxMultiplier;
        float mWidthRatioOfParent;
        boolean mIgnoreScroll;
        float mTrigeerExpandRatio = 1f;
        int mTopWhenLayout;
        boolean mEnable;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyScrollLayout);
            mLayoutOutGravity = a.getInt(R.styleable.EasyScrollLayout_easylayout_layoutGravity, GRAVITY_OUT_INVALID);
            mParallaxMultiplier = a.getFloat(R.styleable.EasyScrollLayout_parallaxMultiplier, 0);
            if (mLayoutOutGravity == GRAVITY_OUT_LEFT) {
                mWidthRatioOfParent = a.getFloat(R.styleable.EasyScrollLayout_outleftWidth_ratioOfParent, 0);
                mIgnoreScroll = true;
            }
            if (mLayoutOutGravity == GRAVITY_OUT_RIGHT) {
                mIgnoreScroll = true;
                mWidthRatioOfParent = a.getFloat(R.styleable.EasyScrollLayout_outRightWidth_ratioOfParent, 0);
            }
            mOverScrollRatio = a.getFloat(R.styleable.EasyScrollLayout_scrollmaster_overscrollratio, 0.7f);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        /**
         * Copy constructor. Clones the width, height, margin values, and
         * gravity of the source.
         *
         * @param source The layout params to copy from.
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
        }

        public float getOverScrollRatio() {
            return mOverScrollRatio;
        }

        public void setOverScrollRatio(float overScrollRatio) {
            this.mOverScrollRatio = overScrollRatio;
        }

        public int getLayoutOutGravity() {
            return mLayoutOutGravity;
        }

        public void setLayoutOutGravity(int layoutOutGravity) {
            this.mLayoutOutGravity = layoutOutGravity;
        }

        public float getParallaxMultiplier() {
            return mParallaxMultiplier;
        }

        public void setmParallaxMultiplier(float parallaxMultiplier) {
            this.mParallaxMultiplier = parallaxMultiplier;
        }

        public float getWidthRatioOfParent() {
            return mWidthRatioOfParent;
        }

        public void setWidthRatioOfParent(float widthRatioOfParent) {
            this.mWidthRatioOfParent = widthRatioOfParent;
        }

        public boolean isEnable() {
            return mEnable;
        }

        public void setEnable(boolean mEnable) {
            this.mEnable = mEnable;
        }
    }

    public OnScollListener mOnScollListener;

    public void setOnScollListener(OnScollListener l) {
        this.mOnScollListener = l;
    }

    public interface OnScollListener {
        public final int STATE_EXPAND = 1;
        public final int STATE_COLLAPSED = -1;
        public final int STATE_DRAGING = 0;

        void onScroll(float positionOffset, int positionOffsetPixels, int offsetRange);

        void onStateChanged(int state);
    }

    /**
     * Interpolator that enforces a specific starting velocity. This is useful to avoid a
     * discontinuity between dragging speed and flinging speed.
     * <p>
     * Similar to a {@link android.view.animation.AccelerateInterpolator} in the sense that
     * getInterpolation() is a quadratic function.
     */
    private static class AcceleratingFlingInterpolator implements Interpolator {

        private final float startingSpeedPixelsPerFrame;
        private final float durationMs;
        private final int pixelsDelta;
        private final float numberFrames;

        public AcceleratingFlingInterpolator(int durationMs, float startingSpeedPixelsPerSecond,
                                             int pixelsDelta) {
            startingSpeedPixelsPerFrame = startingSpeedPixelsPerSecond / getRefreshRate();
            this.durationMs = durationMs;
            this.pixelsDelta = pixelsDelta;
            numberFrames = this.durationMs / getFrameIntervalMs();
        }

        @Override
        public float getInterpolation(float input) {
            final float animationIntervalNumber = numberFrames * input;
            final float linearDelta = (animationIntervalNumber * startingSpeedPixelsPerFrame)
                    / pixelsDelta;
            // Add the results of a linear interpolator (with the initial speed) with the
            // results of a AccelerateInterpolator.
            if (startingSpeedPixelsPerFrame > 0) {
                return Math.min(input * input + linearDelta, 1);
            } else {
                // Initial fling was in the wrong direction, make sure that the quadratic component
                // grows faster in order to make up for this.
                return Math.min(input * (input - linearDelta) + linearDelta, 1);
            }
        }

        private float getRefreshRate() {
            // TODO
//            DisplayInfo di = DisplayManagerGlobal.getInstance().getDisplayInfo(
//                    Display.DEFAULT_DISPLAY);
//            return di.refreshRate;
            return 30f;
        }

        public long getFrameIntervalMs() {
            return (long) (1000 / getRefreshRate());
        }
    }

    /**
     * Interpolator from android.support.v4.view.ViewPager. Snappier and more elastic feeling
     * than the default interpolator.
     */
    private static final Interpolator INTERPOLATOR = new Interpolator() {

        /**
         * {@inheritDoc}
         */
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public static class CurrentPagerAdapter extends PagerAdapter {

        ArrayList<View> mViews;
        ArrayList<String> mTitles;
        View mCurrentView;

        public CurrentPagerAdapter(ArrayList<View> views, ArrayList<String> titles) {
            this.mViews = views;
            this.mTitles = titles;
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            ((ViewPager) container).removeView(mViews.get(position));
        }

        //每次滑动的时候生成的组件
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mViews.get(position).getParent() == container) {
                container.removeView(mViews.get(position));
            }
            ((ViewPager) container).addView(mViews.get(position));
            return mViews.get(position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentView = (View) object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mTitles == null || mTitles.isEmpty()) {
                return super.getPageTitle(position);
            }
            return mTitles.get(position);
        }

        public View getPrimaryItem() {
            return mCurrentView;
        }

        public View getCurentView(int postion) {
            return mViews.get(postion);
        }
    }


}
