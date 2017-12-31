package com.github.ytjojo.easyscrolllayout;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;


/**
 * Created
 * by jaren on 2017/5/26.
 */

public class LikeView extends FrameLayout implements UIHandler {
    /**
     * 圆最大半径（心形）
     */
    private float mRadius;
    /**
     * View变化用时
     */
    private int mCycleTime;
    /**
     * Bézier曲线画圆的近似常数
     */
    private static final float c = 0.551915024494f;
    /**
     * 环绕圆点的颜色
     */
    private static final int[] dotColors = {0xffdaa9fa, 0xfff2bf4b, 0xffe3bca6, 0xff329aed, 0xffb1eb99, 0xff67c9ad, 0xffde6bac};

    private float mCenterX;
    private float mCenterY;
    private Paint mPaint;
    private float mOffset;
    private ValueAnimator mRepeateAnimator;

    ImageView mImageView;

    public LikeView(Context context) {
        this(context, null);
    }

    public LikeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LikeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        mRadius = dp2px(30);
        mCycleTime = 1600;
        mOffset = c * mRadius;
        mCenterX = mRadius;
        mCenterY = mRadius;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        setWillNotDraw(false);


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (child instanceof ImageView) {
                mImageView = (ImageView) child;
            }
        }
        if (mImageView == null) {
            mImageView = new ImageView(getContext());
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER;
            this.addView(mImageView, lp);
        }
//        mImageView.setPivotX(0.5f);
//        mImageView.setPivotY(0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mCenterX, mCenterY);//使坐标原点在canvas中心位置
        drawOuterDotsFrame(canvas);
        drawInnerDotsFrame(canvas);
        drawRing(canvas);
        canvas.translate(-mCenterX,-mCenterY);

    }


    //绘制圆
    private void drawCircle(Canvas canvas, int radius, int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0f, 0f, radius, mPaint);
    }


    //绘制圆环
    private void drawRing(Canvas canvas) {
        canvas.save();
        mPaint.setColor(mCircleColor);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(0f, 0f, mOuterCircleRadiusProgress * mRadius, mPaint);
        Logger.e(mOuterCircleRadiusProgress + "mOuterCircleRadiusProgress");
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawCircle(0, 0, mInnerCircleRadiusProgress * mRadius + 1, mPaint);
        Logger.e(mInnerCircleRadiusProgress + "mInnerCircleRadiusProgress");
        mPaint.setXfermode(null);
        canvas.restore();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        maxDotSize = 8;
        maxOuterDotsRadius = Math.min(w, h) / 2 - maxDotSize * 2;
        maxInnerDotsRadius = 0.8f * maxOuterDotsRadius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMinimumHeight((int) (3 * mRadius));
        setMinimumHeight((int) (3 * mRadius));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


    /**
     * 展现View点击后的变化效果
     */
    private void startViewMotion() {
        if (mRepeateAnimator != null && mRepeateAnimator.isRunning())
            return;
        mRepeateAnimator = ValueAnimator.ofFloat(0f, 1f,2f);
        mRepeateAnimator.setDuration(mCycleTime);
        mRepeateAnimator.setRepeatCount(-1);
        mRepeateAnimator.setInterpolator(new LinearInterpolator());//需要随时间匀速变化
        mRepeateAnimator.start();
        mRepeateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                if(animatedValue > 1f){
                    setCurrentProgress(animatedValue*0.6f-0.3f);
                    animatedValue -=1f;
                    if(animatedValue <= 0.5f){
                        mImageView.setScaleX(1f+0.2f*(animatedValue)/0.5f);
                        mImageView.setScaleY(1f+0.2f*(animatedValue)/0.5f);

                    }else {
                        mImageView.setScaleX(1.2f-0.2f*(animatedValue-0.5f)/0.5f);
                        mImageView.setScaleY(1.2f-0.2f*(animatedValue-0.5f)/0.5f);
                    }
                }else {

                    if(animatedValue <= 0.5f){
                        mImageView.setScaleX(1f-0.2f*(animatedValue)/0.5f);
                        mImageView.setScaleY(1f-0.2f*(animatedValue)/0.5f);
                        setCurrentProgress(0f);

                    }else {
                        mImageView.setScaleX(0.8f+0.2f*(animatedValue-0.5f)/0.5f);
                        mImageView.setScaleY(0.8f+0.2f*(animatedValue-0.5f)/0.5f);
                        setCurrentProgress((animatedValue-0.5f)*0.2f/0.5f+0.1f);
                    }
                }


                invalidate();

            }
        });


    }

    /**
     * 重置为初始状态
     */
    private void resetState() {
        mOuterCircleRadiusProgress = 0f;
        mInnerCircleRadiusProgress = 0f;
        setCurrentProgress(0f);
        if(mImageView != null){
            mImageView.setScaleX(0f);
            mImageView.setScaleY(0f);
        }


    }

    private float calcPercent(float start, float end, float current) {
        return (current - start) / (end - start);
    }


    private float dp2px(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mRepeateAnimator != null&& mRepeateAnimator.isRunning()){
            mRepeateAnimator.cancel();
            mRepeateAnimator.removeAllListeners();
        }


    }

    @Override
    public void onUIReset(BaseRefreshIndicator indicator) {
        resetState();
    }

    @Override
    public void onUIRefreshPrepare(BaseRefreshIndicator indicator) {
        //开始下拉了之回调一次
    }

    @Override
    public void onUIRefreshBegin(BaseRefreshIndicator indicator) {
        startViewMotion();
    }

    @Override
    public void onUIRefreshComplete(BaseRefreshIndicator indicator) {
        if(mRepeateAnimator != null && mRepeateAnimator.isRunning()){
            mRepeateAnimator.cancel();
        }
    }

    @Override
    public void onUIReleaseBeforeRefresh(BaseRefreshIndicator indicator) {

    }

    @Override
    public void onUIScrollChanged(BaseRefreshIndicator indicator, int scrollValue, byte status) {
        if (indicator.getStatus() == BaseRefreshIndicator.PTR_STATUS_PREPARE) {
            if (scrollValue <= Math.abs(indicator.getStableValue() - indicator.getLimitValue())) {
                float percent = Math.abs(scrollValue * 1f / (indicator.getStableValue() - indicator.getLimitValue()));
                float ppppp = 0.5f;
                if (percent <= ppppp) {
                    float percent1 = percent / ppppp;
                    setOuterCircleRadiusProgress(DECCELERATE_INTERPOLATOR.getInterpolation(percent1));
                    if (percent >= 0.1f) {
                        setCurrentProgress(ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation(percent - 0.1f));

                    } else {
                        setInnerCircleRadiusProgress(0f);
                    }
                    if (percent >= 0.4f) {
                        setInnerCircleRadiusProgress(DECCELERATE_INTERPOLATOR.getInterpolation((percent - 0.4f) / percent));
                    }
                    mImageView.setScaleX(0f);
                    mImageView.setScaleY(0f);
                } else {
                    if (percent <= 0.9f) {
                        setInnerCircleRadiusProgress(DECCELERATE_INTERPOLATOR.getInterpolation((percent - 0.4f) / ppppp));
                    }
                    setCurrentProgress(ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation(percent - 0.1f));
                    float scaleXY = (percent-ppppp)/ppppp;
                    scaleXY = OVERSHOOT_INTERPOLATOR.getInterpolation(scaleXY )*0.8f+0.2f;
                    Logger.e(scaleXY + "scaleXY" +percent);
                    mImageView.setScaleX(scaleXY);
                    mImageView.setScaleY(scaleXY);
                }


            } else {
                float percent = (0f + scrollValue - Math.abs(indicator.getStableValue() - indicator.getLimitValue())) / (indicator.getOverScrollValue() - indicator.getStableValue());
                if (percent < 0f) {
                    percent = -percent;
                }
                invalidate();
                Logger.e("percent" + percent);
            }

        }
    }


    private static final int DOTS_COUNT = 7;
    private static final int OUTER_DOTS_POSITION_ANGLE = 51;

    private int COLOR_1 = 0xFFFFC107;
    private int COLOR_2 = 0xFFFF9800;
    private int COLOR_3 = 0xFFFF5722;
    private int COLOR_4 = 0xFFF44336;

    private final Paint[] circlePaints = new Paint[4];


    private float maxOuterDotsRadius;
    private float maxInnerDotsRadius;
    private float maxDotSize;

    private float currentProgress = 0;

    private float currentRadius1 = 0;
    private float currentDotSize1 = 0;

    private float currentDotSize2 = 0;
    private float currentRadius2 = 0;


    private void init() {
        for (int i = 0; i < circlePaints.length; i++) {
            circlePaints[i] = new Paint();
            circlePaints[i].setStyle(Paint.Style.FILL);
            circlePaints[i].setAntiAlias(true);
        }
    }


    private void drawOuterDotsFrame(Canvas canvas) {
        for (int i = 0; i < DOTS_COUNT; i++) {
            int cX = (int) (currentRadius1 * Math.cos(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180));
            int cY = (int) (currentRadius1 * Math.sin(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180));
            canvas.drawCircle(cX, cY, currentDotSize1, circlePaints[i % circlePaints.length]);
        }
    }

    private void drawInnerDotsFrame(Canvas canvas) {
        for (int i = 0; i < DOTS_COUNT; i++) {
            int cX = (int) (currentRadius2 * Math.cos((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
            int cY = (int) (currentRadius2 * Math.sin((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
            canvas.drawCircle(cX, cY, currentDotSize2, circlePaints[(i + 1) % circlePaints.length]);
        }
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;

        updateInnerDotsPosition();
        updateOuterDotsPosition();
        updateDotsPaints();
        updateDotsAlpha();

        postInvalidate();
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    private void updateInnerDotsPosition() {
        if (currentProgress < 0.3f) {
            this.currentRadius2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0, 0.3f, 0.f, maxInnerDotsRadius);
        } else {
            this.currentRadius2 = maxInnerDotsRadius;
        }
        if (currentProgress == 0) {
            this.currentDotSize2 = 0;
        } else if (currentProgress < 0.2) {
            this.currentDotSize2 = maxDotSize;
        } else if (currentProgress < 0.5) {
            this.currentDotSize2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.2f, 0.5f, maxDotSize, 0.3 * maxDotSize);
        } else {
            this.currentDotSize2 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.5f, 1f, maxDotSize * 0.3f, 0);
        }

    }

    private void updateOuterDotsPosition() {
        if (currentProgress < 0.3f) {
            this.currentRadius1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.0f, 0.3f, 0, maxOuterDotsRadius * 0.8f);
        } else {
            this.currentRadius1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.3f, 1f, 0.8f * maxOuterDotsRadius, maxOuterDotsRadius);
        }
        if (currentProgress == 0) {
            this.currentDotSize1 = 0;
        } else if (currentProgress < 0.7) {
            this.currentDotSize1 = maxDotSize;
        } else {
            this.currentDotSize1 = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.7f, 1f, maxDotSize, 0);
        }
    }

    private void updateDotsPaints() {
        if (currentProgress < 0.5f) {
            float progress = (float) Utils.mapValueFromRangeToRange(currentProgress, 0f, 0.5f, 0, 1f);
            circlePaints[0].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_1, COLOR_2));
            circlePaints[1].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_2, COLOR_3));
            circlePaints[2].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_3, COLOR_4));
            circlePaints[3].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_4, COLOR_1));
        } else {
            float progress = (float) Utils.mapValueFromRangeToRange(currentProgress, 0.5f, 1f, 0, 1f);
            circlePaints[0].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_2, COLOR_3));
            circlePaints[1].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_3, COLOR_4));
            circlePaints[2].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_4, COLOR_1));
            circlePaints[3].setColor((Integer) argbEvaluator.evaluate(progress, COLOR_1, COLOR_2));
        }
    }

    public void setColors(@ColorInt int primaryColor, @ColorInt int secondaryColor) {
        COLOR_1 = primaryColor;
        COLOR_2 = secondaryColor;
        COLOR_3 = primaryColor;
        COLOR_4 = secondaryColor;
        invalidate();
    }

    private void updateDotsAlpha() {
        float progress = (float) Utils.clamp(currentProgress, 0.6f, 1f);
        int alpha = (int) Utils.mapValueFromRangeToRange(progress, 0.6f, 1f, 255, 0);
        circlePaints[0].setAlpha(alpha);
        circlePaints[1].setAlpha(alpha);
        circlePaints[2].setAlpha(alpha);
        circlePaints[3].setAlpha(alpha);
    }

    private int START_COLOR = 0xFFFF5722;
    private int END_COLOR = 0xFFFFC107;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();


    private float mOuterCircleRadiusProgress = 0f;
    private float mInnerCircleRadiusProgress = 0f;


    public void setInnerCircleRadiusProgress(float innerCircleRadiusProgress) {
        this.mInnerCircleRadiusProgress = innerCircleRadiusProgress;
        postInvalidate();
    }

    public float getInnerCircleRadiusProgress() {
        return mInnerCircleRadiusProgress;
    }

    public void setOuterCircleRadiusProgress(float outerCircleRadiusProgress) {
        this.mOuterCircleRadiusProgress = outerCircleRadiusProgress;
        updateCircleColor();
        postInvalidate();
    }

    int mCircleColor;

    private void updateCircleColor() {
        float colorProgress = (float) Utils.clamp(mOuterCircleRadiusProgress, 0.5, 1);
        colorProgress = (float) Utils.mapValueFromRangeToRange(colorProgress, 0.5f, 1f, 0f, 1f);
        mCircleColor = (Integer) argbEvaluator.evaluate(colorProgress, START_COLOR, END_COLOR);
    }

    public float getOuterCircleRadiusProgress() {
        return mOuterCircleRadiusProgress;
    }


    public void setStartColor(@ColorInt int color) {
        START_COLOR = color;
        invalidate();
    }

    public void setEndColor(@ColorInt int color) {
        END_COLOR = color;
        invalidate();
    }

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
}