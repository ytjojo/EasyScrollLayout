package com.github.ytjojo.easyscrolllayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2017/11/25 0025.
 */

public class ScrollChildWraper<E extends View> {
    E mScrollChild;
    ViewPager mViewPager;
    boolean isAutomaticHunting = true;
    final protected boolean isVerticalScrollView(View child) {
        if (child instanceof android.support.v4.view.NestedScrollingChild || child instanceof AbsListView || child instanceof ScrollView || child instanceof ViewPager || child instanceof WebView || child instanceof RecyclerView) {
            return true;
        }
        if(isVerticalScrollChild(child)){
            return true;
        }
        return false;
    }
    final protected boolean isHorizontalScrollView(View child) {
        if (child instanceof HorizontalScrollView || child instanceof ScrollView || child instanceof ViewPager || child instanceof RecyclerView) {
            return true;
        }
        if(isHorizontalScrollChild(child)){
            return true;
        }
        return false;
    }
    public boolean isVerticalScrollChild(View child){
        return false;
    }
    public boolean isHorizontalScrollChild(View child){
        return false;
    }
    public E getCurrentScrollChild(){
        return mScrollChild;
    }

    public void setScrollChild(E scrollChild){
        this.mScrollChild = scrollChild;
        isAutomaticHunting= false;

    }
    public boolean isAutomaticHunting(){
        return  isAutomaticHunting;
    }
    protected void huntingScrollChild(ViewGroup viewGroup,int orientation){

    }

    /**
     * Find out the scrollable child view from a ViewGroup.
     *
     * @param viewGroup
     */
    public void findScrollView(ViewGroup viewGroup) {
        if(viewGroup ==null){
            return;
        }
        if (isVerticalScrollView(viewGroup)) {
            return;
        }
        if (viewGroup.getChildCount() > 0) {
            int count = viewGroup.getChildCount();
            View child;
            for (int i = 0; i < count; i++) {
                child = viewGroup.getChildAt(i);
                if (isVerticalScrollView(child)) {
                    return;
                } else if (child instanceof ViewGroup) {
                    findScrollView((ViewGroup) child);
                }
            }
        }
    }

    private void findPagerInitScrollView(){

        final PagerAdapter a = mViewPager.getAdapter();
        int currentItem = mViewPager.getCurrentItem();
        if (a == null||mScrollChild !=null) {
            return;
        }
        if (a instanceof FragmentPagerAdapter) {
            FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
            Fragment item = fadapter.getItem(currentItem);
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = fsAdapter.getItem(currentItem);
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
            final EasyScrollLayout.CurrentPagerAdapter adapter = (EasyScrollLayout.CurrentPagerAdapter) a;
            if (adapter.getPrimaryItem() != null) {
                findScrollView((ViewGroup) adapter.getPrimaryItem());
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        findScrollView((ViewGroup) adapter.getPrimaryItem());
                    }
                });
            }
        }
    }

    private boolean isViewPager(View viewGroup) {
        if (viewGroup instanceof ViewPager) {
            mViewPager = (ViewPager) viewGroup;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);

            return true;
        }
        return false;
    }

    private void findViewPagerAndScrollView(ViewGroup viewGroup) {
        if (isViewPager(viewGroup)) {
        } else if (isVerticalScrollView(viewGroup)) {

        } else {
            if (viewGroup.getChildCount() > 0) {
                int count = viewGroup.getChildCount();
                View child;
                for (int i = 0; i < count; i++) {
                    child = viewGroup.getChildAt(i);
                    if (isViewPager( child)) {

                    } else if (isVerticalScrollView(child)) {

                    } else if (child instanceof ViewGroup) {
                        findViewPagerAndScrollView((ViewGroup) child);
                    }
                }
            }
        }
    }


    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mScrollChild = null;
            final PagerAdapter a = mViewPager.getAdapter();
            if (a instanceof FragmentPagerAdapter) {
                FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
                Fragment item = fadapter.getItem(position);
                findScrollView((ViewGroup) item.getView());
            } else if (a instanceof FragmentStatePagerAdapter) {
                FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
                Fragment item = fsAdapter.getItem(position);
                findScrollView((ViewGroup) item.getView());
            } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
                EasyScrollLayout.CurrentPagerAdapter currentPagerAdapter = (EasyScrollLayout.CurrentPagerAdapter)a;
                findScrollView((ViewGroup)currentPagerAdapter.getCurentView(position));

            }else{
                int  childCount = mViewPager.getChildCount();
                for (int i = 0; i < childCount ; i++) {
                    View child = mViewPager.getChildAt(i);
                    int childX = (int) child.getX();
                    int childY = (int) child.getY();
                    if(childX>=0 && childX <= mViewPager.getMeasuredWidth() & childY>=0 && childX <= mViewPager.getMinimumHeight()){
                        mScrollChild = (E) child;
                        break;
                    }
                }
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }


    };

}
