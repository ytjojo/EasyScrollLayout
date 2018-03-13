package com.github.ytjojo.scrollmaster.demo;

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
        try {
            fragment =mFragments.get(position).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        map.put(position,fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
