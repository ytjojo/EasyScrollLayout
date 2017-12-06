package com.github.ytjojo.easyscrolllayout.demo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class ViewpagerFragmentAdapter extends FragmentStatePagerAdapter {

    ArrayList<Class<? extends Fragment>> mFragments;
    HashMap<Integer,Fragment> map =new HashMap<>();
    public ViewpagerFragmentAdapter(FragmentManager fm, ArrayList<Class<? extends Fragment>> fragments) {
        super(fm);
        this.mFragments =fragments;
    }

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
        return mFragments.size();
    }
}
