package com.github.ytjojo.scrollmaster;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;


/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class ContentWraperView extends FrameLayout {


    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;
    public final static int GRAVITY_OUT_INVALID = -1;
    public final static int GRAVITY_OUT_TOP = 1;
    public final static int GRAVITY_OUT_BOTTOM = 2;
    public final static int GRAVITY_INNER_BOTTOM = 3;


    TopHeaderIndicator mTopHeaderIndicator;
    BottomFooterIndicator mBottomFooterIndicator;
    OverScroller mScroller;
    private int mMinimumVelocity;
    private float mFrictionFactor = 0.6f;

    public ContentWraperView(Context context) {
        this(context, null);
    }

    public ContentWraperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentWraperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollMasterView);
        isSnap = a.getBoolean(R.styleable.ScrollMasterView_sm_isSnap, false);
        a.recycle();
        mTopHeaderIndicator = new TopHeaderIndicator();
        mBottomFooterIndicator = new BottomFooterIndicator();
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                switch (lp.mLayoutOutGravity) {
                    case GRAVITY_OUT_TOP:
                        mOutTopView = child;
                        mTopHeaderIndicator.setTargetView(mOutTopView);
                        break;
                    case GRAVITY_OUT_BOTTOM:
                        mOutBottomView = child;
                        mBottomFooterIndicator.setTargetView(mOutBottomView);
                        break;
                    case GRAVITY_INNER_BOTTOM:
                        mInnerBottomView = child;
                        if (isSnap || lp.isSnap) {
                            isSnap = true;
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l, t, r, b, false);
    }

    View mContentView;
    View mInnerBottomView;
    View mOutTopView;
    View mOutBottomView;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();

        ArrayList<View> contentViews = new ArrayList<>(count);
        int maxArea = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity == GRAVITY_OUT_INVALID) {
                    contentViews.add(child);
                }
            }
        }
        if (!contentViews.isEmpty()) {
            int contentViewsCount = contentViews.size();
            for (int i = 0; i < contentViewsCount; i++) {
                final View child = contentViews.get(i);
                final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                if (childArea >= maxArea) {
                    mContentView = child;
                    maxArea = childArea;
                }
            }
        }
    }

    int mMinVerticalScrollRange;
    int mMaxVerticalScrollRange;

//    int mScrollRange;


    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();
        mMaxVerticalScrollRange = 0;
        mMinVerticalScrollRange = 0;
        mBottomFooterIndicator.setLimitValue(0);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity != GRAVITY_OUT_INVALID) {
                    layoutChildOuter(child, lp, left, top, right, bottom);
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
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                lp.mTopWhenLayout = child.getTop();
            }
        }
    }

    private void layoutChildOuter(View child, LayoutParams lp, int left, int top, int right, int bottom) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft = 0;
        int childTop = 0;

        switch (lp.mLayoutOutGravity) {
            case GRAVITY_OUT_TOP:
                childLeft = 0;
                childTop = -height;
                mMinVerticalScrollRange = (int) (-height * (1f + lp.mOverScrollRatio));
                mTopHeaderIndicator.setOverScrollValue(mMinVerticalScrollRange);
                mTopHeaderIndicator.setLimitValue(0);
                mTopHeaderIndicator.setTriggerValue((int) (-height * lp.mTrigeerExpandRatio));
                mTopHeaderIndicator.setStableValue(-height);
                lp.mMaxScrollY = 0;
                lp.mMinScrollY = mMinVerticalScrollRange;
                lp.mStableScrollY = -height;
                mOutTopView = child;
                mTopHeaderIndicator.setTargetView(mOutTopView);
                break;
            case GRAVITY_OUT_BOTTOM:
                childLeft = 0;
                childTop = bottom - top + (mInnerBottomView == null ? 0 : mInnerBottomView.getMeasuredHeight());
                int scrollRange = (int) (height * (1f + lp.mOverScrollRatio)) + (mInnerBottomView == null ? 0 : mInnerBottomView.getMeasuredHeight());
                mMaxVerticalScrollRange = Math.max(scrollRange, mMaxVerticalScrollRange);
                lp.mStableScrollY = (mInnerBottomView == null ? height : mInnerBottomView.getMeasuredHeight() + height);
                lp.mMaxScrollY = scrollRange;
                lp.mMinScrollY = childTop - (bottom - top);
                mOutBottomView = child;
                mBottomFooterIndicator.setTargetView(mOutBottomView);

                mBottomFooterIndicator.setOverScrollValue(mMaxVerticalScrollRange);
                mBottomFooterIndicator.setLimitValue(lp.mMinScrollY);
                mBottomFooterIndicator.setTriggerValue(lp.mMinScrollY + (int) (height * lp.mTrigeerExpandRatio));
                mBottomFooterIndicator.setStableValue(lp.mStableScrollY);
                break;
            case GRAVITY_INNER_BOTTOM:
                childLeft = 0;
                childTop = bottom - top;
                lp.mMinScrollY = 0;
                lp.mMaxScrollY = height;
                mInnerBottomView = child;
                mBottomFooterIndicator.setLimitValue(height);
                if (mOutBottomView == null) {
                    mBottomFooterIndicator.setOverScrollValue(height);

                }
                break;
            default:
                break;
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        lp.mTopWhenLayout = child.getTop();
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
        int mStableScrollY;
        boolean mIgnoreScroll;
        float mTrigeerExpandRatio;
        int mTopWhenLayout;
        boolean isSnap;
        boolean isHeaderDrawerLayoutStyle;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollMasterView);
            mLayoutOutGravity = a.getInt(R.styleable.ScrollMasterView_sm_contentwrper_layoutGravity, GRAVITY_OUT_INVALID);
            mOverScrollRatio = a.getFloat(R.styleable.ScrollMasterView_sm_overscrollratio, 0.7f);
            mTrigeerExpandRatio = a.getFloat(R.styleable.ScrollMasterView_sm_trigeerExpandRatio, 1.2f);
            isSnap = a.getBoolean(R.styleable.ScrollMasterView_sm_isSnap, false);
            isHeaderDrawerLayoutStyle = a.getBoolean(R.styleable.ScrollMasterView_sm_isDrawerLayoutStyle, false);
            if (isHeaderDrawerLayoutStyle && (mTrigeerExpandRatio < 0.2f || mTrigeerExpandRatio > 0.8f)) {
                mTrigeerExpandRatio = 0.5f;
            }
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

        int mLayoutOutGravity = GRAVITY_OUT_INVALID;

        public float getOverScrollRatio() {
            return mOverScrollRatio;
        }

        public void setOverScrollRatio(float overScrollRatio) {
            this.mOverScrollRatio = overScrollRatio;
        }

        public int getMinScrollY() {
            return mMinScrollY;
        }

        public void setmMinScrollY(int minScrollY) {
            this.mMinScrollY = minScrollY;
        }

        public int getmMaxScrollY() {
            return mMaxScrollY;
        }

        public void setMaxScrollY(int maxScrollY) {
            this.mMaxScrollY = maxScrollY;
        }

        public int getLayoutOutGravity() {
            return mLayoutOutGravity;
        }

        public void setLayoutOutGravity(int layoutOutGravity) {
            this.mLayoutOutGravity = layoutOutGravity;
        }

        public boolean isHeaderDrawerLayoutStyle() {
            return isHeaderDrawerLayoutStyle;
        }

        public void setHeaderDrawerLayoutStyle(boolean headerDrawerLayoutStyle) {
            isHeaderDrawerLayoutStyle = headerDrawerLayoutStyle;
        }
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        if (y > mMaxVerticalScrollRange) {
            y = mMaxVerticalScrollRange;
        }
        if (y < mMinVerticalScrollRange) {
            y = mMinVerticalScrollRange;
        }
        if (mTopHeaderIndicator.isLoading()) {
            final int limitScrollY = mInnerBottomView == null ? 0 : mInnerBottomView.getMeasuredHeight();
            if (y > limitScrollY) {
                y = limitScrollY;
            }
        }
        if (mBottomFooterIndicator.isLoading()) {
            final int limitScrollY = 0;
            if (y < limitScrollY) {
                y = limitScrollY;
            }
        }

        int lastScrollx = getScrollX();
        int lastScrolly = getScrollY();
        if (!mTopHeaderIndicator.getCanLoad()) {
            final int limitScrollY = mTopHeaderIndicator.getLimitValue();
            if (lastScrolly >= limitScrollY && y < limitScrollY) {
                y = limitScrollY;
            }
        }
        if (!mBottomFooterIndicator.getCanLoad()) {
            final int limitScrollY = mBottomFooterIndicator.getLimitValue();
            if (lastScrolly <= limitScrollY && y > limitScrollY) {
                y = limitScrollY;
            }
        }
        if (x != lastScrollx || y != lastScrolly) {
            super.scrollTo(x, y);

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
            mTopHeaderIndicator.onScrollChanged(lastScrolly, y);
            mBottomFooterIndicator.onScrollChanged(lastScrolly, y);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int curY = mScroller.getCurrY();
            scrollTo(0, curY);
            ViewCompat.postInvalidateOnAnimation(this);
            if (mScroller.isFinished()) {
                mTopHeaderIndicator.onStopScroll(getScrollY());
                mBottomFooterIndicator.onStopScroll(getScrollY());
            }
        }
    }


    public void preScrollConsumed(int dy, int[] consumed) {
        if (mFrictionFactor > 0.9f) {
            throw new IllegalArgumentException("mFrictionFactor too big");
        }
        if (mFrictionFactor > 0) {
            preScrollConsumedWithFactor(dy, consumed);
        } else {
            preScrollConsumedNoFactor(dy, consumed);
        }
    }

    public void preScrollConsumedNoFactor(int dy, int[] consumed) {
        if (!canPreScroll(dy)) {
            consumed[1] = consumed[0] = 0;
            return;
        }
        final int lastScrollY = getScrollY();
        int scolldy = dy;
        if (dy > 0 && lastScrollY > 0) {
            if (lastScrollY - dy < 0) {
                scolldy = lastScrollY;
            }
        }
        if (dy < 0 && lastScrollY < 0) {
            if (lastScrollY - dy > 0) {
                scolldy = lastScrollY;
            }
        }
        scrollBy(0, (int) -scolldy);
        consumed[1] = lastScrollY - getScrollY();
        if (consumed[1] != 0) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        consumed[3] += lastScrollY -  getScrollY();
//        Logger.e(dy + "contentwraperPreScroll  " + consumed[1]);
    }

    public void preScrollConsumedWithFactor(int dy, int[] consumed) {
        if (!canPreScroll(dy)) {
            consumed[1] = consumed[0] = 0;
            return;
        }
        final int lastScrollY = getScrollY();
        float totalY;
        int finalScrollY = lastScrollY;
        if (lastScrollY < 0) {
            totalY = lastScrollY / (1 - mFrictionFactor);
            if (dy < 0 && (totalY - dy) * (1 - mFrictionFactor) > 0) {
                finalScrollY = 0;
            } else {
                finalScrollY = (int) ((totalY - dy) * (1 - mFrictionFactor));
            }
        } else if (lastScrollY == 0) {
            totalY = 0;
            if (dy > 0) {
                finalScrollY = (int) ((totalY - dy) * (1 - mFrictionFactor));
            } else {
                if (mInnerBottomView != null) {
                    finalScrollY = (int) (totalY - dy);

                } else if (mOutBottomView != null) {
                    finalScrollY = (int) ((totalY - dy) * (1 - mFrictionFactor));
                }
            }
        } else {
            if (mInnerBottomView != null) {
                if (lastScrollY - dy < 0) {
                    finalScrollY = 0;
                } else {
                    if (lastScrollY <= mBottomFooterIndicator.getLimitValue()) {
                        if (dy > 0) {
                            finalScrollY = lastScrollY - dy;
                        } else {
                            if (lastScrollY - dy > mBottomFooterIndicator.getLimitValue()) {
                                float ss = dy - mBottomFooterIndicator.getLimitValue() + lastScrollY;
                                finalScrollY = (int) (mBottomFooterIndicator.getLimitValue() - ss * (1 - mFrictionFactor));
                            } else {
                                finalScrollY = lastScrollY - dy;
                            }
                        }
                    } else {
                        if (dy > 0) {
                            if (lastScrollY - dy * (1 - mFrictionFactor) < mBottomFooterIndicator.getLimitValue()) {
                                float ss = dy - (lastScrollY - mBottomFooterIndicator.getLimitValue()) / (1 - mFrictionFactor);
                                finalScrollY = (int) (mBottomFooterIndicator.getLimitValue() - ss);
                            } else {
                                finalScrollY = (int) (lastScrollY - dy * (1 - mFrictionFactor));
                            }
                        } else {
                            finalScrollY = (int) (lastScrollY - dy * (1 - mFrictionFactor));
                        }
                    }
                }

            } else if (mOutBottomView != null) {
                if (lastScrollY - dy * (1 - mFrictionFactor) < 0) {
                    finalScrollY = 0;
                } else {
                    finalScrollY = (int) (lastScrollY - dy * (1 - mFrictionFactor));
                }
            }

        }
        int scolldy = lastScrollY - finalScrollY;
        scrollBy(0, -scolldy);
        if (mInnerBottomView != null) {

            if (lastScrollY < 0) {
                final int curScrollY = getScrollY();
                if(curScrollY<=0 && curScrollY >= mTopHeaderIndicator.getOverScrollValue()){
                    if (curScrollY != 0 && curScrollY != mTopHeaderIndicator.getOverScrollValue()) {
                        consumed[1] = dy;
                    } else {
                        consumed[1] = (int) ((lastScrollY - curScrollY) / (1 - mFrictionFactor));
                    }
                }else {
                    consumed[1] = dy;
                }

            } else if (lastScrollY > 0) {
                if (getScrollY() == 0) {
                    consumed[1] = (lastScrollY - getScrollY());
                } else if (lastScrollY < mBottomFooterIndicator.getOverScrollValue() && getScrollY() == mBottomFooterIndicator.getOverScrollValue()) {
                    consumed[1] = (int) ((lastScrollY - getScrollY()) / (1 - mFrictionFactor));
                } else {
                    consumed[1] = dy;
                }
            } else {
                if (getScrollY() > 0) {
                    consumed[1] = dy;
                } else if (getScrollY() < 0) {
                    consumed[1] = dy;
                }
            }
        } else {
            final int curScrollY = getScrollY();
            if (lastScrollY != curScrollY) {

                if ((curScrollY == 0 || curScrollY == mTopHeaderIndicator.getOverScrollValue()
                        || curScrollY == mBottomFooterIndicator.getOverScrollValue())) {

                    consumed[1] = (int) ((lastScrollY - getScrollY()) / (1 - mFrictionFactor));
                } else {
                    consumed[1] = dy;
                }
            }
        }
//        consumed[1] = dy;
        if (consumed[1] != 0) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        consumed[3] += lastScrollY - getScrollY();
//        Logger.e(dy + "contentwraperPreScroll  " + consumed[1]);
    }

    public void preScrollUp(int dy, int[] consumed) {
        final int lastScrolly = getScrollY();
        if ((dy < 0 && lastScrolly < 0)||(dy > 0 && lastScrolly > 0) ) {
            preScrollConsumed(dy, consumed);
        }
    }

    public boolean canFling() {
        return getScrollY() >= mMinVerticalScrollRange && getScrollY() <= mMaxVerticalScrollRange && getScrollY() != 0;
    }

    private void headerFling(int velocityY) {
        LayoutParams lp = (ContentWraperView.LayoutParams) mOutTopView.getLayoutParams();
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
            if (getScrollY() <= lp.mStableScrollY) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollY, lp.mStableScrollY)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            } else {
                if (velocityY != 0) {
                    if (velocityY > 0) {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, lp.mStableScrollY, getScrollY());
                    } else {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, getScrollY(), 0);
                    }
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }

        } else if (mTopHeaderIndicator.isPrepare()) {
            if (getScrollY() <= mTopHeaderIndicator.getTriggerValue()) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollY, lp.mStableScrollY)) {
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

    private void headerDrawerFling(int velocityY) {
        LayoutParams lp = (ContentWraperView.LayoutParams) mOutTopView.getLayoutParams();
        final int curScrollY = getScrollY();
        int targeScrollY = mTopHeaderIndicator.getStableValue() / 2 < curScrollY ? mTopHeaderIndicator.getStableValue() : 0;

        //curScrollY 'value  is < 0
        if (velocityY != 0 && curScrollY > mTopHeaderIndicator.getStableValue()) {
            if (velocityY > 0) {
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, mTopHeaderIndicator.getStableValue(), mTopHeaderIndicator.getStableValue());
            } else {
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, 0);
            }
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }
        if (lp.mTrigeerExpandRatio >= 0.2f && lp.mTrigeerExpandRatio <= 0.8f) {
            if (curScrollY <= mTopHeaderIndicator.getTriggerValue()) {
                targeScrollY = mTopHeaderIndicator.getStableValue();
            } else {
                targeScrollY = 0;
            }
        }
        if (mScroller.springBack(0, curScrollY, 0, 0, targeScrollY, targeScrollY)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void dispatchFling(int velocityY, boolean isDownSlide) {
        if (getScrollY() < 0) {
            LayoutParams lp = (ContentWraperView.LayoutParams) mOutTopView.getLayoutParams();
            if (lp.isHeaderDrawerLayoutStyle) {
                headerDrawerFling(velocityY);
            } else {
                headerFling(velocityY);
                mBottomFooterIndicator.onStopScroll(getScrollY());
            }

        } else {
            mTopHeaderIndicator.onStopScroll(getScrollY());
            if (mInnerBottomView != null && getScrollY() < mInnerBottomView.getMeasuredHeight()) {
                mBottomFooterIndicator.onStopScroll(getScrollY());
                if (velocityY != 0) {
                    fling(velocityY);
                } else {
                    if (isSnap) {
                        int currentY = getScrollY();
                        // 下拉
                        if (isDownSlide) {
                            if (currentY < mInnerBottomView.getMeasuredHeight()) {
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
            } else {
                footerFling(velocityY);
            }
        }
    }

    private void footerFling(int velocityY) {
        LayoutParams lp = (ContentWraperView.LayoutParams) mOutBottomView.getLayoutParams();
        if (getScrollY() >= mBottomFooterIndicator.getTriggerValue()) {
            if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollY, lp.mStableScrollY)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else if (getScrollY() < lp.mStableScrollY) {
            if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mMinScrollY, lp.mMinScrollY)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
        if (mBottomFooterIndicator.isComplete()) {
            if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mMinScrollY, lp.mMinScrollY)) {
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mBottomFooterIndicator.onStopScroll(getScrollY());
            }
        } else if (mBottomFooterIndicator.isLoading()) {
            if (getScrollY() >= lp.mStableScrollY) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollY, lp.mStableScrollY)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            } else {
                if (velocityY != 0) {
                    if (velocityY > 0) {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, lp.mMinScrollY, getScrollY());
                    } else {
                        mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, getScrollY(), lp.mStableScrollY);
                    }
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }

        } else if (mBottomFooterIndicator.isPrepare()) {
            if (getScrollY() >= mBottomFooterIndicator.getTriggerValue()) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mStableScrollY, lp.mStableScrollY)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                    mBottomFooterIndicator.dispatchReleaseBeforeRefresh();
                } else {
                    mBottomFooterIndicator.dispatchReleaseBeforeRefresh();
                    mBottomFooterIndicator.onStopScroll(getScrollY());
                }
            } else {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, lp.mMinScrollY, lp.mMinScrollY)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    mBottomFooterIndicator.onStopScroll(getScrollY());
                }
            }
        }
    }

    boolean isSnap;

    public void fling(int velocityY) {
        if (mInnerBottomView == null) {
            return;
        }
        if (getScrollY() <= mInnerBottomView.getMeasuredHeight() && getScrollY() >= 0) {
            int range = mInnerBottomView.getMeasuredHeight();
            if (velocityY > 0) {
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, isSnap ? 0 : 0, isSnap ? 0 : getScrollY());
            } else {
                mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, isSnap ? range : 0, range);

            }
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public boolean canPreScroll(int dy) {
        if (dy > 0 && reachChildTop()) {
            return true;
        } else if (dy < 0) {
            if (getScrollY() < 0) {
                return true;
            }
            if (reachChildBottom()) {
                return true;
            }

        } else if (dy > 0 && getScrollY() > 0) {
            return true;
        }
        return false;
    }

    View mScrollChild;

    public boolean reachChildTop() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChild, -1);
    }

    public boolean reachChildBottom() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChild, 1);
    }

    public void setScrollChild(View child) {
        this.mScrollChild = child;
    }


    public void setCanTopHeaderLoad(boolean canRefresh) {
        mTopHeaderIndicator.setCanLoad(canRefresh);
    }

    public void setCanBottomFooterLoad(boolean canLoadMore) {
        mBottomFooterIndicator.setCanLoad(canLoadMore);
    }

    public void setLoadComplete() {

        if (mOutTopView != null && ViewCompat.isLaidOut(mOutTopView) && mTopHeaderIndicator.isLoading()) {
            mTopHeaderIndicator.setComplete();
            final int scrollY = getScrollY();
            if (scrollY < 0) {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mScroller.startScroll(getScrollX(), scrollY, 0, -scrollY, 350);
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mTopHeaderIndicator.onStopScroll(scrollY);
            }
            return;
        }
        if (mOutBottomView != null && ViewCompat.isLaidOut(mOutBottomView) && mBottomFooterIndicator.isLoading()) {
            mBottomFooterIndicator.setComplete();
            final int scrollY = getScrollY();
            if (scrollY > mBottomFooterIndicator.getLimitValue()) {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mScroller.startScroll(getScrollX(), scrollY, 0, mBottomFooterIndicator.getLimitValue() - scrollY, 350);
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mBottomFooterIndicator.onStopScroll(scrollY);
            }
            return;
        }

    }

    public TopHeaderIndicator getTopHeaderIndicator() {
        return mTopHeaderIndicator;
    }

    public BottomFooterIndicator getBottomFooterIndicator() {
        return mBottomFooterIndicator;
    }

    public void setTopHeaderOnStartLoadCallback(BaseRefreshIndicator.OnStartLoadCallback callback) {
        mTopHeaderIndicator.setOnStartLoadCallback(callback);
    }

    public void setBottomFooterOnStartLoadCallback(BaseRefreshIndicator.OnStartLoadCallback callback) {
        mBottomFooterIndicator.setOnStartLoadCallback(callback);
    }

    public void setTopHeaderLoadComplete() {
        setLoadComplete();
    }

    public void setBottomFooterLoadComplete() {
        setLoadComplete();
    }
}
