package com.github.ytjojo.easyscrolllayout;

import android.view.View;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2017/12/16 0016.
 */

public class RefreshHeaderIndicator {

    public static final byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;

    public View getOutTopView() {
        return mOutTopView;
    }

    public void setOutTopView(View outTopView) {
        this.mOutTopView = outTopView;
    }

    View mOutTopView;

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

    public void onScrollChanged(int lastScrollY, int curScrollY) {
        if (mStatus == PTR_STATUS_INIT && curScrollY < 0) {
            mStatus = PTR_STATUS_PREPARE;
            dispatchRefreshPrepare();
            dispatchScrollChanged(mStatus, -curScrollY);
        } else if ((mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) && curScrollY < 0) {
            dispatchScrollChanged(mStatus, -curScrollY);
        } else if (lastScrollY < 0 && curScrollY >= 0) {
            if (mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) {
                dispatchScrollChanged(mStatus, 0);
            }
        }

    }

    public void dispatchScrollChanged(byte status, int scrollY) {

    }

    public void onStopScroll(int scrollY) {
        if (mStatus == PTR_STATUS_INIT || mOutTopView == null) {
            return;
        }
        if (scrollY == -mOutTopView.getMeasuredHeight() && mStatus == PTR_STATUS_PREPARE) {
            mStatus = PTR_STATUS_LOADING;
            dispatchStartRefresh();
        } else if (mStatus == PTR_STATUS_COMPLETE && scrollY >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (mStatus == PTR_STATUS_PREPARE && scrollY >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (mStatus == PTR_STATUS_COMPLETE && scrollY >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
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
