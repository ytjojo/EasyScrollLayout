package com.github.ytjojo.scrollmaster;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.orhanobut.logger.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


public class ScrollMasterView extends FrameLayout {

    public static final int STATE_EXPAND = 1;
    public static final int STATE_COLLAPSED = -1;
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

    private final int[] mScrollConsumed = new int[4];
    private int mNestedYOffset;
    private int mNestedXOffset;
    private float mFrictionFactor = 0f;
    ContentChildHolder mContentChildHolder;
    View mInnerTopView;
    View mOutTopView;
    View mOutLeftView;
    View mOutRightView;
    int mMinVerticalScrollRange;
    int mMaxVerticalScrollRange;
    private int mOrientation = ORIENTATION_INVALID;
    private boolean isDrawerLayoutStyle;
    int mLayoutStartOffsetY ;

    public ScrollMasterView(Context context) {
        this(context, null);
    }

    public ScrollMasterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollMasterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();


        mVelocityTracker = VelocityTracker.obtain();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollMasterView);
        isSnap = a.getBoolean(R.styleable.ScrollMasterView_sm_isSnap, false);
        isDrawerLayoutStyle = a.getBoolean(R.styleable.ScrollMasterView_sm_isDrawerLayoutStyle, false);
        mInnerTopParallaxMult = a.getFloat(R.styleable.ScrollMasterView_sm_parallaxMultiplier, 0f);
        mLayoutStartOffsetY = a.getDimensionPixelOffset(R.styleable.ScrollMasterView_sm_layoutstartoffsety,0);
        mShadowStyle = a.getInt(R.styleable.ScrollMasterView_sm_drawer_shadowstyle,0);
        a.recycle();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                switch (lp.mLayoutOutGravity) {
                    case GRAVITY_OUT_TOP:
                        mOutTopView = child;
                        break;
                    case GRAVITY_OUT_LEFT:
                        mOutLeftView = child;
                        if (mHorizontalScrollHandlar == null) {
                            mHorizontalScrollHandlar = new HorizontalScrollHandlar(ScrollMasterView.this, isDrawerLayoutStyle);
                        }
                        break;
                    case GRAVITY_OUT_RIGHT:
                        mOutRightView = child;
                        if (mHorizontalScrollHandlar == null) {
                            mHorizontalScrollHandlar = new HorizontalScrollHandlar(ScrollMasterView.this, isDrawerLayoutStyle);
                        }
                        break;

                    case GRAVITY_INNER_TOP:
                        mInnerTopView = child;
                        break;

                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
        mTopHeaderIndicator = new TopHeaderIndicator();
        mContentChildHolder = new ContentChildHolder();
    }

    HorizontalScrollHandlar mHorizontalScrollHandlar;

    public void setOrientation(@ScrollMasterView.OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;

        }
    }
    public void setLayoutStartOffsetY(int layoutStartOffsetY){
        this.mLayoutStartOffsetY = layoutStartOffsetY;
    }
    public int getLayoutStartOffsetY(){
        return mLayoutStartOffsetY;
    }

    public HorizontalScrollHandlar getHorizontalScrollHandlar() {
        return mHorizontalScrollHandlar;
    }

    public void setLeftComplete() {
        mHorizontalScrollHandlar.setLeftComplete();
    }

    public void setRightComplete() {
        mHorizontalScrollHandlar.setRightComplete();
    }

    public void setLeftOnStartLoadCallback(BaseRefreshIndicator.OnStartLoadCallback callback) {
        mHorizontalScrollHandlar.getLeftRefreshInidicator().setOnStartLoadCallback(callback);
    }

    public void setRightOnStartLoadCallback(BaseRefreshIndicator.OnStartLoadCallback callback) {
        mHorizontalScrollHandlar.getRightRefreshIndicator().setOnStartLoadCallback(callback);
    }

    public void setTopHeaderOnStartLoadCallback(BaseRefreshIndicator.OnStartLoadCallback callback) {
        mTopHeaderIndicator.setOnStartLoadCallback(callback);
    }

    public void setTopHeaderLoadComplete() {

        if (mOutTopView == null || !ViewCompat.isLaidOut(mOutTopView)) {
            return;
        }
        if (mTopHeaderIndicator.isLoading()) {
            mTopHeaderIndicator.setComplete();
            final int scrollY = getScrollY();
            if (scrollY < 0) {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mScroller.startScroll(getScrollX(), scrollY, 0, -scrollY, 300);
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mTopHeaderIndicator.onStopScroll(scrollY);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        mOrientation = ORIENTATION_INVALID;

        mContentChildHolder.onMeasure(this);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity != GRAVITY_OUT_INVALID) {
                    switch (lp.mLayoutOutGravity) {
                        case GRAVITY_INNER_TOP:
                            mInnerTopView = child;
                            mInnerTopParallaxMult = lp.mParallaxMultiplier;
                            isSnap = lp.isSnap;
                            break;
                        case GRAVITY_OUT_LEFT:
                            if (lp.mWidthRatioOfParent > 0f && lp.mWidthRatioOfParent <= 1f) {

                                int widthSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * lp.mWidthRatioOfParent);
                                int childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                                int childHeightSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                                measureChild(child, childWidthSpec, childHeightSpec);
                            }
                            break;
                        case GRAVITY_OUT_RIGHT:
                            if (lp.mWidthRatioOfParent > 0f && lp.mWidthRatioOfParent <= 1f) {
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
        if (mContentChildHolder != null) {
            if(mInnerTopView != null ){
                int heghtSize = MeasureSpec.getSize(heightMeasureSpec) - ViewCompat.getMinimumHeight(mInnerTopView);
                int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
                measureChild(mContentChildHolder.mDirectChild, widthMeasureSpec, childHeightSpec);
            }else {
                if(mLayoutStartOffsetY < 0 ){
                    int heghtSize = MeasureSpec.getSize(heightMeasureSpec);
                    heghtSize -= mLayoutStartOffsetY;
                    int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
                    measureChild(mContentChildHolder.mDirectChild, widthMeasureSpec, childHeightSpec);
                }
            }


        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed,l,t,r,b);
        resetValue();
        layoutChildren(l, t, r, b, false);
        if (mState == INITSTATE) {
            mState = STATE_EXPAND;
            dispatchOnScroll(0f, getScrollY(), mMaxVerticalScrollRange);
        }

        if (mHorizontalScrollHandlar != null) {
            mHorizontalScrollHandlar.setViews(mContentChildHolder.mDirectChild, mOutLeftView, mOutRightView);
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

                final int layoutDirection = ViewCompat.getLayoutDirection(this);
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
                if (child == mContentChildHolder.mDirectChild) {
                    childTop += mLayoutStartOffsetY;
                    if( mInnerTopView != null && mInnerTopView.getVisibility() == VISIBLE){
                        childTop += mInnerTopView.getMeasuredHeight();
                    }
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
                mFrictionFactor = lp.mFrictionFactor;
                mTopHeaderIndicator.setTargetView(mOutTopView);
                mMinVerticalScrollRange = (int) (-height * (1f + lp.mOverScrollRatio));
                lp.mStableScrollValue = -height;
                lp.mMinScrollY = mMinVerticalScrollRange;
                lp.mMaxScrollY = 0;
                mOrientation |= ORIENTATION_VERTICAL;
                mTopHeaderIndicator.setOverScrollValue(mMinVerticalScrollRange);
                mTopHeaderIndicator.setLimitValue(0);
                mTopHeaderIndicator.setTriggerValue((int) (-height * lp.mTrigeerExpandRatio));
                mTopHeaderIndicator.setStableValue(-height);
                if(mLayoutStartOffsetY>0){
                    childTop+=mLayoutStartOffsetY;
                }
                break;
            case GRAVITY_OUT_LEFT:
                childLeft = -width;
                childTop = 0;
                mOutLeftView = child;
                lp.mStableScrollValue = width;
                mOrientation |= ORIENTATION_HORIZONTAL;
                break;
            case GRAVITY_OUT_RIGHT:
                childLeft = width;
                childTop = 0;
                mOutRightView = child;
                lp.mStableScrollValue = -width;
                mOrientation |= ORIENTATION_HORIZONTAL;
                break;

            case GRAVITY_INNER_TOP:
                childLeft = 0;
                childTop = 0 + mLayoutStartOffsetY;
                lp.mMinScrollY = 0;
                mMaxVerticalScrollRange += height - ViewCompat.getMinimumHeight(child);
                if(mLayoutStartOffsetY < 0){
                    mMaxVerticalScrollRange +=mLayoutStartOffsetY;
                }
                lp.mMaxScrollY = mMaxVerticalScrollRange;
                mOrientation |= ORIENTATION_VERTICAL;
                break;

        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        lp.mTopWhenLayout = child.getTop();
    }

    private void resetValue() {
        mOrientation = ORIENTATION_INVALID;
        mMinVerticalScrollRange = 0;
        mMaxVerticalScrollRange = 0;
        if(mLayoutStartOffsetY >  0){

            mMaxVerticalScrollRange += mLayoutStartOffsetY;
        }else if( mLayoutStartOffsetY < 0 ) {
            mMinVerticalScrollRange = mLayoutStartOffsetY;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeCallbacks(mFlingResume);
    }

    TopHeaderIndicator mTopHeaderIndicator;

    public void setCanTopHeaderLoad(boolean enable) {
        if (mOutTopView != null) {
            LayoutParams lp = (LayoutParams) mOutTopView.getLayoutParams();
            if (lp.mEnable == enable) {
                return;
            }
            lp.mEnable = enable;
            mTopHeaderIndicator.setCanLoad(lp.mEnable);

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
    SparseArray<Integer> mNestScrollOffsetYs = new SparseArray<>(5);

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
                mNestedXOffset = 0;
                mNestScrollOffsetYs.clear();
                mVelocityTracker.clear();
                mLastMotionY = (int) event.getY();
                mLastMotionX = (int) event.getX();
                mFirstMotionY = (int) event.getY();
                mFirstMotionX = (int) event.getX();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mNestScrollOffsetYs.put(mActivePointerId,0);
                mPrimaryLastY = event.getY();
                isHandlar = true;
                mFlingResume.reset();
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
                if (mHorizontalScrollHandlar != null && (mOrientation & ORIENTATION_HORIZONTAL) == ORIENTATION_HORIZONTAL) {
                    mHorizontalScrollHandlar.onDownEvent(mFirstMotionX, mFirstMotionY, ScrollMasterView.this);
                }
                dispatchTouchEventSupper(event);
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
                        getParent().requestDisallowInterceptTouchEvent(true);
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
                        final ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
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
                                dispatchVerticalScroll(null,dy);
                            }else if(isHorizontalScroll){
                                dispatchHorizontalScroll(null,dx);
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
                mNestScrollOffsetYs.put(mActivePointerId,0);
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                mNestScrollOffsetYs.put(mActivePointerId,0);
                onSecondaryPointerUp(event);
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                cancelWithAnim();
                isHandlar = dispatchTouchEventSupper(event);
                resetTouch();
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
                resetTouch();
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
           if(mOutTopView != null){
               outTopViewFling(velocityY);
           }else {
               if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, 0)) {
                   ViewCompat.postInvalidateOnAnimation(this);
               }
           }
        } else {
            mTopHeaderIndicator.onStopScroll(getScrollY());
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
    public void outTopViewFling(int velocityY){
        LayoutParams lp = (LayoutParams) mOutTopView.getLayoutParams();
        if (mTopHeaderIndicator.isComplete()) {
            if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mTopHeaderIndicator.onStopScroll(getScrollY());
            }
        } else if (mTopHeaderIndicator.isLoading()) {
//                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, -mOutTopView.getMeasuredHeight(), -mOutTopView.getMeasuredHeight())) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
            if (getScrollY() <= lp.mStableScrollValue) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollValue, lp.mStableScrollValue)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            } else {
                if (velocityY != 0) {
                    if (velocityY > 0) {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, lp.mStableScrollValue, getScrollY());
                    } else {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, getScrollY(), 0);
                    }
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }
        } else if (mTopHeaderIndicator.isPrepare()) {
            if (getScrollY() <= -mOutTopView.getMeasuredHeight() * lp.mTrigeerExpandRatio) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollValue, lp.mStableScrollValue)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                    mTopHeaderIndicator.dispatchReleaseBeforeRefresh();
                } else {
                    mTopHeaderIndicator.dispatchReleaseBeforeRefresh();
                    mTopHeaderIndicator.onStopScroll(getScrollY());
                }
            } else {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, 0)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    mTopHeaderIndicator.onStopScroll(getScrollY());
                }
            }
        }

    }
    FlingResume mFlingResume = new FlingResume(this);

    public void toggle() {
        if (this.isLayoutRequested() || mInnerTopView == null || !ViewCompat.isLaidOut(mInnerTopView) || mInnerTopView.isLayoutRequested()) {
            return;
        }
        if (getScrollY() < 0) {
            return;
        }
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
        if (this.isLayoutRequested() || mInnerTopView == null || !ViewCompat.isLaidOut(mInnerTopView) || mInnerTopView.isLayoutRequested()) {
            return;
        }
        if (getScrollY() < 0) {
            return;
        }
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
            if(!mHorizontalScrollHandlar.mIsDownInOuterViews){
                return mHorizontalScrollHandlar.isHorizontallyScrolled();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }


    /**
     * 默认是打开状态，也就是getScrollY为0
     */
    public void expand() {
        if (this.isLayoutRequested() || mInnerTopView == null || !ViewCompat.isLaidOut(mInnerTopView) || mInnerTopView.isLayoutRequested()) {
            return;
        }
        if (getScrollY() < 0) {
            return;
        }
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
        parentPreScroll(dy, mScrollConsumed);
        int preScrollConsumed = mScrollConsumed[1];
        if (preScrollConsumed != 0) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        int unconsumedY = (dy - mScrollConsumed[1]);
        mNestedYOffset += mScrollConsumed[1];
        int value = mNestScrollOffsetYs.get(mActivePointerId);
        value += mScrollConsumed[1] - mScrollConsumed[3];
        mNestScrollOffsetYs.put(mActivePointerId, value);
        if(event != null){
            childScroll(event, 0, unconsumedY, mScrollConsumed);
        }
    }

    private void dispatchHorizontalScroll(MotionEvent event, int dx) {
        mScrollConsumed[0] = mScrollConsumed[1] = mScrollConsumed[2] = mScrollConsumed[3] = 0;
        mHorizontalScrollHandlar.scrollConsumed(dx, mScrollConsumed);
        int unconsumedX = (dx - mScrollConsumed[0]);
        mNestedXOffset += mScrollConsumed[0] - mScrollConsumed[2];
        if(event !=null){
            childScroll(event, unconsumedX, 0, mScrollConsumed);
        }
    }

    private void parentPreScroll(int dy, int[] consumed) {
        consumed[0] = consumed[1] = consumed[2] = consumed[3] = 0;
        if (!canPreScroll(dy)) {
            mContentChildHolder.preScrollConsumed(dy, consumed);
            return;
        }
        mContentChildHolder.preScrollUp(dy, consumed);
        dy -= consumed[1];
        if (dy == 0) {
            return;
        }
        int childonsumedDy = consumed[1];
        consumed[1] = consumed[0] = 0;
        scrollBy(dy, consumed);
        int consumedDy = consumed[1];
        if (dy - consumedDy != 0) {
            consumed[1] = 0;
            mContentChildHolder.preScrollConsumed(dy - consumedDy, consumed);
            consumed[1] = consumedDy + consumed[1];
        }
        consumed[1] += childonsumedDy;

    }

    private void scrollBy(int dy, int[] consumed) {
        int lastScrollY = getScrollY();
        if (mFrictionFactor > 0) {
            if (lastScrollY > 0) {
                if (lastScrollY - dy < 0) {
                    scrollBy(0, (int) ((lastScrollY - dy) * (1 - mFrictionFactor)));
                } else {
                    scrollBy(0, -dy);
                }
            } else if (lastScrollY < 0) {
                if (lastScrollY - dy * (1 - mFrictionFactor) > 0) {
                    scrollBy(0, (int) -(dy - lastScrollY / (1 - mFrictionFactor) + lastScrollY));
                } else {
                    scrollBy(0, (int) (-dy * (1 - mFrictionFactor)));
                }
            } else {
                if (dy < 0) {
                    scrollBy(0, -dy);
                } else {
                    scrollBy(0, (int) (-dy * (1 - mFrictionFactor)));
                }
            }
            final int curScrollY = getScrollY();
            if (lastScrollY < 0) {
                if (curScrollY < 0) {
                    consumed[1] = dy;
                } else if (curScrollY == 0) {
                    consumed[1] = (int) ((lastScrollY - curScrollY) / (1 - mFrictionFactor));
                } else {
                    consumed[1] = dy;
                }
            } else if (lastScrollY > 0) {
                if (curScrollY >= 0) {
                    consumed[1] = lastScrollY - getScrollY();
                } else {
                    consumed[1] = dy;
                }
            } else {
                if (curScrollY > 0) {
                    consumed[1] = dy;
                } else if (curScrollY < 0) {
                    consumed[1] = dy;
                }

            }
        } else {
            scrollBy(0, -dy);
            consumed[1] = consumed[1] = lastScrollY - getScrollY();
        }
        consumed[3] += lastScrollY - getScrollY();
        Logger.e(dy + "parentPreScroll " + consumed[1]);
    }

    private void childScroll(MotionEvent event, int dx, int dy, int[] consumed) {
        if (dy == 0 && dx == 0) {
            consumed[1] = consumed[0] = consumed[2] = consumed[3] = 0;

            return;
        }
        int activePointerIndex = event.findPointerIndex(mActivePointerId);
        if (dx != 0) {
            float curY = event.getY(activePointerIndex);
            event.offsetLocation(-mNestedXOffset, mFirstMotionY - curY);
            dispatchTouchEventSupper(event);
        } else if (dy != 0) {
//            float curX = event.getX(activePointerIndex);
//            event.offsetLocation(mFirstMotionX - curX, -mNestedYOffset);
//            dispatchTouchEventSupper(event);
            float curX = event.getX(activePointerIndex);
            event.offsetLocation(mFirstMotionX - curX, -mNestScrollOffsetYs.get(mActivePointerId));
            dispatchTouchEventSupper(event);
        }


    }

    private boolean canPreScroll(int dy) {

        if (dy > 0 && mContentChildHolder.reachChildTop()) {
            return true;
        } else if (dy < 0 && getScrollY()< mMaxVerticalScrollRange) {
            return true;
        }
        return false;

    }

    private void resetTouch() {
        mVelocityTracker.clear();
        mActivePointerId = INVALID_ID;
        mDragging = false;
        mIgnoreTouchEvent = false;
        if (mLastMoveEvent != null) {
            mLastMoveEvent.recycle();
            mLastMoveEvent = null;
        }
        mNestedYOffset = 0;
        mNestedXOffset = 0;
        mScrollConsumed[0] = mScrollConsumed[1] = mScrollConsumed[2] = mScrollConsumed[3] = 0;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        int index = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        mNestScrollOffsetYs.put(pointerId,0);
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
            return false;
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
                if(isSnap){
                    mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, isSnap ? mMaxVerticalScrollRange : -2 * mMaxVerticalScrollRange, mMaxVerticalScrollRange);
                }else {
                    mFlingResume.start(getScrollY(),-velocityY);
                    mFlingResume.setMaxVerticalScrollRange(mMaxVerticalScrollRange);

                }
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
        if (!mTopHeaderIndicator.getCanLoad()) {
            final int limitScrollY = mTopHeaderIndicator.getLimitValue();
            if (lastScrolly >= limitScrollY && y < limitScrollY) {
                y = limitScrollY;
            }
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
            mTopHeaderIndicator.onScrollChanged(lastScrolly, y);
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
            if (mInnerTopView != null) {
                if (getScrollY() >= 0) {
                    float offsetRatio = ((float) scrollY) / max;
                    if (offsetRatio == 0) {
                        mState = STATE_EXPAND;

                    } else if (offsetRatio == 1) {
                        mState = STATE_COLLAPSED;
                    }
                    dispatchOnScroll(offsetRatio, scrollY, max);
                    if (mInnerTopParallaxMult != 0) {
                        int totalOffset = (int) ((mInnerTopView.getMeasuredHeight() - ViewCompat.getMinimumHeight(mInnerTopView)) * mInnerTopParallaxMult);
                        float verticalOffset = totalOffset * offsetRatio;
                        ViewCompat.setTranslationY(mInnerTopView, (int) verticalOffset);
                    }
                } else if (lastScrolly > 0 && y <= 0) {
                    dispatchOnScroll(0f,0,max);
                    mState = STATE_EXPAND;
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
                    mFlingResume.setTrigger();
                    mTopHeaderIndicator.onStopScroll(getScrollY());
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
        if(mDragging){
            return;
        }
        if(mOrientation ==ORIENTATION_HORIZONTAL ){
            final int dy = Math.abs(mLastEventPoint.y - mFirstMotionY);
            final int dx = Math.abs(mLastEventPoint.x - mFirstMotionX);
            if (dy - mTouchSlop > 0 && dx - mTouchSlop > 0 && dx > dy) {
                ViewParent parent = getParent();
                while (parent !=null){
                    if(parent instanceof ScrollMasterView){
                        ScrollMasterView scrollMasterView = (ScrollMasterView) parent;
                        scrollMasterView.setUnableToDrag();
                        break;
                    }
                    parent = parent.getParent();
                }
                isHorizontalScroll = true;
                mDragging = true;
            }
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (!isEnabled()) {
            return;
        }
        if (mIsUnableToDrag) {
            return;
        }
        if (!mDragging && !isVerticalScroll && !isHorizontalScroll) {
            final int dy = Math.abs(mLastEventPoint.y - mFirstMotionY);
            final int dx = Math.abs(mLastEventPoint.x - mFirstMotionX);
            Logger.e(dx + "dx  dy" + dy);
            if (dy - mTouchSlop > 0 && dx - mTouchSlop > 0) {
//                if (dy - mTouchSlop > dx - mTouchSlop && (mOrientation & ORIENTATION_HORIZONTAL) == ORIENTATION_HORIZONTAL) {
//                    isHorizontalScroll = true;
//                    mDragging = true;
//                } else if ((mOrientation & ORIENTATION_VERTICAL) == ORIENTATION_VERTICAL) {
//                    isVerticalScroll = true;
//                    mDragging = true;
//                }
                if  ((mOrientation & ORIENTATION_VERTICAL) == ORIENTATION_VERTICAL) {
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
            /**
             * 如果child已经requestDisallowInterceptTouchEvent 仍然不能拖拽，就忽略此次滑动
             */
            if (dy - mTouchSlop > 0 || dx - mTouchSlop > 0) {
                if (!mIsUnableToDrag && !mDragging) {
                    mIsUnableToDrag = true;
                }
            }
        }

    }

    public void setUnableToDrag(){
        mIsUnableToDrag = true;
    }

    ShadowDrawable mShadowDrawable;
    GradientDrawable mGradientDrawable;

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

        boolean result = super.drawChild(canvas, child, drawingTime);

        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void drawGradientDrawable(View child, Canvas canvas) {
        if (mGradientDrawable == null) {
            mGradientDrawable = new GradientDrawable();
            mGradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            mGradientDrawable.setColors(new int[]{0x34000000, 0x11000000, 0x00000000});
        }
        mGradientDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
        mGradientDrawable.setBounds(child.getRight() - 60, child.getTop(), child.getRight(), child.getBottom());
        mGradientDrawable.draw(canvas);
    }
    private int mShadowStyle = ShadowDrawable.STYLE_GRADIENT;
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHorizontalScrollHandlar == null || !mHorizontalScrollHandlar.isDrawerLayoutStyle) {
            return;
        }
        if (mHorizontalScrollHandlar.getScrollX() < 0) {
            if (mContentChildHolder.mDirectChild != null) {
                if (mShadowDrawable == null) {
                    mShadowDrawable = new ShadowDrawable(getContext(), Gravity.LEFT);
                    mShadowDrawable.setShadowStyle(mShadowStyle);

                }
                mShadowDrawable.setOffsetRatio(mHorizontalScrollHandlar.getCurOffsetRatio());
                if (mHorizontalScrollHandlar.isOutLeftViewTopOfContent()) {
                    mShadowDrawable.setShadowGravity(Gravity.RIGHT);
                    mShadowDrawable.setBounds(0, mOutRightView.getTop(), mOutRightView.getLeft(), mOutRightView.getBottom());
                } else {
                    mShadowDrawable.setShadowGravity(Gravity.LEFT);
                    mShadowDrawable.setBounds(mContentChildHolder.mDirectChild.getRight(), mOutRightView.getTop(), getMeasuredWidth(), mOutRightView.getBottom());

                }
                mShadowDrawable.draw(canvas);
            }
        } else if (mHorizontalScrollHandlar.getScrollX() > 0) {
            if (mContentChildHolder.mDirectChild != null) {
                if (mShadowDrawable == null) {
                    mShadowDrawable = new ShadowDrawable(getContext(), Gravity.LEFT);
                    mShadowDrawable.setShadowStyle(mShadowStyle);
                }
                mShadowDrawable.setOffsetRatio(mHorizontalScrollHandlar.getCurOffsetRatio());
                if (mHorizontalScrollHandlar.isOutLeftViewTopOfContent()) {
                    mShadowDrawable.setShadowGravity(Gravity.LEFT);
                    mShadowDrawable.setBounds(mOutLeftView.getRight(), mOutLeftView.getTop(), getMeasuredWidth(), mOutLeftView.getBottom());
                } else {
                    mShadowDrawable.setShadowGravity(Gravity.RIGHT);
                    mShadowDrawable.setBounds(0, mOutLeftView.getTop(), mContentChildHolder.mDirectChild.getLeft(), mOutLeftView.getBottom());

                }

                mShadowDrawable.draw(canvas);
            }
        }
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
        int mStableScrollValue;
        float mFrictionFactor;
        boolean isSnap;
        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollMasterView);
            mLayoutOutGravity = a.getInt(R.styleable.ScrollMasterView_sm_layoutGravity, GRAVITY_OUT_INVALID);
            mParallaxMultiplier = a.getFloat(R.styleable.ScrollMasterView_sm_parallaxMultiplier, 0);
            mTrigeerExpandRatio = a.getFloat(R.styleable.ScrollMasterView_sm_trigeerExpandRatio, 1.2f);
            mFrictionFactor = a.getFloat(R.styleable.ScrollMasterView_sm_frictionfactor,0f);
            mIgnoreScroll = a.getBoolean(R.styleable.ScrollMasterView_sm_ignorescroll,false);
            float defaultOverScrollRatio = 0.7f;
            if (mLayoutOutGravity == GRAVITY_OUT_LEFT) {
                mWidthRatioOfParent = a.getFloat(R.styleable.ScrollMasterView_sm_left_widthRatioOfParent, 0);
                if (mTrigeerExpandRatio > 0.8f || mTrigeerExpandRatio < 0.2f) {
                    mTrigeerExpandRatio = 0.5f;
                }
                mIgnoreScroll = true;
                defaultOverScrollRatio = 0f;
            }
            if (mLayoutOutGravity == GRAVITY_OUT_RIGHT) {
                mWidthRatioOfParent = a.getFloat(R.styleable.ScrollMasterView_sm_left_widthRatioOfParent, 0);
                if (mTrigeerExpandRatio > 0.8f || mTrigeerExpandRatio < 0.2f) {
                    mTrigeerExpandRatio = 0.5f;
                }
                mIgnoreScroll = true;
                mWidthRatioOfParent = a.getFloat(R.styleable.ScrollMasterView_sm_right_widthRatioOfParent, 0);
                defaultOverScrollRatio = 0f;
            }
            mOverScrollRatio = a.getFloat(R.styleable.ScrollMasterView_sm_overscrollratio, defaultOverScrollRatio);
            isSnap = a.getBoolean(R.styleable.ScrollMasterView_sm_isSnap, false);
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

        public void setParallaxMultiplier(float parallaxMultiplier) {
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
        public void setSnap(boolean snap){
            this.isSnap = snap;
        }
        public boolean getSnap(){
            return isSnap;
        }
    }


    ArrayList<OnScollListener> mOnScollListeners;
    public void addOnScrollListener(OnScollListener l){
        if(mOnScollListeners == null){
            mOnScollListeners = new ArrayList<>();
        }
        mOnScollListeners.add(l);
    }
    public void dispatchOnScroll(float offsetRatio, int positionOffsetPixels, int offsetRange){
        if(mOnScollListeners != null) {
            for(OnScollListener l: mOnScollListeners){
                l.onScroll(offsetRatio,positionOffsetPixels,offsetRange);
            }
        }
    }

    public interface OnScollListener {
        void onScroll(float offsetRatio, int positionOffsetPixels, int offsetRange);

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

    public void setDrawShadowStyle(int style){
        if(mShadowDrawable == null){
            mShadowDrawable = new ShadowDrawable(getContext(),Gravity.LEFT);
        }
        mShadowDrawable.setShadowStyle(style);
    }
    public void setDrawerShadowColor(@ColorInt int color){
        if(mShadowDrawable == null){
            mShadowDrawable = new ShadowDrawable(getContext(),Gravity.LEFT);
        }
        mShadowDrawable.setColor(color);
    }

    public void setShadowRadius(float radius){
        if(mShadowDrawable == null){
            mShadowDrawable = new ShadowDrawable(getContext(),Gravity.LEFT);
        }
        mShadowDrawable.setShadowRadius(radius);
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
