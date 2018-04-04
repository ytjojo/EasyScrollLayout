package com.github.ytjojo.scrollmaster.demo;

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
import android.view.ViewGroup;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ContentWraperView;
import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class DrawerRecylerViewWithHeaderActivity extends AppCompatActivity {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_recylerview);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                MyViewHolder viewHolder;
                if(viewType ==0){
                    viewHolder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_viewpager, parent,false)) ;
                    ViewPager viewPager = (ViewPager) viewHolder.itemView.findViewById(R.id.banner_viewPager);
                    Indicator indicator = (Indicator) viewHolder.itemView.findViewById(R.id.banner_indicator);
                    indicator.setScrollBar(new ColorBar(parent.getContext(), Color.WHITE, 0, ScrollBar.Gravity.CENTENT_BACKGROUND));
                    viewPager.setOffscreenPageLimit(2);
                    BannerComponent bannerComponent = new BannerComponent(indicator, viewPager, false);
                    bannerComponent.setAdapter(new ImageViewgerAdapte());

                }else {
                    viewHolder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple, parent,false)) ;
                }
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 50;
            }

            @Override
            public int getItemViewType(int position) {
                if(position ==0){
                    return 0;
                }
                return 1;
            }
        });
        final ContentWraperView contentWraperView = (ContentWraperView) findViewById(R.id.contentWraperview);
        contentWraperView.setCanTopHeaderLoad(true);
        contentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                contentWraperView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentWraperView.setLoadComplete();
                    }
                },3000);
            }
        });
        contentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                contentWraperView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentWraperView.setLoadComplete();
                    }
                },3000);
            }
        });


    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

}
