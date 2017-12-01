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

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by Administrator on 2017/11/29 0029.
 */

public class HorizontalScrollHandlar {

    EasyScrollLayout mEasyScrollLayout;
    View mOutLeftView;
    View mOutRightView;
    View mInnerTopView;
    View mOutTopView;
    ContentWraperView mContentView;
    int mMaxHorizontalScrollRange;
    int mMinHorizontalScrollRange;
    int mMinHorizontalStableScrollRange;
    int mMaxHorizontalStableScrollRange;
    int mScrollX;

    public void setViews(ContentWraperView contentWraperView, View outLeftView, View outRightView) {
        this.mOutLeftView = outLeftView;
        this.mOutRightView = outRightView;
        this.mContentView = contentWraperView;
        if (mOutLeftView != null) {
            mMaxHorizontalStableScrollRange = mOutLeftView.getMeasuredWidth();
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutLeftView.getLayoutParams();
            if (lp.mOverScrollRatio > 0) {
                mMaxHorizontalScrollRange = (int) (mMaxHorizontalStableScrollRange * (1f + lp.mOverScrollRatio));

            } else {
                mMaxHorizontalScrollRange = mMaxHorizontalStableScrollRange;
            }
        } else {
            mMaxHorizontalScrollRange = mMaxHorizontalStableScrollRange = 0;
        }
        if (mOutRightView != null) {
            mMinHorizontalStableScrollRange = mOutRightView.getMeasuredWidth();
            EasyScrollLayout.LayoutParams lp = (EasyScrollLayout.LayoutParams) mOutRightView.getLayoutParams();
            if (lp.mOverScrollRatio > 0) {
                mMinHorizontalScrollRange = (int) (mMinHorizontalStableScrollRange * (1f + lp.mOverScrollRatio));

            } else {
                mMinHorizontalScrollRange = mMinHorizontalStableScrollRange;
            }
        } else {
            mMinHorizontalScrollRange = mMinHorizontalStableScrollRange = 0;
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
        int startX = mScrollX;
        offsetLeftAndRight(dx);
        consumed[0] = mScrollX - startX;
    }
    public void offsetLeftAndRight(int dx){
        int mTargetScrollX = mScrollX;
        mTargetScrollX += dx;
        if (mTargetScrollX > mMaxHorizontalScrollRange) {
            mTargetScrollX = mMaxHorizontalScrollRange;
        }
        if (mTargetScrollX < mMinHorizontalScrollRange) {
            mTargetScrollX = mMinHorizontalScrollRange;
        }
        if(mScrollX < 0 &&dx > 0 && mTargetScrollX>0){
            mTargetScrollX = 0;
        }else if(mScrollX >0 && dx <0 &&mTargetScrollX <0){
            mTargetScrollX = 0;
        }
        int offsetDx = mTargetScrollX - mScrollX ;
        ViewCompat.offsetLeftAndRight(mContentView, offsetDx);

        if (mOutLeftView != null) {
            ViewCompat.offsetLeftAndRight(mOutLeftView, offsetDx);
        }
        if (mOutRightView != null) {
            ViewCompat.offsetLeftAndRight(mOutRightView, offsetDx);
        }
        if (mInnerTopView != null) {
            ViewCompat.offsetLeftAndRight(mInnerTopView, offsetDx);
        }
        if (mOutTopView != null) {
            ViewCompat.offsetLeftAndRight(mOutTopView, offsetDx);
        }
        mScrollX = mTargetScrollX;

    }
    private void cancelAnim() {

    }

    public boolean canHorizontalScroll() {
        if (mOutLeftView == null && mOutRightView == null) {
            return false;
        }
        return true;
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
    Rect mRect = new Rect();
    int[] mLocation = new int[2];

    boolean shouldOffsetEvent;

    public boolean shouldOffsetEvent() {
        return shouldOffsetEvent;
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
            mScrollChild = parent;
            return mScrollChild;
        } else if (isVerticalScrollView(parent)) {
            View child = null;
            if (parent instanceof ScrollView || parent instanceof NestedScrollView) {
                ViewGroup group = (ViewGroup) parent;
                if (group.getChildCount() > 0 && group.getChildAt(0) instanceof ViewGroup) {
                    child = group.getChildAt(0);
                    offsetPoint(point, group, child);
                }
            }
            View hitView = findChildViewUnder((ViewGroup) child, x, y);
            if (hitView == null) {
                return null;
            }
            if (isHorizontalScrollView(hitView)) {
                mScrollChild = hitView;
                return mScrollChild;
            }
        } else {
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                View hitView = findChildViewUnder(viewGroup, point.x, point.y);
                if (hitView == null) {
                    return null;
                }
                if (isHorizontalScrollView(hitView)) {
                    mScrollChild = hitView;
                } else if (isVerticalScrollView(hitView)) {
                    viewGroup = (ViewGroup) hitView;
                    View child = null;
                    if (viewGroup instanceof ScrollView || viewGroup instanceof NestedScrollView) {
                        if (viewGroup.getChildCount() > 0 && viewGroup.getChildAt(0) instanceof ViewGroup) {
                            child = viewGroup.getChildAt(0);
                            offsetPoint(point, viewGroup, child);
                        } else {
                            return null;
                        }
                    }
                    hitView = findChildViewUnder((ViewGroup) hitView, point.x, point.y);
                    if (hitView != null && isHorizontalScrollView(hitView)) {
                        mScrollChild = hitView;
                        return mScrollChild;
                    }
                }
            }

        }
        return null;
    }

    public boolean vertiaclParentContainHorizentalScrollChild() {
        return true;
    }

    Point mPoint = new Point();


    public boolean onDownEvent(int x, int y, EasyScrollLayout parent) {
        shouldOffsetEvent = false;
        mScrollChild = null;
        if (mContentView != null) {
            mContentView.getHitRect(mRect);
            shouldOffsetEvent |= mRect.contains(x, y);
            if (shouldOffsetEvent) {
                mPoint.set(x, y);
                offsetPoint(mPoint, parent, mContentView);
                offsetPoint(mPoint, mContentView, mContentView.mContentView);
                mScrollChild = huntingScrollChild(mContentView.mContentView, mPoint);
                return shouldOffsetEvent;
            }
        }
        if (mInnerTopView != null) {
            mInnerTopView.getHitRect(mRect);
            shouldOffsetEvent |= mRect.contains(x, y);
            if (shouldOffsetEvent) {
                mPoint.set(x, y);
                offsetPoint(mPoint, parent, mInnerTopView);
                return shouldOffsetEvent;
            }
        }
        if (mOutTopView != null) {
            mOutTopView.getHitRect(mRect);
            shouldOffsetEvent |= mRect.contains(x, y);
        }
        return shouldOffsetEvent;
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

//    public static boolean pointInChild(View child, int x, int y, int scrollX, int scrollY) {
//
//        return !(y < child.getY() - scrollY
//                || y > child.getY() + child.getMeasuredHeight() - scrollY
//                || x < child.getX() - scrollX
//                || x > child.getX() + child.getMeasuredWidth() - scrollX);
//    }

    public static boolean isTouchPointInView(View view, int rawX, int rawY) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        Rect rect = new Rect();
        return screenPointInView(view, rawY, rawY, rect, location);
    }

    public boolean canFling() {
        return isHorizontallyScrolled();
    }

    OverScroller mScroller;

    public void dispatchFling(int velocityX) {
        if (mScroller == null) {
            mScroller = new OverScroller(mContentView.getContext());
        }
        if (mScrollX == 0) {
            return;
        }
        if (mFlingRunnable != null) {
            mContentView.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if (velocityX != 0 && mScrollX < mMaxHorizontalStableScrollRange && mScrollX > mMinHorizontalStableScrollRange) {
            fiing(velocityX);
        } else {
            if (mScrollX < 0) {
                int targetX = (mMinHorizontalStableScrollRange) / 2 < mScrollX ? 0 : mMinHorizontalStableScrollRange;
                if (mScroller.springBack(mScrollX, 0, targetX, targetX, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mContentView);
                    ViewCompat.postOnAnimation(mContentView, mFlingRunnable);
                }
            } else {
                int targetX = (mMaxHorizontalStableScrollRange) / 2 < mScrollX ? mMaxHorizontalStableScrollRange : 0;
                if (mScroller.springBack(mScrollX, 0, targetX, targetX, 0, 0)) {
                    mFlingRunnable = new FlingRunnable(mContentView);
                    ViewCompat.postOnAnimation(mContentView, mFlingRunnable);
                }
            }

        }
    }

    Runnable mFlingRunnable;

    private void fiing(int velocityX) {
        if (mScrollX > 0 ) {
            if(velocityX>0){

                mScroller.fling(mScrollX, 0, velocityX, 0, mMaxHorizontalStableScrollRange, mMaxHorizontalStableScrollRange, 0, 0);
            }else {
                mScroller.fling(mScrollX, 0, velocityX, 0, 0, 0, 0, 0);
            }
        }else {
            if(velocityX>0){
                mScroller.fling(mScrollX, 0, velocityX, 0, 0,0 , 0, 0);
            }else {
                mScroller.fling(mScrollX, 0, velocityX, 0, mMinHorizontalStableScrollRange, mMinHorizontalStableScrollRange, 0, 0);
            }
        }

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable(mContentView);
            ViewCompat.postOnAnimation(mContentView, mFlingRunnable);
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
                    offsetLeftAndRight(curX-mScrollX);
                    // Post ourselves so that we run on the next animation
                    ViewCompat.postOnAnimation(mLayout, this);
                } else {
                    onFlingFinished();
                }
            }
        }
    }

    private void onFlingFinished() {


    }
}
