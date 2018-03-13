package com.github.ytjojo.scrollmaster;

/**
 * Created by Administrator on 2017/12/20 0020.
 */

public class RightRefreshIndicator extends BaseRefreshIndicator{

    public RightRefreshIndicator(){
        setType(TYPE_RIGHT);
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

    public void onStopScroll(int scrollX) {
        if (mStatus == PTR_STATUS_INIT || getTargetView() == null) {
            return;
        }
        if (scrollX == -getTargetView().getMeasuredWidth() && mStatus == PTR_STATUS_PREPARE) {
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
}
