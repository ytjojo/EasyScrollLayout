package com.github.ytjojo.easyscrolllayout;


/**
 * Created by Administrator on 2017/12/16 0016.
 */

public class TopHeaderIndicator extends BaseRefreshIndicator{
    public TopHeaderIndicator(){
        setType(TYPE_TOP);
    }
    @Override
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

    @Override
    public void onStopScroll(int scrollY) {
        if (mStatus == PTR_STATUS_INIT || getTargetView() == null) {
            return;
        }
        if (scrollY == -getTargetView().getMeasuredHeight() && mStatus == PTR_STATUS_PREPARE) {
            mStatus = PTR_STATUS_LOADING;
            dispatchStartRefresh();
        } else if (mStatus == PTR_STATUS_COMPLETE && scrollY >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (mStatus == PTR_STATUS_PREPARE && scrollY >= 0) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        }
    }
}
