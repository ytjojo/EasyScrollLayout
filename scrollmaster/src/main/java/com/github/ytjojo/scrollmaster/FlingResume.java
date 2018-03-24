package com.github.ytjojo.scrollmaster;

import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.widget.OverScroller;

/**
 * Created by Administrator on 2018/3/24 0024.
 */

public class FlingResume implements Runnable {
    OverScroller mScroller;
    float mOffsetY;
    ScrollMasterView mScrollMasterView;
    int mEventX;

    public FlingResume(ScrollMasterView scrollMasterView) {
        this.mScrollMasterView = scrollMasterView;
        this.mScroller = new OverScroller(mScrollMasterView.getContext());
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
            if (mTrigger && state == 0) {
                long time = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, mEventX, mOffsetY - mScroller.getCurrY(), 0);
                state++;
                mScrollMasterView.dispatchTouchEventSupper(event);
            } else {
                if (state == 1) {
                    state++;
                }
                if (state == 2) {
                    long time = SystemClock.uptimeMillis();
                    event = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, mEventX, mOffsetY - mScroller.getCurrY(), 0);
                    mScrollMasterView.dispatchTouchEventSupper(event);
                }
            }
            ViewCompat.postOnAnimation(mScrollMasterView, this);
        } else {
            if (state == 2 && mTrigger) {
                state++;
                long time = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, mEventX, mOffsetY - mScroller.getCurrY(), 0);
                mScrollMasterView.dispatchTouchEventSupper(event);
            }
        }

    }

    public void reset() {
        if (state == 1 || state == 2) {
            long time = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, mEventX, mOffsetY - mScroller.getCurrY(), 0);
            mScrollMasterView.dispatchTouchEventSupper(event);
        }
        state = 0;
        mTrigger = false;
        mScrollMasterView.removeCallbacks(this);
        mScroller.abortAnimation();

    }


    public void start(int startScrollY, int velocityY) {
        mOffsetY = mScrollMasterView.getHeight()*0.75f;
        mEventX = mScrollMasterView.getWidth() / 2;
        mScroller.fling(0, startScrollY, 0, velocityY,
                0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        ViewCompat.postOnAnimation(mScrollMasterView, this);
    }
}
