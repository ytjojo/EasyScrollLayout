package com.github.ytjojo.easyscrolllayout;

import android.view.View;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/25 0025.
 */

public abstract class BaseRefreshIndicator {
    public static final byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;

    public View getTargetView() {
        return mTargetView;
    }

    public void setTargetView(View targetView) {
        this.mTargetView = targetView;
        if (targetView instanceof UIHandler && !mUiHandlers.contains(targetView)) {

            mUiHandlers.add((UIHandler) targetView);
        }
    }

    ArrayList<UIHandler> mUiHandlers = new ArrayList<>();

    View mTargetView;

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

    public abstract void onScrollChanged(int lastScrollValue, int curScrollValue);

    public abstract void onStopScroll(int scrollValue);

    public byte getStatus() {
        return mStatus;
    }

    private int mStableValue;
    private int mOverScrollValue;
    private int mTriggerValue;
    private int mLimitValue;

    public int getLimitValue() {
        return mLimitValue;
    }

    public void setLimitValue(int limitScrollY) {
        this.mLimitValue = limitScrollY;
    }

    public int getStableValue() {

        return mStableValue;
    }

    public int getOverScrollValue() {
        return mOverScrollValue;
    }

    public int getTriggerValue() {
        return mTriggerValue;
    }

    public void dispatchScrollChanged(byte status, int scrollValue) {
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIScrollChanged(this, scrollValue, mStatus);
        }
    }


    public void setComplete() {
        if (mStatus == PTR_STATUS_LOADING) {
            mStatus = PTR_STATUS_COMPLETE;
            dispatchRefreshComplete();
        }
    }

    public void dispatchRefreshComplete() {
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIRefreshComplete(this);
        }
    }

    public void dispatchReset() {
        Logger.e("header ::: ------> dispatchReset");
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIReset(this);
        }
    }

    public void dispatchStartRefresh() {
        Logger.e("header ::: ------> dispatchStartRefresh");
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIRefreshBegin(this);
        }
    }

    public void dispatchRefreshPrepare() {
        Logger.e("header ::: ------> dispatchRefreshPrepare");
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIRefreshPrepare(this);
        }
    }

    public void dispatchReleaseBeforeRefresh() {
        Logger.e("header ::: ------> dispatchReleaseBeforeRefresh");
        for (UIHandler uiHandler : mUiHandlers) {
            uiHandler.onUIReleaseBeforeRefresh(this);
        }
    }

    private boolean mCanLoad = true;

    public boolean getCanLoad() {
        return mCanLoad;
    }

    public void setCanLoad(boolean canLoad) {
        this.mCanLoad = canLoad;
    }

    public void addUIHandler(UIHandler uiHandler){
        if(!mUiHandlers.contains(uiHandler)){
            mUiHandlers.add(uiHandler);
        }
    }
    public void removeUIHandler(UIHandler uiHandler){
        mUiHandlers.remove(uiHandler);
    }
}
