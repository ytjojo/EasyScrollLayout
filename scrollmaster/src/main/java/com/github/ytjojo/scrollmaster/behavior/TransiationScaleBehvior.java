package com.github.ytjojo.scrollmaster.behavior;

import android.support.v4.view.ViewCompat;
import android.view.View;

import com.github.ytjojo.scrollmaster.ScrollMasterView;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class TransiationScaleBehvior implements ScrollMasterView.OnScollListener {

    private final float mMinScale;
    private final float mMinMarginLeft;
    private final float mMInBottom;
    private final View mTargetView;
    private int mViewWidth;
    private int mViewHeight;
    private int mMinViewWidth;
    private int mMinViewHeight;

    public TransiationScaleBehvior(View targetView, float mMinMarginLeft,
                                   float minBottom,
                                   int minViewHeight) {
        this.mTargetView = targetView;
        this.mMinMarginLeft = mMinMarginLeft;
        this.mMInBottom = minBottom;
        this.mMinViewHeight = minViewHeight;
        this.mViewWidth = targetView.getWidth();
        this.mViewHeight = targetView.getHeight();
        this.mMinScale = mMinViewHeight * 1f / mViewHeight;
        ViewCompat.setPivotX(mTargetView, 0.0f);
        ViewCompat.setPivotY(mTargetView, 0.5f* mViewHeight);
    }

    @Override
    public void onScroll(float offsetRatio, int positionOffsetPixels, int offsetRange) {

        float scale = mMinScale + (1 - offsetRatio) * mMinScale;
        mTargetView.setScaleX(scale);
        mTargetView.setScaleY(scale);
        mTargetView.setTranslationX(-(mTargetView.getLeft() - mMinMarginLeft) * offsetRatio);
        mTargetView.setTranslationY(positionOffsetPixels-((mTargetView.getTop()+mViewHeight/2f - mMInBottom) * offsetRatio));
//        Logger.e(((mTargetView.getTop()+mViewHeight/2f - mMInBottom))+"positionOffsetPixels"+ positionOffsetPixels+ " offsetRatio"+ offsetRatio + "offsetRange" + offsetRange );
    }

    public static void setTranslationScale(final ScrollMasterView scrollMasterView,
                                           final View targetView,final int minViewHeight, final float mMinMarginLeft,
                                           final float actionbarSize, final float statusBarHeight) {
        targetView.post(new Runnable() {
            @Override
            public void run() {
//                float minBottom = actionbarSize + statusBarHeight- minViewHeight/2f-(actionbarSize/2 -minViewHeight/2);
                float minBottom =   statusBarHeight+(actionbarSize/2);

                TransiationScaleBehvior behvior = new TransiationScaleBehvior(targetView, mMinMarginLeft, minBottom, minViewHeight);
                scrollMasterView.addOnScrollListener(behvior);
            }
        });
    }
}
