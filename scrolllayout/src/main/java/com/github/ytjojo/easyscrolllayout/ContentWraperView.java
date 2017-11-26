package com.github.ytjojo.easyscrolllayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.orhanobut.logger.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class ContentWraperView extends FrameLayout {


    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;
    public final static int GRAVITY_OUT_INVALID = -1;
    public final static int GRAVITY_OUT_TOP = 1;
    public final static int GRAVITY_OUT_BOTTOM = 2;
    public final static int GRAVITY_INNER_BOTTOM = 3;

    public ContentWraperView(Context context) {
        this(context,null);
    }

    public ContentWraperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ContentWraperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l,t,r,b,false);
    }
    int mContentViewHeight;
    View mContentView;
    View mInnerBottomView;
    View mOutTopView;
    View mOutBottomView;
    private int mOrientation;

    public void setOrientation(@EasyScrollLayout.OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;

        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();

        ArrayList<View> contentViews =new ArrayList<>(count);
        int maxArea =0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if(lp.mLayoutOutGravity != GRAVITY_OUT_INVALID){
                    final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                    maxArea = childArea> maxArea?childArea:maxArea;
                    switch (lp.mLayoutOutGravity){
                        case GRAVITY_INNER_BOTTOM:
                            mInnerBottomView = child;
                            break;

                        default:

                            break;
                    }
                }else {
                   contentViews.add(child);
                }
            }
        }
        if(mContentView == null){
           if(!contentViews.isEmpty()){
               int contentViewsCount = contentViews.size();
               for (int i = 0; i < contentViewsCount ; i++) {
                   final View child = getChildAt(i);
                   final int childArea = child.getMeasuredHeight() * child.getMeasuredWidth();
                   if(childArea > maxArea){
                       mContentView = child;
                       maxArea = childArea;
                   }
               }

           }
        }
        if(mContentView !=null && mContentView instanceof ViewGroup && mScrollChildHandlar.isAutomaticHunting()){
            mScrollChildHandlar.huntingScrollChild((ViewGroup) mContentView);
            Logger.e(mScrollChildHandlar.getCurrentScrollChild().getClass().getName());
        }
    }
    int mMinVerticalScrollRange;
    int mMaxVerticalScrollRange;
    int mMinHorizontalScrollRange;
    int mMaxHorizontalScrollRange;


    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if(lp.mLayoutOutGravity != GRAVITY_OUT_INVALID){
                    layoutChildOuter(child,lp,left,top,right,bottom);
                    continue;
                }
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }
    private void layoutChildOuter(View child , LayoutParams lp,int left, int top, int right, int bottom){
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft = 0;
        int childTop = 0;

        switch (lp.mLayoutOutGravity){
            case GRAVITY_OUT_TOP:
                childLeft = 0;
                childTop = -height;
                mMinVerticalScrollRange = (int) (-height*(1f+lp.mOverScrollRatio));
                lp.mMaxScrollY = mMinVerticalScrollRange;
                lp.mMinScrollY = 0;
                break;
            case GRAVITY_OUT_BOTTOM:
                childLeft = 0 ;
                int offset = 0;
                childTop = bottom+(mInnerBottomView==null?0:mInnerBottomView.getMeasuredHeight());
                int scrollRange = childTop +(int) (height*(1f+lp.mOverScrollRatio)) - (bottom-top);
                mMaxVerticalScrollRange = Math.max(scrollRange,mMaxVerticalScrollRange);
                lp.mMaxScrollY = scrollRange;
                lp.mMinScrollY = childTop  - (bottom-top);
                break;
            case GRAVITY_INNER_BOTTOM:
                childLeft = 0 ;
                childTop = bottom;
                lp.mMinScrollY =0;
                lp.mMaxScrollY = child.getMeasuredHeight();
                break;
           default:
                break;
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
    }




    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        float mOverScrollRatio = 0.7f;
        int mMinScrollY;
        int mMaxScrollY;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        /**
         * Copy constructor. Clones the width, height, margin values, and
         * gravity of the source.
         *
         * @param source The layout params to copy from.
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
        }
        int mLayoutOutGravity = GRAVITY_OUT_INVALID;

    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        if(y > mMaxVerticalScrollRange){
            y = mMaxHorizontalScrollRange;
        }
        if(y < mMinVerticalScrollRange){
            y = mMinVerticalScrollRange;
        }
        if(x < mMinHorizontalScrollRange){
            x = mMinHorizontalScrollRange;
        }
        if(x > mMaxHorizontalScrollRange){
            x = mMaxHorizontalScrollRange;
        }
        int lastScrollx = getScrollX();
        int lastScrolly = getScrollY();
        if(x != lastScrollx || y != lastScrolly){
            super.scrollTo(x, y);
        }
    }

    ScrollChildHandlar mScrollChildHandlar =new ScrollChildHandlar();
    public boolean reachChildTop() {
        if (mScrollChildHandlar.getCurrentScrollChild() == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChildHandlar.getCurrentScrollChild() , -1);
    }

    public boolean reachChildBottom() {
        if (mScrollChildHandlar.getCurrentScrollChild()  == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollChildHandlar.getCurrentScrollChild() , 1);
    }
}
