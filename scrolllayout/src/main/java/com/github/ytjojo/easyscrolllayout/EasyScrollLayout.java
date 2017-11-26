package com.github.ytjojo.easyscrolllayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
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

    /** @hide */
    @IntDef({HORIZONTAL, VERTICAL,ORIENTATION_BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int ORIENTATION_BOTH = 2;

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
    private boolean mHasSendCancelEvent;
    private MotionEvent mLastMoveEvent;
    private boolean mPreventForHorizontal;
//    private int mMaxTopTranslationY ;
    private float mMaxTopTranslationYRate =0.5f ;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;
    private int mChildYOffset;
    ContentWraperView mContentView;
    View mInnerTopView;
    View mOuterLeftView;
    View mOuterRightView;
    int mMinVerticalScrollRange;
    int mMaxVerticalScrollRange;
    int mMinHorizontalScrollRange;
    int mMaxHorizontalScrollRange;
    public EasyScrollLayout(Context context) {
        this(context,null);
    }

    public EasyScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
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

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        int maxArea =0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if(child instanceof ContentWraperView){
                    mContentView = (ContentWraperView) child;
                    continue;
                }
                if(lp.mLayoutOutGravity != GRAVITY_OUT_INVALID){
                    final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                    maxArea = childArea> maxArea?childArea:maxArea;
                    switch (lp.mLayoutOutGravity){
                        case GRAVITY_INNER_TOP:
                            mInnerTopView = child;
                            break;
                    }
                }
            }
        }
        if(mInnerTopView != null && mContentView != null ){
            int heghtSize = MeasureSpec.getSize(heightMeasureSpec)-mInnerTopView.getMinimumHeight();
            int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
            measureChild(mContentView, widthMeasureSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l,t,r,b,false);
        if(mState == INITSTATE){
            mState = OnScollListener.STATE_EXPAND;
            if(mOnScollListener !=null){
                mOnScollListener.onStateChanged(mState);
                mOnScollListener.onScroll(0,getScrollY(),getScrollY()>0||mMinVerticalScrollRange==0?mMaxVerticalScrollRange:mMinVerticalScrollRange);
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
                if(lp.mLayoutOutGravity != GRAVITY_OUT_INVALID){
                    layoutChildOuter(child,lp,left,top,right,bottom);
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
                if(child == mContentView){
                    childTop += mInnerTopView.getMeasuredHeight();
                }else {

                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }
    private void layoutChildOuter(View child ,LayoutParams lp, int left, int top, int right, int bottom){
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft = 0;
        int childTop = 0;

        switch (lp.mLayoutOutGravity){
            case GRAVITY_OUT_TOP:
                childLeft = 0;
                childTop = -height;
                mMinVerticalScrollRange = (int) (-height*(1f+lp.mOverScrollRatio));
                lp.mMinScrollY = mMinVerticalScrollRange;
                lp.mMaxScrollY = 0;
                break;
            case GRAVITY_OUT_LEFT:
                childLeft = -width;
                childTop = 0;
                mMinHorizontalScrollRange = (int) (-width*(1f+lp.mOverScrollRatio));
                break;
            case GRAVITY_OUT_RIGHT:
                childLeft = right ;
                childTop = 0;
                mMaxHorizontalScrollRange = (int) (width*(1f+lp.mOverScrollRatio));
                break;

            case GRAVITY_INNER_TOP:
                childLeft = 0 ;
                childTop = 0;
                lp.mMinScrollY =0;
                lp.mMaxScrollY = child.getMeasuredHeight() -child.getMinimumHeight();
                mMaxVerticalScrollRange = Math.max(mInnerTopView.getMeasuredHeight() -mInnerTopView.getMinimumHeight(),mMaxVerticalScrollRange);
                break;

        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    private void sendCancelEvent() {
        // The ScrollChecker will update position and lead to send cancel event when mLastMoveEvent is null.
        // fix #104, #80, #92
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL,mFirstMotionX, last.getY(), last.getMetaState());
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

    private boolean inChild(View child, int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final int scrollX = getScrollX();
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft() - scrollX
                    || x >= child.getRight() - scrollX);
        }
        return false;
    }

    private final static int INVALID_ID = -1;
    private int mActivePointerId = INVALID_ID;
    private int mSecondaryPointerId = INVALID_ID;
    private float mPrimaryLastX = -1;
    private float mPrimaryLastY = -1;
    private float mSecondaryLastX = -1;
    private float mSecondaryLastY = -1;

    boolean isChanged;
    boolean isVerticalScroll = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled() || mContentView == null){
            return super.onTouchEvent(event);
        }
        super.onTouchEvent(event);
        return isVerticalScroll;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled() || mContentView == null ) {
            return dispatchTouchEventSupper(event);
        }
        int action = MotionEventCompat.getActionMasked(event);
        if (action != MotionEvent.ACTION_DOWN && !isVerticalScroll) {
            return dispatchTouchEventSupper(event);
        }
        final MotionEvent vtev = MotionEvent.obtain(event);
        boolean isHandlar = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mNestedYOffset = 0;
                mChildYOffset = 0;
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                mVelocityTracker.clear();
                mLastMotionY = (int) event.getY();
                mLastMotionX = (int) event.getX();
                mFirstMotionY = (int) event.getY();
                mFirstMotionX = (int) event.getX();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mPrimaryLastY = event.getY();
                dispatchTouchEventSupper(event);
                isChanged = false;
                isHandlar = true;
                isVerticalScroll = true;
                mContentView.mScrollChildHandlar.onDownInit();
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent =  MotionEvent.obtain(event);
                int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    isHandlar = dispatchTouchEventSupper(event);
                    break;
                }
                int x = (int) event.getX( activePointerIndex);
                int y = (int) event.getY( activePointerIndex);
                if (!mDragging) {
                    int dy = y - mFirstMotionY;
                    int dx = x - mFirstMotionX;
                    if (Math.abs(dy) > mTouchSlop) {
                        if (Math.abs(dy) > Math.abs(dx)) {
                            mDragging = true;
//                            if(dy >0){
//                                dy -= mTouchSlop;
//                            }else {
//                                dy += mTouchSlop;
//                            }
                            isHandlar =true;
                            dispatchScroll(vtev,dy);
                            mLastMotionY =  y;
                            mLastMotionX =  x;
                        } else {
                            mDragging = false;
                            isVerticalScroll = false;
                            isHandlar = dispatchTouchEventSupper(event);
                            cancelWithAnim();
                        }

                    }else{
                        dispatchTouchEventSupper(event);
                        isHandlar = true;
                    }
                }else {
                    isHandlar = true;
                    int dy = y - mLastMotionY;
                    if(Math.abs(dy)<=1){
                        vtev.offsetLocation(mFirstMotionX - x,0);
                        dispatchTouchEventSupper(vtev);
                        break;
                    }
                    dispatchScroll(vtev,dy);
                    mLastMotionY =  y;
                    mLastMotionX =  x;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(event);
                mLastMotionY = (int) MotionEventCompat.getY(event, index);
                mLastMotionX = (int) MotionEventCompat.getX(event,index);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                mFirstMotionY = mLastMotionY;
                mFirstMotionX = mLastMotionX;
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                isVerticalScroll = true;
                cancelWithAnim();
                isHandlar = dispatchTouchEventSupper(event);
                reset();
                break;
            case MotionEvent.ACTION_UP:
                isVerticalScroll = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId);
                if (mDragging &&getScrollY()> mMinVerticalScrollRange && getScrollY() < mMaxVerticalScrollRange && getScrollY() != 0) {
                    mDragging = false;
                    isHandlar = true;
                    isFlingToNestScroll = false;
                    sendCancelEvent();
                    if(getScrollY() < 0){
                        if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, mMinVerticalScrollRange, 0)) {
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    }else {
                        // 手指离开之后，根据加速度进行滑动
                        if (Math.abs(velocityY) > mMinimumVelocity) {
                            fling(velocityY);

                        } else {
                            if(isSnap){
                                int currentY = getScrollY();
                                // 下拉
                                final boolean isDownSlide = (event.getY() - mFirstMotionY) > 0;
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


                } else {
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        flingToNestScroll(velocityY);
                    }
                    if(mDragging &&Math.abs(velocityY) < mMinimumVelocity ){
                        sendCancelEvent();
                    }else{
                        isHandlar = dispatchTouchEventSupper(event);
                    }
                }
                reset();
                break;
        }
        mVelocityTracker.addMovement(event);
        return isHandlar;
    }
    public void toggle(){
        if(!mScroller.isFinished()){
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if(currentY>=mMaxVerticalScrollRange/2){
            mScroller.startScroll(0, currentY, 0, -currentY);
        }else{
            mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                    - currentY);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }
    public void close(){
        if(!mScroller.isFinished()){
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if(currentY == mMaxVerticalScrollRange){
            return;
        }
        mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                - currentY);
        ViewCompat.postInvalidateOnAnimation(this);
    }
    private void springback(){

    }
    @Override
    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        super.scrollTo(scrollX, scrollY);
    }

    /**
     * 默认是打开状态，也就是getScrollY为0
     */
    public void expand(){

        if(!mScroller.isFinished()){
            mScroller.abortAnimation();
        }
        int currentY = getScrollY();
        if(currentY == 0){
            return;
        }
        mScroller.startScroll(0, currentY, 0, -currentY);
        ViewCompat.postInvalidateOnAnimation(this);

    }
    public void cancelWithAnim(){
        if(!isSnap){
            return;
        }
        int currentY = getScrollY();
        if(currentY ==0||currentY ==mMaxVerticalScrollRange){
            return;
        }
        if(!mScroller.isFinished()){
            mScroller.abortAnimation();
        }

        if(currentY>=mMaxVerticalScrollRange/2){
            mScroller.startScroll(0, currentY, 0, mMaxVerticalScrollRange
                    - currentY);
            ViewCompat.postInvalidateOnAnimation(this);
        }else{
            mScroller.startScroll(0, currentY, 0, -currentY);
            ViewCompat.postInvalidateOnAnimation(this);

        }

        ViewCompat.postInvalidateOnAnimation(this);
    }
    private void dispatchScroll(MotionEvent event, int dy){
        int activePointerIndex = event.findPointerIndex( mActivePointerId);
        float curY = event.getY(activePointerIndex);
        parentPreScroll(0, dy,mScrollConsumed,mScrollOffset);
        int preScrollConsumed = mScrollConsumed[1];
        mNestedYOffset+=mScrollConsumed[1];
        int unconsumedY =  (dy-mScrollConsumed[1]);
        childScroll(event,0, unconsumedY,mScrollConsumed,mScrollOffset);
        mChildYOffset += mScrollConsumed[1];
        unconsumedY = unconsumedY- mScrollConsumed[1];

        int totaldy = (int) (curY-mFirstMotionY);
        int childConsumed = mScrollConsumed[1];
        if(Math.abs(dy)+1<Math.abs(childConsumed)||dy*childConsumed<0){
            Logger.e( "出错了 dy " + dy + " child "+ childConsumed + "   preScrollConsumed"+ preScrollConsumed );
        }
        if(unconsumedY!=0){
            parentScroll(0, unconsumedY,mScrollConsumed,mScrollOffset);
            mChildYOffset += mScrollConsumed[1];
            Logger.e("parentScroll   "+ mScrollConsumed[1]);
        }
        Logger.e(mNestedYOffset+"mChildYOffset  "+mChildYOffset +"totaldy  "+totaldy + " dy " + dy + " child "+ childConsumed);

    }

    private void parentPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow){
        if(!checkSelfConsume(dy)){
            consumed[1] =consumed[0] = 0;
            return;
        }
        int lastScrolly = getScrollY();
        scrollBy(0, (int) -dy);
        consumed[1]= lastScrolly - getScrollY();
        Logger.e("parentPreScroll  " + consumed[1]);
    }
    private void childScroll(MotionEvent event,int dx, int dy, int[] consumed, int[] offsetInWindow){
        if(dy==0){
            consumed[1] =consumed[0] = 0;
            return;
        }
        int activePointerIndex = event.findPointerIndex( mActivePointerId);
        float curX = event.getX(activePointerIndex);
//        float curY = event.getY(activePointerIndex);
        event.offsetLocation(mFirstMotionX - curX,0);
        dispatchTouchEventSupper(event);
        mContentView.mScrollChildHandlar.childScrollConsumed(mScrollConsumed);


    }
    private void parentScroll(int dx, int dy, int[] consumed, int[] offsetInWindow){
        consumed[1] = consumed[0] =0;
        if(dy>0){
            if(mContentView.reachChildTop()){
                int lastScrolly = getScrollY();
                scrollBy(0, (int) -dy);
                consumed[1]= lastScrolly - getScrollY();
            }
        }else if(dy <0){
            if(mContentView.reachChildBottom()){
                int lastScrolly = getScrollY();
                scrollBy(0, (int) -dy);
                consumed[1]= lastScrolly - getScrollY();
            }
        }

    }

    private boolean checkSelfConsume(float dy){

        if(dy > 0 && mContentView.reachChildTop()){
            return true;
        }else if(dy <0){
            return true;
        }
        return false;

    }

    private void reset() {
        mVelocityTracker.clear();
        mActivePointerId = INVALID_ID;
        mDragging = false;
        isChanged = false;
        mLastMoveEvent = null;
        mScrollConsumed[0] = mScrollConsumed[1] =0;
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
            mLastMotionY = (int) ev.getY( newPointerIndex);
            mActivePointerId = ev.getPointerId( newPointerIndex);
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
        final int offset = getScrollX();
        if (direction > 0) {
            return offset < mMaxHorizontalScrollRange;
        } else {
            return offset > mMinHorizontalScrollRange;
        }
    }
    boolean isSnap;
    public void fling(int velocityY) {
        isFlingToNestScroll = false;
        if (velocityY > 0) {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, isSnap?0:3*mMaxVerticalScrollRange);
        } else {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, isSnap?mMaxVerticalScrollRange:-2* mMaxVerticalScrollRange, mMaxVerticalScrollRange);

        }
        ViewCompat.postInvalidateOnAnimation(this);
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
        if(getScrollY()<= 0){
            isFlingToNestScroll = false;
            return;
        }
        // For reasons I do not understand, scrolling is less janky when maxY=Integer.MAX_VALUE
        // then when maxY is set to an actual value.
        if (velocityY > 0) {
            isFlingToNestScroll = true;
//            mScroller.fling(0, mContentView.getHeight() + getScrollY(), 0, -velocityY, 0, 0, 0, 0);
//
            int initialY = velocityY < 0 ? Integer.MAX_VALUE : 0;
            mScroller.fling(0, initialY, 0, velocityY,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        final int  min = Math.min(mMinVerticalScrollRange,mMaxVerticalScrollRange);
        final int  max = Math.max(mMinVerticalScrollRange,mMaxVerticalScrollRange);
        if (y < min) {
            y =  min;
        }
        if (y > max) {
            y =  max;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
            if(getScrollY() >= 0 && mInnerTopView != null){
                float offsetRatio = ((float) getScrollY()) / mMaxVerticalScrollRange;
                if(mOnScollListener !=null){
                    if(offsetRatio ==0){
                        mState = OnScollListener.STATE_EXPAND;
                        mOnScollListener.onStateChanged(OnScollListener.STATE_EXPAND);

                    }else if(offsetRatio ==1){
                        mState = OnScollListener.STATE_COLLAPSED;
                        mOnScollListener.onStateChanged(OnScollListener.STATE_COLLAPSED);
                    }
                    mOnScollListener.onScroll(offsetRatio,getScrollY(),max);

                    if (mMaxTopTranslationYRate != 0) {
                        int totalOffset = (int) ((mInnerTopView.getMeasuredHeight()-mInnerTopView.getMinimumHeight()) * mMaxTopTranslationYRate);
                        float verticalOffset = totalOffset * offsetRatio;
                        ViewCompat.setTranslationY(mInnerTopView, (int) verticalOffset);
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
//                int startY = mScroller.getStartY();
//                int finalY = mScroller.getFinalY();
//                Logger.e( reachChildTop() + "cury" + curY +"velocityY = "+velocityY + "  startY=" + startY +" finalY= " +finalY);
                if (mContentView.reachChildTop()) {
                    float velocityY = mScroller.getCurrVelocity();
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        mScroller.abortAnimation();
                        fling((int) velocityY);
//                        int currentY =getScrollY();
//                        mScroller.startScroll(0, currentY, 0, -currentY);
                    }
                }
            } else {
                scrollTo(0, curY);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    public void setMaxTopTranslationYRate(float rage){
        this.mMaxTopTranslationYRate = rage;
    }



    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        float mOverScrollRatio = 0.7f;
        int mMinScrollY;
        int mMaxScrollY;
        int mLayoutOutGravity = GRAVITY_OUT_INVALID;


        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyScrollLayout);
            mLayoutOutGravity = a.getInt(R.styleable.EasyScrollLayout_easylayout_layoutGravity, GRAVITY_OUT_INVALID);
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

    }
    public OnScollListener mOnScollListener;
    public void setOnScollListener(OnScollListener l){
        this.mOnScollListener = l;
    }
    public  interface OnScollListener{
        public final int STATE_EXPAND = 1;
        public final int STATE_COLLAPSED = -1;
        public final int STATE_DRAGING =0;
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
            if(mViews.get(position).getParent() ==container){
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
            if (mTitles ==null ||mTitles.isEmpty()) {
                return super.getPageTitle(position);
            }
            return mTitles.get(position);
        }

        public View getPrimaryItem() {
            return mCurrentView;
        }
        public View getCurentView(int postion){
            return mViews.get(postion);
        }
    }


}
