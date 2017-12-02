package com.github.ytjojo.easyscrolllayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/2 0002.
 */

public class ContentChildHolder {

    ContentWraperView mCurContentView;
    View mDirectChild;
    boolean isDirectChildContentWraper;
    ViewPager mViewPager;
    public void onLayout(EasyScrollLayout parent){
        final int count = parent.getChildCount();

        int maxArea = 0;
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final ContentWraperView.LayoutParams lp = (ContentWraperView.LayoutParams) child.getLayoutParams();
                if (lp.mLayoutOutGravity == EasyScrollLayout.GRAVITY_OUT_INVALID) {
                    final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                    if (childArea >= maxArea) {
                        mDirectChild =  child;
                        maxArea = childArea;
                    }
                }
            }
        }
        if (mDirectChild != null) {
            if(mDirectChild instanceof ContentWraperView){
                mCurContentView = (ContentWraperView) mDirectChild;
                isDirectChildContentWraper = true;
            }else if (mDirectChild instanceof ViewPager){
                onViewpagerFound();
            }
        }
    }

    private void onViewpagerFound() {

        final PagerAdapter a = mViewPager.getAdapter();
        int currentItem = mViewPager.getCurrentItem();
        if (a == null || mViewPager.getChildCount() == 0) {
            return;
        }
        if (a instanceof FragmentPagerAdapter) {
            FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
            Fragment item = fadapter.getItem(currentItem);
            if (item == null || (!item.isAdded()) || item.isDetached() || item.getActivity() == null || item.getView() == null) {
                return;
            }
            notifyContentWraperFound((ContentWraperView) item.getView());
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = fsAdapter.getItem(currentItem);
            if (item == null || (!item.isAdded()) || item.isDetached() || item.getActivity() == null || item.getView() == null) {
                return;
            }
            notifyContentWraperFound((ContentWraperView) item.getView());
        } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
            final EasyScrollLayout.CurrentPagerAdapter adapter = (EasyScrollLayout.CurrentPagerAdapter) a;
            if (adapter.getPrimaryItem() != null) {
                notifyContentWraperFound((ContentWraperView) adapter.getPrimaryItem());
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        View child = adapter.getPrimaryItem();
                        if (child != null && child instanceof ViewGroup) {
                            notifyContentWraperFound((ContentWraperView) child);
                        }
                    }
                });
            }
        } else {
            int childCount = mViewPager.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = mViewPager.getChildAt(i);
                int childX = (int) child.getX() - mViewPager.getScrollX();
                int childY = (int) child.getY() - mViewPager.getScrollY();
                if (childX >= 0 && childX <= mViewPager.getMeasuredWidth() & childY >= 0 && childX <= mViewPager.getMinimumHeight()) {
                    notifyContentWraperFound((ContentWraperView)child);
                    break;
                }

            }
        }
    }
    public void notifyContentWraperFound(ContentWraperView view){
        this.mCurContentView = view;
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        int position;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            this.position = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                final PagerAdapter a = mViewPager.getAdapter();
                if (a instanceof FragmentPagerAdapter) {
                    FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
                    Fragment item = fadapter.getItem(position);
                    notifyContentWraperFound((ContentWraperView) item.getView());
                } else if (a instanceof FragmentStatePagerAdapter) {
                    FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
                    Fragment item = fsAdapter.getItem(position);
                    notifyContentWraperFound((ContentWraperView) item.getView());
                } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
                    EasyScrollLayout.CurrentPagerAdapter currentPagerAdapter = (EasyScrollLayout.CurrentPagerAdapter) a;
                    notifyContentWraperFound((ContentWraperView) currentPagerAdapter.getCurentView(position));

                }else {
                    int childCount = mViewPager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = mViewPager.getChildAt(i);
                        int childX = (int) child.getX() - mViewPager.getScrollX();
                        int childY = (int) child.getY() - mViewPager.getScrollY();
                        if (childX >= 0 && childX <= mViewPager.getMeasuredWidth() & childY >= 0 && childX <= mViewPager.getMinimumHeight()) {
                            notifyContentWraperFound((ContentWraperView)child );
                            break;
                        }

                    }
                }
            }


        }


    };

}
