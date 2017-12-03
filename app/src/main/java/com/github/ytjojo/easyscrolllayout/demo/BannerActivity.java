package com.github.ytjojo.easyscrolllayout.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;

import java.util.HashMap;

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
        bannerComponent.setAdapter(adapter);

        //默认就是800毫秒，设置单页滑动效果的时间
//        bannerComponent.setScrollDuration(800);
        //设置播放间隔时间，默认情况是3000毫秒
        bannerComponent.setAutoPlayTime(2500);

        ViewPager mainviewPager = (ViewPager) findViewById(R.id.viewpager);

        mainviewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            HashMap<Integer,Fragment> map =new HashMap<>();
            @Override
            public Fragment getItem(int position) {
                Fragment fragment = map.get(position);
                if(fragment != null){

                    return  fragment;
                }
                switch (position){
                    case 0:

                        fragment =   new RecyclerViewFragment();
                        break;
                    case 1:

                        fragment =   new ScrollViewFragment();
                        break;
                    case 2:

                        fragment =   new NestedScrollFragment();
                        break;
                    case 3:

                        fragment =   new WebViewFragment();
                        break;
                }
                map.put(position,fragment);
                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }
        });
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

    private int[] images = {R.mipmap.p1, R.mipmap.p2, R.mipmap.p3, R.mipmap.p4};

    private IndicatorViewPager.IndicatorViewPagerAdapter adapter = new IndicatorViewPager.IndicatorViewPagerAdapter() {

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = new View(container.getContext());
            }
            return convertView;
        }

        @Override
        public View getViewForPage(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = new ImageView(getApplicationContext());
                convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            ImageView imageView = (ImageView) convertView;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(images[position]);
            return convertView;
        }

//        @Override
//        public int getItemPosition(Object object) {
//            return RecyclingPagerAdapter.POSITION_NONE;
//        }

        @Override
        public int getCount() {
            return images.length;
        }
    };
}