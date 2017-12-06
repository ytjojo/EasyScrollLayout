package com.github.ytjojo.easyscrolllayout.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;

import java.util.ArrayList;

public class BannerActivity extends AppCompatActivity {

    private BannerComponent bannerComponent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_headerviewpager);
        //ViewPager,Indicator
        ViewPager viewPager = (ViewPager) findViewById(R.id.banner_viewPager);
        Indicator indicator = (Indicator) findViewById(R.id.banner_indicator);
        indicator.setScrollBar(new ColorBar(getApplicationContext(), Color.WHITE, 0, ScrollBar.Gravity.CENTENT_BACKGROUND));
        viewPager.setOffscreenPageLimit(2);

        bannerComponent = new BannerComponent(indicator, viewPager, false);
        bannerComponent.setAdapter(new ImageViewgerAdapte());

        //默认就是800毫秒，设置单页滑动效果的时间
//        bannerComponent.setScrollDuration(800);
        //设置播放间隔时间，默认情况是3000毫秒
        bannerComponent.setAutoPlayTime(2500);

        ViewPager mainviewPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<Class<? extends Fragment>> classes = new ArrayList<>();
        classes.add(RecyclerViewFragment.class);
        classes.add(ScrollViewFragment.class);
        classes.add(NestedScrollFragment.class);
        classes.add(WebViewFragment.class);
        mainviewPager.setAdapter(new ViewpagerFragmentAdapter(getSupportFragmentManager(),classes)) ;

    }

    @Override
    protected void onStart() {
        super.onStart();
        bannerComponent.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bannerComponent.stopAutoPlay();
    }


}