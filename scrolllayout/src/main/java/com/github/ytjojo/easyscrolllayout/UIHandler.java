package com.github.ytjojo.easyscrolllayout;

import android.view.View;

public interface UIHandler {
    void onUIReset(BaseRefreshIndicator indicator);

    void onUIRefreshPrepare(BaseRefreshIndicator indicator);

    void onUIRefreshBegin(BaseRefreshIndicator indicator);

    void onUIRefreshComplete(BaseRefreshIndicator indicator);

    void onUIReleaseBeforeRefresh(BaseRefreshIndicator indicator);

    void onUIScrollChanged(BaseRefreshIndicator indicator,int scrollValue,byte status);


}