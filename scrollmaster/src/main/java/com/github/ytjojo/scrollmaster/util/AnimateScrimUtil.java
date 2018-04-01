package com.github.ytjojo.scrollmaster.util;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.github.ytjojo.scrollmaster.R;
import com.github.ytjojo.scrollmaster.ScrollMasterView;
import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2018/4/1 0001.
 */

public class AnimateScrimUtil {
    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 500;
    static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public static void updateScrimVisibility(View dummyView) {

    }

    public static void setUpdateScrimAbility(final ScrollMasterView masterView, final View header, final View dummyView) {
        masterView.addOnScrollListener(new ScrollMasterView.OnScollListener() {
            @Override
            public void onScroll(float offsetRatio, int positionOffsetPixels, int offsetRange) {
                updateScrimVisibility(dummyView);
                Logger.e(masterView.getLayoutStartOffsetY() + "positionOffsetPixels" + positionOffsetPixels + "offsetRange" + offsetRange);
                setScrimsShown(dummyView, positionOffsetPixels > 2 * offsetRange / 3);
            }
        });
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value. Any visibility change will be animated if
     * this view has already been laid out.
     *
     * @param shown whether the scrims should be shown
     */
    public static void setScrimsShown(View dummyView, boolean shown) {
        setScrimsShown(dummyView, shown, ViewCompat.isLaidOut(dummyView) && !dummyView.isInEditMode());
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     */
    public static void setScrimsShown(View dummyView, boolean shown, boolean animate) {
        Boolean isScrimsAreShown = (Boolean) dummyView.getTag(R.id.isscrimshow);
        if (isScrimsAreShown == null || (isScrimsAreShown.booleanValue() != shown)) {
            if (animate) {
                animateScrim(dummyView, shown ? 1f : 0f);
            } else {
                setScrimAlpha(dummyView, shown ? 1f : 0f);
            }
            dummyView.setTag(R.id.isscrimshow, shown);
        }
    }

    private static void animateScrim(final View view, @FloatRange(from = 0.0, to = 1.0) float targetAlpha) {
        ValueAnimator mScrimAnimator = (ValueAnimator) view.getTag(R.id.scrimAnimator);
        if (mScrimAnimator == null) {
            mScrimAnimator = new ValueAnimator();
            mScrimAnimator.setDuration(DEFAULT_SCRIM_ANIMATION_DURATION);
            mScrimAnimator.setInterpolator(
                    targetAlpha > view.getAlpha()
                            ? FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
//                    if(!ViewCompat.isInLayout(view)){
//                        animator.cancel();
//                        return;
//                    }
                    setScrimAlpha(view, (float) animator.getAnimatedValue());
                }
            });
            view.setTag(R.id.scrimAnimator, mScrimAnimator);
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setFloatValues(view.getAlpha(), targetAlpha);
        mScrimAnimator.start();
    }

    private static void setScrimAlpha(View view, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (alpha != view.getAlpha()) {
            view.setAlpha(alpha);
        }
    }

    public static void setScrimBackgroud(ScrollMasterView masterView, final View actionbar, @ColorInt int color) {
        ColorDrawable drawable = new ColorDrawable(color);
        drawable.setAlpha(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            actionbar.setBackground(drawable);
        }else {
            actionbar.setBackgroundDrawable(drawable);
        }
        masterView.addOnScrollListener(new ScrollMasterView.OnScollListener() {
            @Override
            public void onScroll(float offsetRatio, int positionOffsetPixels, int offsetRange) {
                boolean shown = positionOffsetPixels == offsetRange;
                Boolean isScrimsAreShown = (Boolean) actionbar.getTag(R.id.isscrimshow);
                if (isScrimsAreShown == null || (isScrimsAreShown.booleanValue() != shown)) {
                    animateBackgroud(actionbar, shown ? 0xFF : 0x0);
                    actionbar.setTag(R.id.isscrimshow, shown);
                }
            }
        });
    }

    public static void animateBackgroud(final View view, final @IntRange(from = 0, to = 255) int targetAlpha) {
        ValueAnimator mScrimAnimator = (ValueAnimator) view.getTag(R.id.scrimAnimator);
        final ColorDrawable drawable = (ColorDrawable) view.getBackground();
        if (mScrimAnimator == null) {
            mScrimAnimator = new ValueAnimator();
            mScrimAnimator.setDuration(DEFAULT_SCRIM_ANIMATION_DURATION);
            mScrimAnimator.setInterpolator(
                    targetAlpha > drawable.getAlpha()
                            ? FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {

                    int alpha = (int) animator.getAnimatedValue();
                    if (drawable.getAlpha() != alpha) {
                        drawable.setAlpha(alpha);
                        drawable.invalidateSelf();
                    }
                }
            });
            view.setTag(R.id.scrimAnimator, mScrimAnimator);
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(drawable.getAlpha(), targetAlpha);
        mScrimAnimator.start();
    }
}
