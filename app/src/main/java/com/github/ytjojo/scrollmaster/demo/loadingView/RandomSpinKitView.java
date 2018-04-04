package com.github.ytjojo.scrollmaster.demo.loadingView;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.UIHandler;
import com.github.ytjojo.scrollmaster.Utils;
import com.github.ytjojo.scrollmaster.demo.R;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class RandomSpinKitView extends FrameLayout implements UIHandler {
    Sprite drawable;
    SpinKitView spinKitView;
    public RandomSpinKitView(Context context) {
        this(context,null);
    }

    public RandomSpinKitView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RandomSpinKitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int padding = (int) Utils.dipToPixels(getContext(),15);
        setPadding(0,padding,0,padding);
        setBackgroundColor(getResources().getColor(R.color.colorAccent));
         spinKitView = new SpinKitView(getContext());
        FrameLayout.LayoutParams lp= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)Utils.dipToPixels(getContext(),60));
        lp.gravity = Gravity.CENTER;
        this.addView(spinKitView,lp);
        spinKitView.setColor(Color.WHITE);
        random();
    }
    private void random(){
        Sprite sprite = SpriteFactory.create(Style.values()[(int) ((Math.random()*1500)%15)]);
        spinKitView.setIndeterminateDrawable(sprite);
        drawable = sprite;
    }

    @Override
    public void onUIReset(BaseRefreshIndicator indicator) {
        drawable.stop();
        random();
    }

    @Override
    public void onUIRefreshPrepare(BaseRefreshIndicator indicator) {
        //我不想刚下来就loading，先停止掉动画，等真正开始刷新的时候再启动
        drawable.stop();
    }

    @Override
    public void onUIRefreshBegin(BaseRefreshIndicator indicator) {

    }

    @Override
    public void onUIRefreshComplete(BaseRefreshIndicator indicator) {
    }

    @Override
    public void onUIReleaseBeforeRefresh(BaseRefreshIndicator indicator) {
        drawable.start();
    }

    @Override
    public void onUIScrollChanged(BaseRefreshIndicator indicator, int scrollValue, byte status) {
        if(indicator.getStableValue()>0){
            int maxValue = Math.abs(indicator.getStableValue()- indicator.getLimitValue());
            if(scrollValue < maxValue){
                this.setTranslationY(maxValue-scrollValue);
            }else {
                this.setTranslationY(0);
            }
        }else {
            int maxValue = Math.abs(indicator.getStableValue()- indicator.getLimitValue());
            if(scrollValue < maxValue){
                this.setTranslationY(maxValue-scrollValue);
            }else {
                this.setTranslationY(0);
            }
        }
    }
}
