package com.github.ytjojo.scrollmaster;

import android.view.View;

/**
 * Created by Administrator on 2017/12/2 0002.
 */

public class ContentChildHolder {

    View mDirectChild;
    VerticalScrollCheckHandlar mVerticalScrollCheckHandlar;

    public ContentChildHolder() {
        mVerticalScrollCheckHandlar = new VerticalScrollCheckHandlar();
    }

    public void onMeasure(ScrollMasterView parent) {
        final int count = parent.getChildCount();

        int maxArea = 0;
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final ScrollMasterView.LayoutParams lp = (ScrollMasterView.LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity == ScrollMasterView.GRAVITY_OUT_INVALID) {
                    final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                    if (childArea >= maxArea) {
                        mDirectChild = child;
                        maxArea = childArea;
                    }
                }
            }
        }
        if (mDirectChild != null) {
            mVerticalScrollCheckHandlar.huntingScrollChild(mDirectChild);
        }
    }

    public boolean reachChildTop() {
        return mVerticalScrollCheckHandlar.reachChildTop();
    }

    public boolean reachChildBottom() {
        return mVerticalScrollCheckHandlar.reachChildBottom();
    }

    public void fling(int velocityY) {
        if (mVerticalScrollCheckHandlar.mCurContentView != null) {
            mVerticalScrollCheckHandlar.mCurContentView.fling(velocityY);
        }
    }

    public void dispatchFling(int velocityY, boolean isDownSlide) {
        if (mVerticalScrollCheckHandlar.mCurContentView != null) {
            mVerticalScrollCheckHandlar.mCurContentView.dispatchFling(velocityY, isDownSlide);
        }
    }

    public boolean canFling() {
        if (mVerticalScrollCheckHandlar.mCurContentView != null) {
            return mVerticalScrollCheckHandlar.mCurContentView.canFling();
        }
        return false;
    }

    public boolean canNestedScrollVetical() {
        if (mVerticalScrollCheckHandlar.mCurContentView != null &&
                (mVerticalScrollCheckHandlar.mCurContentView.mMinVerticalScrollRange < 0 ||
                        mVerticalScrollCheckHandlar.mCurContentView.mMaxVerticalScrollRange > 0)) {
            return true;
        }
        return false;
    }

    public void preScrollConsumed(int dy, int[] consumed) {
        consumed[0] = consumed[1] = 0;
        if (mVerticalScrollCheckHandlar.mCurContentView != null) {
            mVerticalScrollCheckHandlar.mCurContentView.preScrollConsumed(dy, consumed);
        }
    }
    public void preScrollUp(int dy, int[] consumed) {
        consumed[0] = consumed[1] = 0;
        if (mVerticalScrollCheckHandlar.mCurContentView != null) {
            mVerticalScrollCheckHandlar.mCurContentView.preScrollUp(dy, consumed);
        }
    }
    public boolean canNestedFlingToBottom(){
        if(mVerticalScrollCheckHandlar.mCurContentView !=null && mVerticalScrollCheckHandlar.mCurContentView.mInnerBottomView !=null){
            return true;
        }
        return false;
    }


}
