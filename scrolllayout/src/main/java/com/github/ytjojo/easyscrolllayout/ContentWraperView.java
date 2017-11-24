package com.github.ytjojo.easyscrolllayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class ContentWraperView extends FrameLayout {

    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;
    View mStartView;
    View mEndView;
    public final static int GRAVITY_OUT_INVALID = -1;
    public final static int GRAVITY_OUT_LEFT = 0;
    public final static int GRAVITY_OUT_TOP = 1;
    public final static int GRAVITY_OUT_RIGHT = 2;
    public final static int GRAVITY_OUT_BOTTOM = 3;
    public final static int GRAVITY_INNER_TOP = 4;
    public final static int GRAVITY_INNER_BOTTOM = 5;

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
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        ContentWraperView contentWraperView = null;
        int topViewHeight = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if(child instanceof ContentWraperView){
                    contentWraperView = (ContentWraperView) child;
                    continue;
                }
                if(lp.mLayoutOutGravity != GRAVITY_OUT_INVALID){
                    switch (lp.mLayoutOutGravity){
                        case GRAVITY_INNER_TOP:
                            topViewHeight = child.getMeasuredHeight();
                            break;
                        case GRAVITY_INNER_BOTTOM:

                            break;
                        default:

                            break;
                    }
                }
            }
        }
        if(topViewHeight != 0 ){
            int heghtSize = MeasureSpec.getSize(heightMeasureSpec)-topViewHeight;
            int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
            measureChild(contentWraperView, widthMeasureSpec, childHeightSpec);
        }
        mContentViewHeight = contentWraperView.getMeasuredHeight();
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
                break;
            case GRAVITY_OUT_LEFT:
                childLeft = -width;
                childTop = 0;
                mMinHorizontalScrollRange = (int) (-width*(1f+lp.mOverScrollRatio));
                break;
            case GRAVITY_OUT_RIGHT:
                childLeft = right ;
                childTop = 0;
                mMaxHorizontalScrollRange = (int) (width*(1f+lp.mOverScrollRatio));
                break;
            case GRAVITY_OUT_BOTTOM:
                childLeft = 0 ;
                childTop = bottom;
                mMaxVerticalScrollRange = (int) (height*(1f+lp.mOverScrollRatio));
                break;
            case GRAVITY_INNER_TOP:
                childLeft = 0 ;
                childTop = 0;
                break;
            case GRAVITY_INNER_BOTTOM:
                childLeft = 0 ;
                childTop = 0;
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
}
