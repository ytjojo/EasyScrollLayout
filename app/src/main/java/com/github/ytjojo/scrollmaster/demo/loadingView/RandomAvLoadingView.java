package com.github.ytjojo.scrollmaster.demo.loadingView;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.UIHandler;
import com.github.ytjojo.scrollmaster.Utils;
import com.github.ytjojo.scrollmaster.demo.R;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class RandomAvLoadingView extends FrameLayout implements UIHandler {

    String[] mArray = new String[]{
            "BallPulseIndicator",
            "BallGridPulseIndicator",
            "BallClipRotateIndicator",
            "BallClipRotatePulseIndicator",
            "SquareSpinIndicator",
            "BallClipRotateMultipleIndicator",
            "BallPulseRiseIndicator",
            "BallRotateIndicator",
            "CubeTransitionIndicator",
            "BallZigZagIndicator",
            "BallZigZagDeflectIndicator",
            "BallTrianglePathIndicator",
            "BallScaleIndicator",
            "LineScaleIndicator",
            "LineScalePartyIndicator",
            "BallScaleMultipleIndicator",
            "BallPulseSyncIndicator",
            "BallBeatIndicator",
            "LineScalePulseOutIndicator",
            "LineScalePulseOutRapidIndicator",
            "BallScaleRippleIndicator",
            "BallScaleRippleMultipleIndicator",
            "BallSpinFadeLoaderIndicator",
            "LineSpinFadeLoaderIndicator",
            "TriangleSkewSpinIndicator",
            "PacmanIndicator",
            "BallGridBeatIndicator",
            "SemiCircleSpinIndicator"

    };

    AVLoadingIndicatorView mIndicatorView;
    public RandomAvLoadingView(@NonNull Context context) {
        this(context,null);
    }

    public RandomAvLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RandomAvLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

         mIndicatorView = new AVLoadingIndicatorView(context);
        int width = (int) Utils.dipToPixels(getContext(),50);
        int padding = (int) Utils.dipToPixels(getContext(),5);
        setPadding(0, padding,0,padding);
        FrameLayout.LayoutParams lp= new FrameLayout.LayoutParams(width,width);
        lp.gravity = Gravity.CENTER;
        this.addView(mIndicatorView,lp);

        mIndicatorView.setIndicator(mArray[(int) ((Math.random()*28000)%28)]);
        mIndicatorView.setIndicatorColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onUIReset(BaseRefreshIndicator indicator) {
        mIndicatorView.getIndicator().stop();
        mIndicatorView.setIndicator(mArray[(int) ((Math.random()*28000)%28)]);
//        mIndicatorView.setIndicatorColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onUIRefreshPrepare(BaseRefreshIndicator indicator) {
        mIndicatorView.getIndicator().stop();
        mIndicatorView.setPivotX(mIndicatorView.getWidth()/2);
        mIndicatorView.setPivotY(mIndicatorView.getHeight());
    }

    @Override
    public void onUIRefreshBegin(BaseRefreshIndicator indicator) {

    }

    @Override
    public void onUIRefreshComplete(BaseRefreshIndicator indicator) {

    }

    @Override
    public void onUIReleaseBeforeRefresh(BaseRefreshIndicator indicator) {
        mIndicatorView.getIndicator().start();
    }

    @Override
    public void onUIScrollChanged(BaseRefreshIndicator indicator, int scrollValue, byte status) {
            if(status == BaseRefreshIndicator.PTR_STATUS_PREPARE){
                float progress =indicator.getProgress(scrollValue);
                if(progress<=1){
                    this.setScaleX(progress);
                    this.setScaleY(progress);
                }else {
                    this.setScaleX(1f);
                    this.setScaleY(1f);
                }
            }
    }
}
