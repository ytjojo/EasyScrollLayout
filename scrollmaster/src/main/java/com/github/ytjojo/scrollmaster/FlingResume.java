package com.github.ytjojo.scrollmaster;

import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.widget.OverScroller;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2018/3/24 0024.
 */

public class FlingResume implements Runnable {
    OverScroller mScroller;
    float mEventYOffset;
    ScrollMasterView mScrollMasterView;
    int mEventX;
    int mMaxVerticalScrollRange;

    public FlingResume(ScrollMasterView scrollMasterView) {
        this.mScrollMasterView = scrollMasterView;
        this.mScroller = new OverScroller(mScrollMasterView.getContext());
    }

    public void setMaxVerticalScrollRange(int maxVerticalScrollRange){
        this.mMaxVerticalScrollRange =maxVerticalScrollRange;
    }
    int state = 0;
    boolean mTrigger;

    public void setTrigger() {
        mTrigger = true;

    }

    @Override
    public void run() {
        MotionEvent event;
        if (mScroller.computeScrollOffset()) {
            if(!mTrigger){
                mScrollMasterView.scrollTo(0,mScroller.getCurrY());
                if(mScrollMasterView.getScrollY() >= mMaxVerticalScrollRange){
                    mTrigger = true;
                }
            }
            if (mTrigger && state == 0) {
                long time = SystemClock.uptimeMillis();
                mEventYOffset =  mScrollMasterView.getHeight() * 0.75f + mScroller.getCurrY();
                event = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, mEventX, mEventYOffset - mScroller.getCurrY(), 0);
                state++;
                mScrollMasterView.dispatchTouchEventSupper(event);
            } else {
                if (state == 1) {
                    state++;
                }
                if (state == 2) {
                    long time = SystemClock.uptimeMillis();
                    event = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, mEventX, mEventYOffset - mScroller.getCurrY(), 0);
                    mScrollMasterView.dispatchTouchEventSupper(event);
                }
            }
            ViewCompat.postOnAnimation(mScrollMasterView, this);
        } else {
            if (state == 2|| state ==1) {
                long time = SystemClock.uptimeMillis();
                if(state == 2 ){
                    event = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, mEventX, mEventYOffset - mScroller.getCurrY(), 0);
                }else {
                    event = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, mEventX, mEventYOffset - mScroller.getCurrY(), 0);
                }
                mScrollMasterView.dispatchTouchEventSupper(event);
                state++;
            }
        }

    }

    public void reset() {
        if (state == 1 || state == 2) {
            long time = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, mEventX, mEventYOffset - mScroller.getCurrY(), 0);
            mScrollMasterView.dispatchTouchEventSupper(event);
        }
        state = 0;
        mTrigger = false;
        mScrollMasterView.removeCallbacks(this);
        mScroller.abortAnimation();

    }


    public void start(int startScrollY, int velocityY) {
        mEventX = mScrollMasterView.getWidth() / 2;
        int maxY = 4 * mScrollMasterView.getHeight();
        mScroller.fling(0, startScrollY, 0, velocityY,
                0, 0, 0, maxY);
        ViewCompat.postOnAnimation(mScrollMasterView, this);
    }
}
