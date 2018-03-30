package com.github.ytjojo.scrollmaster.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.gxz.PagerSlidingTabStrip;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/11/30 0030.
 */

public class DL_VP_HD_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dl_vp_hd);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tab);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
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

            @Override
            public CharSequence getPageTitle(int position) {

                switch (position){
                    case 0:
                    return "RecycleView";
                    case 1:
                    return "ScrollView";
                    case 2:
                    return "NestedScrollView";
                    case 3:
                    return "WebView";

                }
                return super.getPageTitle(position);
            }
        });
        pagerSlidingTabStrip.setViewPager(viewPager);
    }
}
