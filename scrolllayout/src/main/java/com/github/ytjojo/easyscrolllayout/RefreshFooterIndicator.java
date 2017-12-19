package com.github.ytjojo.easyscrolllayout;

import android.view.View;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2017/12/16 0016.
 */

public class RefreshFooterIndicator {
    public static final byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;
    View mOutBottomView;

    public View getOutBottomView() {
        return mOutBottomView;
    }

    public void setOutBottomView(View outBottomView) {
        this.mOutBottomView = outBottomView;
    }

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
        if (mOutBottomView == null) {
            return;
        }
        ContentWraperView.LayoutParams lp = (ContentWraperView.LayoutParams) mOutBottomView.getLayoutParams();
        if (mStatus == PTR_STATUS_INIT && curScrollY > lp.mMinScrollY) {
            mStatus = PTR_STATUS_PREPARE;
            dispatchRefreshPrepare();
            dispatchScrollChanged(mStatus, curScrollY - lp.mMinScrollY);
        } else if ((mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) && curScrollY > lp.mMinScrollY) {
            dispatchScrollChanged(mStatus, curScrollY - lp.mMinScrollY);
        } else if (mStatus == PTR_STATUS_PREPARE && curScrollY > lp.mMinScrollY) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (lastScrollY > lp.mMinScrollY && lastScrollY <= lp.mMinScrollY) {
            dispatchScrollChanged(mStatus, 0);
        }

    }

    public void dispatchScrollChanged(byte status, int scrollY) {
        Logger.e(status + "footer ::: ------> status  " + scrollY);
    }

    public void onStopScroll(int scrollY) {
        if (mOutBottomView == null) {
            return;
        }
        ContentWraperView.LayoutParams lp = (ContentWraperView.LayoutParams) mOutBottomView.getLayoutParams();
        if (scrollY == lp.mStableScrollY && mStatus == PTR_STATUS_PREPARE) {
            mStatus = PTR_STATUS_LOADING;
            dispatchStartRefresh();
        } else if (mStatus == PTR_STATUS_COMPLETE && scrollY <= lp.mMinScrollY) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        }else if (mStatus == PTR_STATUS_PREPARE && scrollY <= lp.mMinScrollY) {
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
        Logger.e("footer ::: ------> dispatchReset");
    }

    public void dispatchStartRefresh() {
        Logger.e("footer ::: ------> dispatchStartRefresh");
    }

    public void dispatchRefreshPrepare() {
        Logger.e("footer ::: ------> dispatchRefreshPrepare");
    }

    public void dispatchReleaseBeforeRefresh() {
        Logger.e("footer ::: ------> dispatchReleaseBeforeRefresh");
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
