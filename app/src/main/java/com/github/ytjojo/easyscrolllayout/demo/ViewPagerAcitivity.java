package com.github.ytjojo.easyscrolllayout.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.github.ytjojo.easyscrolllayout.BaseRefreshIndicator;
import com.github.ytjojo.easyscrolllayout.EasyScrollLayout;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/11/26 0026.
 */

public class ViewPagerAcitivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpage);
        final EasyScrollLayout easyScrollLayout = (EasyScrollLayout) findViewById(R.id.easyScrolllayout);
        easyScrollLayout.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setTopHeaderLoadComplete();
                    }
                },4000);
            }
        });
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

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
        });
    }
}
