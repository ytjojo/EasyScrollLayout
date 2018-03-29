package com.github.ytjojo.scrollmaster;

import android.view.View;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/25 0025.
 */

public abstract class BaseRefreshIndicator {
    public static final byte PTR_STATUS_INIT = 1;
    protected byte mStatus = PTR_STATUS_INIT;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;
    public static final byte TYPE_TOP = 0;
    public static final byte TYPE_BOTTOM = 1;
    public static final byte TYPE_LEFT = 2;
    public static final byte TYPE_RIGHT = 3;
    public  int mType;

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

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

    public void setStableValue(int stableValue) {
        this.mStableValue = stableValue;
    }

    public void setOverScrollValue(int overScrollValue) {
        this.mOverScrollValue = overScrollValue;
    }

    public void setTriggerValue(int triggerValue) {
        this.mTriggerValue = triggerValue;
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
        if(mOnStartLoadCallback !=null){
            mOnStartLoadCallback.onStartLoad();
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

    private OnStartLoadCallback mOnStartLoadCallback;

    public void setOnStartLoadCallback(OnStartLoadCallback callback){
        this.mOnStartLoadCallback = callback;
    }
    public interface OnStartLoadCallback{
        void onStartLoad();
    }
    public float getProgress(int curValue) {
        return (curValue*1f/Math.abs(getStableValue()-getLimitValue()));
    }
}
