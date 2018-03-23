package com.github.ytjojo.scrollmaster.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shizhefei.view.indicator.BannerComponent;

import java.util.ArrayList;

public class DrawerBannerActivity extends AppCompatActivity {
    int mLastY;
    int mFirstY;
    private BannerComponent bannerComponent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_drawer_headerviewpager);
        //ViewPager,Indicator
//        ViewPager viewPager = (ViewPager) findViewById(R.id.banner_viewPager);
//        Indicator indicator = (Indicator) findViewById(R.id.banner_indicator);
//        indicator.setScrollBar(new ColorBar(getApplicationContext(), Color.WHITE, 0, ScrollBar.Gravity.CENTENT_BACKGROUND));
//        viewPager.setOffscreenPageLimit(2);
//
//        bannerComponent = new BannerComponent(indicator, viewPager, false);
//        bannerComponent.setAdapter(new ImageViewgerAdapte());
//
//        //默认就是800毫秒，设置单页滑动效果的时间
////        bannerComponent.setScrollDuration(800);
//        //设置播放间隔时间，默认情况是3000毫秒
//        bannerComponent.setAutoPlayTime(2500);

        ViewPager mainviewPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<Class<? extends Fragment>> classes = new ArrayList<>();
        classes.add(RecyclerViewFragment.class);
        classes.add(ScrollViewFragment.class);
        classes.add(NestedScrollFragment.class);
        classes.add(WebViewFragment.class);
        mainviewPager.setAdapter(new ViewpagerFragmentAdapter(getSupportFragmentManager(),classes)) ;
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_left);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Logger.e(dy + "dy   recyclerView" );
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mLastY = (int) event.getY();
                        mFirstY =mLastY;
                        Logger.e(mLastY + "mLastY  recyclerView" );
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mLastY = (int) event.getY();
                        Logger.e("recyclerView mFirstY" + mFirstY  + " mLastY" + mLastY);
                        break;
                }

                return false;
            }
        });
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"ssssss",Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.right_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"ssssss",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        bannerComponent.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        bannerComponent.stopAutoPlay();
    }


}