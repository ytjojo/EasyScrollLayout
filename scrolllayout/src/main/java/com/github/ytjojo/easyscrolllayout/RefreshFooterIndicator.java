package com.github.ytjojo.easyscrolllayout;


/**
 * Created by Administrator on 2017/12/16 0016.
 */

public class RefreshFooterIndicator extends BaseRefreshIndicator {
    @Override
    public void onScrollChanged(int lastScrollY, int curScrollY) {
        if (getTargetView() == null) {
            return;
        }
        if (mStatus == PTR_STATUS_INIT && curScrollY > getLimitValue()) {
            mStatus = PTR_STATUS_PREPARE;
            dispatchRefreshPrepare();
            dispatchScrollChanged(mStatus, curScrollY - getLimitValue());
        } else if ((mStatus == PTR_STATUS_PREPARE || mStatus == PTR_STATUS_LOADING) && curScrollY > getLimitValue()) {
            dispatchScrollChanged(mStatus, curScrollY - getLimitValue());
        } else if (mStatus == PTR_STATUS_PREPARE && curScrollY > getLimitValue()) {
            mStatus = PTR_STATUS_INIT;
            dispatchReset();
        } else if (lastScrollY > getLimitValue() && lastScrollY <= getLimitValue()) {
            dispatchScrollChanged(mStatus, 0);
        }

    }

    @Override
    public void onStopScroll(int scrollY) {
        if (getTargetView() == null) {
            return;
        }
        ContentWraperView.LayoutParams lp = (ContentWraperView.LayoutParams) getTargetView().getLayoutParams();
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
}
