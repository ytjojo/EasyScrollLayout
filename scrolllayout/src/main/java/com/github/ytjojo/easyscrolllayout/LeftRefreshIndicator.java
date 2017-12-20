package com.github.ytjojo.easyscrolllayout;

import android.view.View;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2017/12/20 0020.
 */

public class LeftRefreshIndicator {


    public static final byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;

    public View getView() {
        return mChild;
    }

    public void setView(View child) {
        this.mChild = child;
    }

    View mChild;

    public void setStatus(byte status) {
        this.mStatus = status;
    }

    public boolean isComplete() {
        return mStatus == PTR_STATUS_COMPLETE;
    }

    public boolean isPrepare() {
        return mStatus == PTR_STATUS_PREPARE;
    }

    public boolean isLoading() {
        return mStatus == PTR_STATUS_LOADING;
    }

    public void onScrollChanged(int lastScrollX, int curScrollX) {
        if (mStatus == PTR_STATUS_INIT && curScrollX < 0) {
            mStatus = PTR_STATUS_PREPARE;
            dispatchRefreshPrepare();
            dispatchScrollChanged(mStatus, -curScrollX);
        } else if ((mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) && curScrollX < 0) {
            dispatchScrollChanged(mStatus, -curScrollX);
        } else if (lastScrollX < 0 && curScrollX >= 0) {
            if (mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) {
                dispatchScrollChanged(mStatus, 0);
            }
        }

    }

    public void dispatchScrollChanged(byte status, int scrollX) {

    }

    public void onStopScroll(int scrollX) {
        if (mStatus == PTR_STATUS_INIT || mChild == null) {
            return;
        }
        if (scrollX == -mChild.getMeasuredWidth() && mStatus == PTR_STATUS_PREPARE) {
            mStatus = PTR_STATUS_LOADING;
            dispatchStartRefresh();
        } else if (mStatus == PTR_STATUS_COMPLETE && scrollX >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (mStatus == PTR_STATUS_PREPARE && scrollX >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        }
    }
    public void setComplete(){
        if(mStatus ==PTR_STATUS_LOADING){
            mStatus = PTR_STATUS_COMPLETE;
        }
    }

    public void dispatchReset() {
        Logger.e("header ::: ------> dispatchReset");
    }

    public void dispatchStartRefresh() {
        Logger.e("header ::: ------> dispatchStartRefresh");
    }

    public void dispatchRefreshPrepare() {
        Logger.e("header ::: ------> dispatchRefreshPrepare");
    }

    public void dispatchReleaseBeforeRefresh() {
        Logger.e("header ::: ------> dispatchReleaseBeforeRefresh");
    }

    private boolean mCanLoad = true;

    public boolean getCanLoad() {
        return mCanLoad;
    }

    public void setCanLoad(boolean canLoad) {
        this.mCanLoad = canLoad;
    }

    private int mLimitScrollY;

    public int getLimitScrollY() {
        return mLimitScrollY;
    }

    public void setLimitScrollY(int limitScrollY) {
        this.mLimitScrollY = limitScrollY;
    }
}
