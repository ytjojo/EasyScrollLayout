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
import android.widget.ScrollView;

/**
 * Created by Administrator on 2017/11/25 0025.
 */

public class VerticalScrollCheckHandlar {
    View mScrollChild;
    ViewPager mViewPager;
    boolean isAutomaticHunting = true;
    ContentWraperView mCurContentView;

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
            if (child instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) child;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager != null && manager instanceof RecyclerView.LayoutManager) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                    if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    public boolean isVerticalScrollChild(View child) {
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

    public boolean isScrollChanged() {
        return isScrollChanged;
    }

    public boolean isAutomaticHunting() {
        return isAutomaticHunting;
    }

    public void disableAutomaticHuunting() {
        isAutomaticHunting = false;
    }

    protected void huntingScrollChild(View child) {
        if(isAutomaticHunting()){
            resetViews();
            findViewPagerAndScrollView(child, 0);
        }

    }

    private void resetViews() {
        mCurContentView = null;
        mViewPager = null;
        mScrollChild = null;
    }


    boolean isDonwEventHitScrollChild;

    public boolean isDonwEventHitScrollChild() {
        return isDonwEventHitScrollChild;
    }

    public void onDownInit(int rawX, int rawY) {
        isDonwEventHitScrollChild = HorizontalScrollHandlar.isTouchPointInView(mScrollChild, rawX, rawY);
        if (mScrollChild == null) {
            return;
        }
    }


    /**
     * Find out the scrollable child view from a ViewGroup.
     *
     * @param childParent
     */
    public boolean findScrollView(View childParent, int stackCount) {
        if (childParent == null) {
            return false;
        }
        if (isVerticalScrollView(childParent)) {
            mScrollChild = childParent;
            return true;
        }
        if (!(childParent instanceof ViewGroup)) {
            return false;
        }
        ViewGroup viewGroup = (ViewGroup) childParent;
        if (stackCount > 4) {
            return false;
        }
        if (viewGroup instanceof ContentWraperView) {
            mCurContentView = (ContentWraperView) viewGroup;
            findViewPagerAndScrollView(mCurContentView.mContentView, (++stackCount));
            mCurContentView.setScrollChild(mScrollChild);
            return true;
        }
        if (viewGroup.getChildCount() > 0) {

            int count = viewGroup.getChildCount();
            View child;
            for (int i = 0; i < count; i++) {
                child = viewGroup.getChildAt(i);
                if (findScrollView(child, (++stackCount))) {
                    return true;
                }
            }
        }
        return false;
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
            findScrollView(item.getView(), 0);
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = fsAdapter.getItem(currentItem);
            if (item == null || (!item.isAdded()) || item.isDetached() || item.getActivity() == null || item.getView() == null) {
                return;
            }
            findScrollView(item.getView(), 0);
        } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
            final EasyScrollLayout.CurrentPagerAdapter adapter = (EasyScrollLayout.CurrentPagerAdapter) a;
            if (adapter.getPrimaryItem() != null) {
                findScrollView(adapter.getPrimaryItem(), 0);
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        View child = adapter.getPrimaryItem();
                        if (child != null && child instanceof ViewGroup) {
                            findScrollView(adapter.getPrimaryItem(), 0);
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
                    mScrollChild = child;
                    break;
                }

            }
        }
    }

    private boolean isViewPager(View viewGroup) {
        if (viewGroup instanceof ViewPager) {
            return true;
        }
        return false;
    }

    private boolean findViewPagerAndScrollView(View parent, int stackCount) {
        if (parent == null) {
            return false;
        }
        if (!(parent instanceof ViewGroup)) {
            if (parent instanceof WebView) {
                mScrollChild = parent;
                return true;
            }
            return false;
        }
        ViewGroup viewGroup = (ViewGroup) parent;
        if (isViewPager(viewGroup)) {
            mViewPager = (ViewPager) viewGroup;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
            onViewpagerFound();
            return true;
        } else if (isVerticalScrollView(viewGroup)) {
            mScrollChild = viewGroup;
            return true;
        } else {
            if (stackCount > 5) {
                return false;
            }
            if (viewGroup instanceof ContentWraperView) {
                mCurContentView = (ContentWraperView) viewGroup;
                findViewPagerAndScrollView(mCurContentView.mContentView, (++stackCount));
                mCurContentView.setScrollChild(mScrollChild);
                return true;
            }
            if (viewGroup.getChildCount() > 0) {
                int count = viewGroup.getChildCount();
                View child;
                for (int i = 0; i < count; i++) {
                    child = viewGroup.getChildAt(i);
                    if (findViewPagerAndScrollView(child, (++stackCount))) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                    findScrollView(item.getView(), 0);
                } else if (a instanceof FragmentStatePagerAdapter) {
                    FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
                    Fragment item = fsAdapter.getItem(position);
                    findScrollView(item.getView(), 0);
                } else if (a instanceof EasyScrollLayout.CurrentPagerAdapter) {
                    EasyScrollLayout.CurrentPagerAdapter currentPagerAdapter = (EasyScrollLayout.CurrentPagerAdapter) a;
                    findScrollView(currentPagerAdapter.getCurentView(position), 0);

                } else {
                    int childCount = mViewPager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = mViewPager.getChildAt(i);
                        int childX = (int) child.getX() - mViewPager.getScrollX();
                        int childY = (int) child.getY() - mViewPager.getScrollY();
                        if (childX >= 0 && childX <= mViewPager.getMeasuredWidth() & childY >= 0 && childX <= mViewPager.getMinimumHeight()) {
                            findScrollView(child, 0);
                            break;
                        }

                    }
                }
            }


        }


    };

}
