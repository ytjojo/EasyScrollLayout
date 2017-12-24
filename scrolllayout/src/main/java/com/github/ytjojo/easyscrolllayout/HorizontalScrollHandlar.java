package com.github.ytjojo.easyscrolllayout;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.OverScroller;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2017/11/29 0029.
 */

public class HorizontalScrollHandlar {

    View mOutLeftView;
    View mOutRightView;
    View mInnerTopView;
    View mOutTopView;
    View mDirectContentView;
    int mMaxHorizontalScrollRange;
    int mMinHorizontalScrollRange;
    int mMinHorizontalStableScrollRange;
    int mMaxHorizontalStableScrollRange;
    int mScrollX;
    LeftRefreshIndicator mLeftRefreshInidicator;
    EasyScrollLayout mParentView;
    public HorizontalScrollHandlar(EasyScrollLayout parent){
        mLeftRefreshInidicator = new LeftRefreshIndicator();
        mRightRefreshIndicator = new RightRefreshIndicator();
        this.mParentView = parent;
    }

    boolean isDrawLayoutStyle = true;

    public void setViews(View contentView, View outLeftView, View outRightView) {
        this.mOutLeftView = outLeftView;
        this.mOutRightView = outRightView;
        this.mDirectContentView = contentView;
        if (mOutLeftView != null) {
            mOutLeftView.setClickable(true);
            mMaxHorizontalStableScrollRange = mOutLeftView.getMeasuredWidth();
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutLeftView.getLayoutParams();
            if (lp.mOverScrollRatio > 0) {
                mMaxHorizontalScrollRange = (int) (mMaxHorizontalStableScrollRange * (1f + lp.mOverScrollRatio));

            } else {
                mMaxHorizontalScrollRange = mMaxHorizontalStableScrollRange;
            }
            mLeftParallaxMult = lp.mParallaxMultiplier;
        } else {
            mMaxHorizontalScrollRange = mMaxHorizontalStableScrollRange = 0;
        }
        if (mOutRightView != null) {
            mOutRightView.setClickable(true);
            mMinHorizontalStableScrollRange = -mOutRightView.getMeasuredWidth();
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutRightView.getLayoutParams();
            if (lp.mOverScrollRatio > 0) {
                mMinHorizontalScrollRange = (int) (mMinHorizontalStableScrollRange * (1f + lp.mOverScrollRatio));

            } else {
                mMinHorizontalScrollRange = mMinHorizontalStableScrollRange;
            }
            mRightParallaxMult = lp.mParallaxMultiplier;
        } else {
            mMinHorizontalScrollRange = mMinHorizontalStableScrollRange = 0;
        }
        if (mDirectContentView != null) {
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mDirectContentView.getLayoutParams();
            mContentParallaxMult = lp.mParallaxMultiplier;
        }
        mLeftRefreshInidicator.setView(mOutLeftView);
        mRightRefreshIndicator.setView(mOutRightView);
    }

    boolean isOutLeftViewTopOfContent;
    boolean isOutRightViewTopOfContent;

    public void onLayout() {
        offsetLeftAndRight(0);
        isOutLeftViewTopOfContent = false;
        isOutRightViewTopOfContent = false;
        if(mOutLeftView != null && mDirectContentView !=null){
            isOutLeftViewTopOfContent = mParentView.indexOfChild(mOutLeftView)> mParentView.indexOfChild(mDirectContentView);
        }
        if(mOutRightView != null && mDirectContentView !=null){
            isOutRightViewTopOfContent = mParentView.indexOfChild(mOutRightView)> mParentView.indexOfChild(mDirectContentView);
        }
    }

    public void setTopViews(View innerTopView, View outTopView) {
        this.mInnerTopView = innerTopView;
        this.mOutTopView = outTopView;
    }

    public boolean isHorizontallyScrolled() {
        return mScrollX != 0;
    }

    public void scrollConsumed(int dx, int[] consumed) {
        consumed[0] = consumed[1] = 0;
        if (!canPreScroll(dx)) {
            return;
        }
        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int startX = mScrollX;
        offsetLeftAndRight(dx);
        consumed[0] = mScrollX - startX;
    }

    float mLeftParallaxMult = 0.5f;
    float mRightParallaxMult = 0f;
    float mContentParallaxMult = 0.5f;

    public void offsetLeftAndRight(int dx) {
        int targetScrollX = mScrollX;
        final int mLastScrollX = mScrollX;
        targetScrollX += dx;
        if (targetScrollX > mMaxHorizontalScrollRange) {
            targetScrollX = mMaxHorizontalScrollRange;
        }
        if (targetScrollX < mMinHorizontalScrollRange) {
            targetScrollX = mMinHorizontalScrollRange;
        }
        if (mScrollX < 0 && dx > 0 && targetScrollX > 0) {
            targetScrollX = 0;
        } else if (mScrollX > 0 && dx < 0 && targetScrollX < 0) {
            targetScrollX = 0;
        }
        if(!isDrawLayoutStyle){
            if(mLeftRefreshInidicator.isLoading()){
                if(targetScrollX <0){
                    targetScrollX = 0;
                }
            }
            if(mRightRefreshIndicator.isLoading()){
                if(targetScrollX > 0){
                    targetScrollX = 0;
                }
            }
            if (!mRightRefreshIndicator.getCanLoad()) {
                if (mLastScrollX >= 0 && targetScrollX < 0) {
                    targetScrollX = 0;
                }
            }
            if (!mLeftRefreshInidicator.getCanLoad()) {
                if (mLastScrollX <= 0 && targetScrollX > 0) {
                    targetScrollX = 0;
                }
            }
        }else {
            if (!mIsRightEnable) {
                if (mLastScrollX >= 0 && targetScrollX < 0) {
                    targetScrollX = 0;
                }
            }
            if (!mIsLeftEnable) {
                if (mLastScrollX <= 0 && targetScrollX > 0) {
                    targetScrollX = 0;
                }
            }
        }

        int offsetDx = targetScrollX - mScrollX;


        mScrollX = targetScrollX;

        offsetContenViews();
        offsetLeftView();
        offsetRightView();
        if(!isDrawLayoutStyle){
            mLeftRefreshInidicator.onScrollChanged(mLastScrollX,mScrollX);
            mRightRefreshIndicator.onScrollChanged(mLastScrollX,mScrollX);
        }

    }
    private void offsetContenViews(){
        if (mContentParallaxMult != 0) {
            int contentLeft = (int) (mScrollX * (1 - mContentParallaxMult));
            ViewCompat.offsetLeftAndRight(mDirectContentView, contentLeft - mDirectContentView.getLeft());
        } else {

            ViewCompat.offsetLeftAndRight(mDirectContentView, mScrollX - mDirectContentView.getLeft());
        }
        if (mInnerTopView != null) {
            if (mContentParallaxMult != 0) {
                int offset = (int) (mScrollX * (1 - mContentParallaxMult));
                ViewCompat.offsetLeftAndRight(mInnerTopView, offset - mInnerTopView.getLeft());
            } else {
                ViewCompat.offsetLeftAndRight(mInnerTopView, mScrollX - mInnerTopView.getLeft());
            }
        }

        if (mOutTopView != null) {
            int offset = (mScrollX - mOutTopView.getLeft());
            ViewCompat.offsetLeftAndRight(mOutTopView, offset);
        }
    }
    private void offsetLeftView(){
        if (mOutLeftView != null) {
            if (mLeftParallaxMult != 0) {
                if (mScrollX >= 0 && mScrollX < mMaxHorizontalStableScrollRange) {
                    float offsetRatio = ((float) mScrollX) / mMaxHorizontalStableScrollRange;
                    int totalOffset = (int) (mMaxHorizontalStableScrollRange * mLeftParallaxMult);
                    float horizontalOffset = mMaxHorizontalStableScrollRange - totalOffset + totalOffset * offsetRatio;
                    int offset = (int) (horizontalOffset - mOutLeftView.getRight());
                    ViewCompat.offsetLeftAndRight(mOutLeftView, offset);
                } else {
                    int offset = (mScrollX - mOutLeftView.getRight());
                    ViewCompat.offsetLeftAndRight(mOutLeftView, offset);
                }
            } else {
                int offset = (mScrollX - mOutLeftView.getRight());
                ViewCompat.offsetLeftAndRight(mOutLeftView, offset);
            }

        }
    }
    private void offsetRightView(){
        if (mOutRightView != null) {
            if (mRightParallaxMult != 0) {
                if (mScrollX <= 0 && mScrollX > mMinHorizontalStableScrollRange) {
                    float offsetRatio = ((float) mScrollX) / mMinHorizontalStableScrollRange;
                    int totalOffset = (int) (mMinHorizontalStableScrollRange * mRightParallaxMult);
                    float horizontalOffset = mMinHorizontalStableScrollRange - totalOffset + totalOffset * offsetRatio;

//                    Logger.e(" mOutRightView " + (mOutRightView.getLeft() - mContentView.getMeasuredWidth()));
                    int offset = (int) (horizontalOffset - mOutRightView.getLeft() + mDirectContentView.getMeasuredWidth());
                    ViewCompat.offsetLeftAndRight(mOutRightView, offset);
                } else if (mScrollX < 0) {
                    int offset = (mScrollX - mOutRightView.getLeft() + mDirectContentView.getMeasuredWidth());
                    ViewCompat.offsetLeftAndRight(mOutRightView, offset);
                } else {
                    int totalOffset = (int) (mMinHorizontalStableScrollRange * mRightParallaxMult);
                    totalOffset += mScrollX;
                    final int offset = totalOffset - mOutRightView.getLeft() + mDirectContentView.getMeasuredWidth();
                    ViewCompat.offsetLeftAndRight(mOutRightView, offset);

                }
            } else {
                int offset = (mScrollX - mOutRightView.getLeft() + mDirectContentView.getMeasuredWidth());
                ViewCompat.offsetLeftAndRight(mOutRightView, offset);
            }
        }
    }
    private boolean mIsLeftEnable = true;
    private boolean mIsRightEnable = true;
    public void setLeftEnable(boolean enable){
        mIsLeftEnable = enable;
    }
    public void setRightEnable(boolean enable){
        mIsRightEnable = enable;
    }
    private boolean canPreScroll(int dx) {
        if (isHorizontallyScrolled()) {
            return true;
        }
        if (dx > 0 && reachChildLeft()) {
            return true;
        } else if (dx < 0 && reachChildRight()) {
            return true;
        }
        return false;

    }

    View mScrollChild;
    boolean isAutomaticHunting = true;

    boolean mIsDownInOuterViews;

    public boolean shouldOffsetEvent() {
        return mIsDownInOuterViews;
    }

    public boolean reachChildLeft() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollHorizontally(mScrollChild, -1);
    }

    public boolean reachChildRight() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollHorizontally(mScrollChild, 1);
    }

    final protected boolean isHorizontalScrollView(View child) {
        if (isHorizontalScrollChild(child)) {
            return true;
        }
        if (child instanceof HorizontalScrollView || child instanceof ScrollView || child instanceof ViewPager || child instanceof RecyclerView) {
            if (child instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) child;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager != null && manager instanceof RecyclerView.LayoutManager) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                    if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                        return true;
                    }
                }
                return false;
            }

            return true;
        }

        return false;
    }

    final protected boolean isVerticalScrollView(View child) {
        if (isVerticalScrollChild(child)) {
            return true;
        }
        if (child instanceof android.support.v4.view.NestedScrollingChild || child instanceof AbsListView || child instanceof ScrollView || child instanceof ViewPager || child instanceof RecyclerView) {
            if (child instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) child;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof RecyclerView.LayoutManager) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                    if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean isVerticalScrollChild(View child) {
        return false;
    }

    public boolean isHorizontalScrollChild(View child) {
        return false;
    }

    public View getCurrentScrollChild() {
        return mScrollChild;
    }

    public void setScrollChild(View scrollChild) {
        this.mScrollChild = scrollChild;
        isAutomaticHunting = false;

    }

    boolean isScrollChanged;

    protected void resetChildScrollChange() {
        isScrollChanged = false;
    }

    public void notifyChildScrollChaged() {
        isScrollChanged = true;
    }

    public boolean isScrollChanged() {
        return isScrollChanged;
    }

    public boolean isAutomaticHunting() {
        return isAutomaticHunting;
    }


    protected View huntingScrollChild(View parent, Point point) {
        if (isHorizontalScrollView(parent)) {
            if (pointInView(parent, point.x, point.y, 0)) {
                mScrollChild = parent;
            }
            return mScrollChild;
        } else if (isVerticalScrollView(parent)) {
            return findScrollChildInVerticalView(point, (ViewGroup) parent);
        } else {
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                View hitView = findChildViewUnder(viewGroup, point.x, point.y);
                if (hitView == null) {
                    return null;
                }
                if (isHorizontalScrollView(hitView)) {
                    mScrollChild = hitView;
                    return mScrollChild;
                } else if (isVerticalScrollView(hitView)) {
                    offsetPoint(mPoint, viewGroup, hitView);
                    return findScrollChildInVerticalView(point, (ViewGroup) hitView);
                }
            }

        }
        return null;
    }

    public View isDirectChildCanScroll(Point point, ViewGroup parent, ViewGroup child) {
        offsetPoint(point, parent, child);
        View hitView = findChildViewUnder(child, point.x, point.y);
        if (hitView == null) {
            return null;
        }
        if (isHorizontalScrollView(child)) {
            mScrollChild = hitView;
            return mScrollChild;
        } else {
            offsetPoint(point, child, hitView);
            hitView = findChildViewUnder(child, point.x, point.y);
            if (isHorizontalScrollView(hitView)) {
                mScrollChild = hitView;
                return mScrollChild;
            }
        }
        return null;

    }

    public View findScrollChildInVerticalView(Point point, ViewGroup verticalScrollView) {
        View child = verticalScrollView;
        if (verticalScrollView instanceof ScrollView || verticalScrollView instanceof NestedScrollView) {
            if (verticalScrollView.getChildCount() > 0 && verticalScrollView.getChildAt(0) instanceof ViewGroup) {
                child = verticalScrollView.getChildAt(0);
                offsetPoint(point, verticalScrollView, child);
            }
        }
        View hitView = findChildViewUnder((ViewGroup) child, point.x, point.y);
        if (hitView == null) {
            return null;
        }
        if (isHorizontalScrollView(hitView)) {
            mScrollChild = hitView;
            return mScrollChild;
        } else if (hitView instanceof ViewGroup) {
            return isDirectChildCanScroll(point, (ViewGroup) child, (ViewGroup) hitView);
        }
        return null;
    }

    Point mPoint = new Point();

    public boolean onDownEvent(int x, int y, EasyScrollLayout parent) {
        mIsDownInOuterViews = false;
        mScrollChild = null;
        if (mDirectContentView != null && (mOutRightView != null || mOutLeftView != null)) {
            if (mOutLeftView != null) {
                if (isOutLeftViewTopOfContent) {
                    mPoint.set(x, y);
                    offsetPoint(mPoint, parent, mOutLeftView);
                    mIsDownInOuterViews |= pointInView(mOutLeftView, mPoint.x, mPoint.y, 0);
                    if (mIsDownInOuterViews) {
                        return true;
                    }

                }
            }
            if (mOutRightView != null) {
                if (isOutRightViewTopOfContent) {
                    mPoint.set(x, y);
                    offsetPoint(mPoint, parent, mOutRightView);
                    mIsDownInOuterViews |= pointInView(mOutRightView, mPoint.x, mPoint.y, 0);
                    if (mIsDownInOuterViews) {
                        return true;
                    }

                }
            }
        }

        if (mDirectContentView != null) {
            mPoint.set(x, y);
            offsetPoint(mPoint, parent, mDirectContentView);
            boolean isPointInView = pointInView(mDirectContentView, mPoint.x, mPoint.y, 0);
            if (mDirectContentView instanceof ViewPager) {
                if (isPointInView) {
                    mScrollChild = mDirectContentView;
                    return mIsDownInOuterViews;
                }
            }
            if (isPointInView && mDirectContentView instanceof ContentWraperView) {
                final ContentWraperView contentWraperView = (ContentWraperView) mDirectContentView;
                offsetPoint(mPoint, (ViewGroup) mDirectContentView, ((ContentWraperView) mDirectContentView).mContentView);
                if (!pointInView(contentWraperView.mContentView, mPoint.x, mPoint.y, 0)) {
                    return mIsDownInOuterViews;
                }
                huntingScrollChild(contentWraperView.mContentView, mPoint);
                return mIsDownInOuterViews;
            }
        }
        if (mInnerTopView != null) {
            mPoint.set(x, y);
            offsetPoint(mPoint, parent, mInnerTopView);
            boolean isPointInView = pointInView(mInnerTopView, mPoint.x, mPoint.y, 0);
            if (isPointInView) {
                huntingScrollChild(mInnerTopView, mPoint);
                return mIsDownInOuterViews;
            }
        }
        if (mOutTopView != null) {
            mPoint.set(x, y);
            offsetPoint(mPoint, parent, mOutTopView);
            boolean isPointInView = pointInView(mOutTopView, mPoint.x, mPoint.y, 0);
            if (isPointInView) {
                return mIsDownInOuterViews;
            }
        }
        return mIsDownInOuterViews = true;
    }

    public void offsetPoint(Point point, ViewGroup parent, View child) {
        final int offsetX = (int) (parent.getScrollX() - child.getX());
        final int offsetY = (int) (parent.getScrollY() - child.getY());
        point.offset(offsetX, offsetY);
    }

    public View findChildViewUnder(ViewGroup viewGroup, int x, int y) {
        if (viewGroup != null) {
            final int count = viewGroup.getChildCount();
            x += viewGroup.getScrollX();
            y += viewGroup.getScrollY();
            for (int i = count - 1; i >= 0; i--) {
                final View child = viewGroup.getChildAt(i);
                final float translationX = ViewCompat.getTranslationX(child);
                final float translationY = ViewCompat.getTranslationY(child);
                if (x >= child.getLeft() + translationX &&
                        x <= child.getRight() + translationX &&
                        y >= child.getTop() + translationY &&
                        y <= child.getBottom() + translationY) {
                    return child;
                }
            }
        }
        return null;
    }

    public boolean canScrollHorizontally(int direction) {
        final int offset = mScrollX;
        if (direction > 0) {
            return offset < mMaxHorizontalScrollRange;
        } else {
            return offset > mMinHorizontalScrollRange;
        }
    }

    public boolean pointInView(View child, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((child.getRight() - child.getLeft()) + slop) &&
                localY < ((child.getBottom() - child.getTop()) + slop);
    }

    public static boolean screenPointInView(View view, int rawX, int rawY, Rect rect, int[] mLocation) {
        if (view == null) {
            return false;
        }
        view.getLocationOnScreen(mLocation);
        rect.set(mLocation[0], mLocation[1], mLocation[0] + view.getMeasuredWidth(), mLocation[1] + view.getMeasuredHeight());
        if (rect.contains(rawX, rawY)) {
            return true;
        }
        return false;
    }


    public static boolean isTouchPointInView(View view, int rawX, int rawY) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        Rect rect = new Rect();
        return screenPointInView(view, rawX, rawY, rect, location);
    }

    public boolean canFling() {
        return isHorizontallyScrolled();
    }

    OverScroller mScroller;

    public void dispatchFling(int velocityX) {
        if (mScroller == null) {
            mScroller = new OverScroller(mParentView.getContext());
        }
        if (mFlingRunnable != null) {
            mParentView.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if(isDrawLayoutStyle){
            drawlayoutFling(velocityX);
        }else {
            leftFling(velocityX);
            rightFling(velocityX);
        }

    }
    private void drawlayoutFling(int velocityX){
        if (mScrollX == 0) {
            return;
        }
        if (mScrollX < 0) {
            int targetX = (mMinHorizontalStableScrollRange) / 2 < mScrollX ? 0 : mMinHorizontalStableScrollRange;
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutRightView.getLayoutParams();
            if (velocityX != 0 && mScrollX > mMinHorizontalStableScrollRange && mScrollX < lp.mTrigeerExpandRatio * mMinHorizontalStableScrollRange) {
                fiing(velocityX,true);
                return;
            }
            if (lp.mTrigeerExpandRatio >= 0 && lp.mTrigeerExpandRatio <= 1f + lp.mOverScrollRatio) {
                if (mScrollX <= lp.mTrigeerExpandRatio * mMinHorizontalStableScrollRange) {
                    targetX = mMinHorizontalStableScrollRange;
                } else {
                    targetX = 0;
                }
            }
            if (mScroller.springBack(mScrollX, 0, targetX, targetX, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
            }
        } else {
            int targetX = (mMaxHorizontalStableScrollRange) / 2 < mScrollX ? mMaxHorizontalStableScrollRange : 0;
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutLeftView.getLayoutParams();
            if (velocityX != 0 && mScrollX < mMaxHorizontalStableScrollRange && mScrollX > lp.mTrigeerExpandRatio * mMaxHorizontalStableScrollRange) {
                fiing(velocityX,true);
                return;
            }
            if (lp.mTrigeerExpandRatio >= 0 && lp.mTrigeerExpandRatio <= 1f + lp.mOverScrollRatio) {
                if (mScrollX >= lp.mTrigeerExpandRatio * mMaxHorizontalStableScrollRange) {
                    targetX = mMaxHorizontalStableScrollRange;
                } else {
                    targetX = 0;
                }
            }
            if (mScroller.springBack(mScrollX, 0, targetX, targetX, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
            }
        }
    }
    RightRefreshIndicator mRightRefreshIndicator;
    private void rightFling(int velocityX){
        EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutRightView.getLayoutParams();
        if (mRightRefreshIndicator.isComplete()) {
            if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);

            } else {
                mRightRefreshIndicator.onStopScroll(mScrollX);
            }
        }else if(mRightRefreshIndicator.isLoading()){
            if (mScrollX < mMinHorizontalStableScrollRange) {
                if (mScroller.springBack(mScrollX, 0, mMinHorizontalStableScrollRange,mMinHorizontalStableScrollRange, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                }
            }else{
                if (velocityX != 0) {
                    fiing(velocityX,false);
                }
            }

        } else if (mRightRefreshIndicator.isPrepare()) {
            if (mScrollX <= -mOutRightView.getMeasuredWidth() * lp.mTrigeerExpandRatio) {
                if (mScroller.springBack(mScrollX,0, mMinHorizontalStableScrollRange,mMinHorizontalStableScrollRange, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                    mRightRefreshIndicator.dispatchReleaseBeforeRefresh();
                } else {
                    mRightRefreshIndicator.dispatchReleaseBeforeRefresh();
                    mRightRefreshIndicator.onStopScroll(mScrollX);
                }
            } else {
                if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                } else {
                    mRightRefreshIndicator.onStopScroll(mScrollX);
                }
            }
        }
    }

    private void leftFling(int velocityX){
        EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutLeftView.getLayoutParams();
        if (mLeftRefreshInidicator.isComplete()) {
            if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);

            } else {
                mLeftRefreshInidicator.onStopScroll(mScrollX);
            }
        }else if(mLeftRefreshInidicator.isLoading()){
            if (mScrollX > mMaxHorizontalStableScrollRange) {
                if (mScroller.springBack(mScrollX, 0, mMaxHorizontalStableScrollRange,mMaxHorizontalStableScrollRange, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                }
            }else{
                if (velocityX != 0) {
                    fiing(velocityX,false);
                }
            }

        } else if (mLeftRefreshInidicator.isPrepare()) {
            if (mScrollX >= mOutRightView.getMeasuredWidth() * lp.mTrigeerExpandRatio) {
                if (mScroller.springBack(mScrollX,0, mMaxHorizontalStableScrollRange,mMaxHorizontalStableScrollRange, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                    mLeftRefreshInidicator.dispatchReleaseBeforeRefresh();
                } else {
                    mLeftRefreshInidicator.dispatchReleaseBeforeRefresh();
                    mLeftRefreshInidicator.onStopScroll(mScrollX);
                }
            } else {
                if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mParentView);
                    ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
                } else {
                    mLeftRefreshInidicator.onStopScroll(mScrollX);
                }
            }
        }
    }

    Runnable mFlingRunnable;

    private void fiing(int velocityX,boolean isSnap) {
        if (mScrollX > 0) {
            if (velocityX > 0) {
                mScroller.fling(mScrollX, 0, velocityX, 0, isSnap?mMaxHorizontalStableScrollRange:mScrollX, mMaxHorizontalStableScrollRange, 0, 0);
            } else {
                mScroller.fling(mScrollX, 0, velocityX, 0, 0, isSnap?0:mScrollX, 0, 0);
            }
        } else {
            if (velocityX > 0) {
                mScroller.fling(mScrollX, 0, velocityX, 0, isSnap?0:mScrollX, 0, 0, 0);
            } else {
                mScroller.fling(mScrollX, 0, velocityX, 0, mMinHorizontalStableScrollRange, isSnap?mMinHorizontalStableScrollRange:mScrollX, 0, 0);
            }
        }

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable(mParentView);
            ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
        } else {
            onFlingFinished();
        }
    }

    private class FlingRunnable implements Runnable {
        private final View mLayout;

        FlingRunnable(View layout) {
            mLayout = layout;
        }

        @Override
        public void run() {
            if (mLayout != null && mScroller != null) {
                if (mScroller.computeScrollOffset()) {
                    int curX = mScroller.getCurrX();
                    offsetLeftAndRight(curX - mScrollX);
                    // Post ourselves so that we run on the next animation
                    ViewCompat.postOnAnimation(mLayout, this);
                } else {
                    onFlingFinished();
                }
            }
        }
    }

    private void onFlingFinished() {
        if (!isDrawLayoutStyle) {
            mLeftRefreshInidicator.onStopScroll(mScrollX);
            mRightRefreshIndicator.onStopScroll(mScrollX);
        }

    }
    private void setLeftComplete(){
        if (mLeftRefreshInidicator.isLoading()) {
            if (mFlingRunnable != null) {
                mParentView.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }

            mLeftRefreshInidicator.setComplete();
            if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
            }else {
                mLeftRefreshInidicator.onStopScroll(mScrollX);
            }
        }
    }
    private void setRightComplete(){
        if (mRightRefreshIndicator.isLoading()) {
            if (mFlingRunnable != null) {
                mParentView.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }

            mRightRefreshIndicator.setComplete();
            if (mScroller.springBack(mScrollX, 0, 0, 0, 0, 0)) {
                mFlingRunnable = new FlingRunnable(mParentView);
                ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
            }else {
                mRightRefreshIndicator.onStopScroll(mScrollX);
            }
        }
    }
    private void closeLeft(){
        if(mScrollX >0 ){
            if (mFlingRunnable != null) {
                mParentView.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mScroller.startScroll(mScrollX,0,-mScrollX,0,250);
            mFlingRunnable = new FlingRunnable(mParentView);
            ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
        }
    }
    private void closeRight(){
        if(mScrollX < 0 ){
            if (mFlingRunnable != null) {
                mParentView.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mScroller.startScroll(mScrollX,0,-mScrollX,0,250);
            mFlingRunnable = new FlingRunnable(mParentView);
            ViewCompat.postOnAnimation(mParentView, mFlingRunnable);
        }
    }

    public int getScrollX() {
        return mScrollX;
    }

    public boolean isOutLeftViewTopOfContent() {
        return isOutLeftViewTopOfContent;
    }

    public boolean isOutRightViewTopOfContent() {
        return isOutRightViewTopOfContent;
    }
}
