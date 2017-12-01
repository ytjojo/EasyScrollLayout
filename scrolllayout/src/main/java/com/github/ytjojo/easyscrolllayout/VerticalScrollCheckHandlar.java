package com.github.ytjojo.easyscrolllayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
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

public class VerticalScrollCheckHandlar {
    View mScrollChild;
    ViewPager mViewPager;
    boolean isAutomaticHunting = true;

    public boolean reachChildTop() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChild, -1);
    }

    public boolean reachChildBottom() {
        if (mScrollChild == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChild, 1);
    }

    final protected boolean isVerticalScrollView(View child) {
        if (isVerticalScrollChild(child)) {
            return true;
        }
        if (child instanceof android.support.v4.view.NestedScrollingChild || child instanceof AbsListView || child instanceof ScrollView || child instanceof ViewPager || child instanceof WebView || child instanceof RecyclerView) {
            if(child instanceof RecyclerView){
                RecyclerView recyclerView = (RecyclerView) child;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if(manager !=null && manager instanceof RecyclerView.LayoutManager ){
                    LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                    if(layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    final protected boolean isHorizontalScrollView(View child) {
        if (child instanceof HorizontalScrollView || child instanceof ScrollView || child instanceof ViewPager || child instanceof RecyclerView) {
            return true;
        }
        if (isHorizontalScrollChild(child)) {
            return true;
        }
        return false;
    }

    public boolean isVerticalScrollChild(View child) {
        return false;
    }

    public boolean isHorizontalScrollChild(View child) {
        return false;
    }

    public View getCurrentScrollChild() {
        return mScrollChild;
    }

    public void setScrollChild(View scrollChild) {
        this.mScrollChild = scrollChild;
        isAutomaticHunting = false;

    }

    boolean isScrollChanged;

    protected void resetChildScrollChange() {
        isScrollChanged = false;
    }

    public void notifyChildScrollChaged() {
        isScrollChanged = true;
    }
    public boolean isScrollChanged(){
        return isScrollChanged;
    }

    public boolean isAutomaticHunting() {
        return isAutomaticHunting;
    }

    protected void huntingScrollChild(View child) {
        findViewPagerAndScrollView(child);
    }

    View mScrollviewDirectChild;
    int mLastTop;

    public void childScrollConsumed(int[] consumed) {
        consumed[0] = consumed[1] = 0;
        if (mScrollChild == null) {
            return;
        }
        if (mScrollChild instanceof ViewGroup) {
            ViewGroup scrollView = (ViewGroup) mScrollChild;
            if (scrollView.getChildCount() == 0) {
                return;
            }

            if (mScrollviewDirectChild != null) {
                consumed[1] = mScrollviewDirectChild.getTop() - mLastTop;
            } else {

            }
            int index = scrollView.getChildCount() / 2;
            mScrollviewDirectChild = scrollView.getChildAt(index);
            mLastTop = mScrollviewDirectChild.getTop();
        }


    }
    boolean isDonwEventHitScrollChild;
    public boolean isDonwEventHitScrollChild(){
        return isDonwEventHitScrollChild;
    }
    public void onDownInit(int rawX,int rawY) {
        isDonwEventHitScrollChild = HorizontalScrollHandlar.isTouchPointInView(mScrollChild,rawX,rawY);
        if (mScrollChild == null) {
            return;
        }
        if (mScrollChild instanceof ViewGroup) {
            ViewGroup scrollView = (ViewGroup) mScrollChild;
            if (scrollView.getChildCount() == 0) {
                return;
            }
            int index = scrollView.getChildCount() / 2;
            mScrollviewDirectChild = scrollView.getChildAt(index);
            mLastTop = mScrollviewDirectChild.getTop();
        }
    }


    /**
     * Find out the scrollable child view from a ViewGroup.
     *
     * @param viewGroup
     */
    public void findScrollView(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        if (isVerticalScrollView(viewGroup)) {
            mScrollChild = viewGroup;
            return;
        }
        if (viewGroup.getChildCount() > 0) {
            int count = viewGroup.getChildCount();
            View child;
            for (int i = 0; i < count; i++) {
                child = viewGroup.getChildAt(i);
                if (isVerticalScrollView(child)) {
                    mScrollChild = viewGroup;
                    return;
                } else if (child instanceof ViewGroup) {
                    findScrollView((ViewGroup) child);
                }
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
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = fsAdapter.getItem(currentItem);
            if (item == null || (!item.isAdded()) || item.isDetached() || item.getActivity() == null || item.getView() == null) {
                return;
            }
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
            final EasyScrollLayout.CurrentPagerAdapter adapter = (EasyScrollLayout.CurrentPagerAdapter) a;
            if (adapter.getPrimaryItem() != null) {
                findScrollView((ViewGroup) adapter.getPrimaryItem());
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        View child = adapter.getPrimaryItem();
                        if (child != null && child instanceof ViewGroup) {
                            findScrollView((ViewGroup) child);
                        }
                    }
                });
            }
        } else {

        }
    }

    private boolean isViewPager(View viewGroup) {
        if (viewGroup instanceof ViewPager) {
            return true;
        }
        return false;
    }

    private void findViewPagerAndScrollView(View parent) {
        if (!(parent instanceof ViewGroup)) {
            if (parent instanceof WebView) {
                mScrollChild = parent;
            }
            return;
        }
        ViewGroup viewGroup = (ViewGroup) parent;
        if (isViewPager(viewGroup)) {
            mViewPager = (ViewPager) viewGroup;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
            onViewpagerFound();
        } else if (isVerticalScrollView(viewGroup)) {
            mScrollChild = viewGroup;
        } else {
            if (viewGroup.getChildCount() > 0) {
                int count = viewGroup.getChildCount();
                View child;
                for (int i = 0; i < count; i++) {
                    child = viewGroup.getChildAt(i);
                    if (child instanceof WebView) {
                        mScrollChild = child;
                        return;
                    } else if (isViewPager(child)) {
                        mViewPager = (ViewPager) viewGroup;
                        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
                        onViewpagerFound();
                    } else if (isVerticalScrollView(child)) {
                        mScrollChild = child;
                    } else if (child instanceof ViewGroup) {
                        findViewPagerAndScrollView((ViewGroup) child);
                    }
                }
            }
        }
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
                    EasyScrollLayout.CurrentPagerAdapter currentPagerAdapter = (EasyScrollLayout.CurrentPagerAdapter) a;
                    findScrollView((ViewGroup) currentPagerAdapter.getCurentView(position));

                }
                if (mScrollChild == null) {
                    int childCount = mViewPager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = mViewPager.getChildAt(i);
                        int childX = (int) child.getX();
                        int childY = (int) child.getY();
                        if (childX >= 0 && childX <= mViewPager.getMeasuredWidth() & childY >= 0 && childX <= mViewPager.getMinimumHeight()) {
                            mScrollChild = (View) child;
                            break;
                        }
                    }
                }
            }


        }


    };

}
