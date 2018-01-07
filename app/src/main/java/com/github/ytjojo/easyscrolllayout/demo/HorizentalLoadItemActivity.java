package com.github.ytjojo.easyscrolllayout.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.github.ytjojo.easyscrolllayout.BaseRefreshIndicator;
import com.github.ytjojo.easyscrolllayout.EasyScrollLayout;
import com.orhanobut.logger.Logger;
import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class HorizentalLoadItemActivity extends AppCompatActivity {
    private BannerComponent bannerComponent;
    EasyScrollLayout mMainEasyScrollLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recylerview);
        mMainEasyScrollLayout = (EasyScrollLayout) findViewById(R.id.StickyNavLayout);
        mMainEasyScrollLayout.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                mMainEasyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMainEasyScrollLayout.setTopHeaderLoadComplete();
                    }
                },3000);
            }
        });
        ViewPager viewPager = (ViewPager) findViewById(R.id.banner_viewPager);
        Indicator indicator = (Indicator) findViewById(R.id.banner_indicator);
        indicator.setScrollBar(new ColorBar(getApplicationContext(), Color.WHITE, 0, ScrollBar.Gravity.CENTENT_BACKGROUND));
        viewPager.setOffscreenPageLimit(2);

        bannerComponent = new BannerComponent(indicator, viewPager, false);
        bannerComponent.setAdapter(new ImageViewgerAdapte());
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_list_simple,getHeader()));

    }

    private View getHeader() {
        final EasyScrollLayout easyScrollLayout = (EasyScrollLayout) LayoutInflater.from(this).inflate(R.layout.item_header_recyclerview, recyclerView, false);
//        easyScrollLayout.setEnabled(false);
        easyScrollLayout.setLeftOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(), "left request http", Toast.LENGTH_SHORT).show();
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setLeftComplete();
                    }
                }, 3000);
            }
        });
        easyScrollLayout.setRightOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(), "right request http", Toast.LENGTH_SHORT).show();
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setRightComplete();
                    }
                }, 3000);
            }
        });


        RecyclerView recyclerView = (RecyclerView) easyScrollLayout.findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Logger.e(dy + "dy   recyclerView");
            }
        });

        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_horizontal,20));
        return easyScrollLayout;
    }

    RecyclerView recyclerView;


}
