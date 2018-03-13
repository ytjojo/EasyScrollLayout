package com.github.ytjojo.scrollmaster;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2018/1/6 0006.
 */

public class PendulumView extends View {
    private Paint mPaint;
    private Paint mDotPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    Pendulum mPendulum;
    int mHeight;
    int mWidth;
    private ValueAnimator mRepeateAnimator;

    public PendulumView(Context context) {
        this(context,null);
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mPaint = new Paint();
        mPaint.setColor(0xffdaa9fa);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mDotPaint = new Paint();
        mDotPaint.setColor(0xffdaa9fa);
        mDotPaint.setAntiAlias(true);
        mDotPaint.setStyle(Paint.Style.FILL);

        mPendulum = new Pendulum();
        mRadius = 20;
        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                startViewMotion();
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
        mPendulum.initDotCollections();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        mHeight = h;
        mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(mCenterX,3*mCenterY/4);
        canvas.drawCircle(mPendulum.getOffsetX() * 25,mPendulum.getOffsetY()* 25,mRadius,mPaint);
        mPendulum.drawInnerDotsFrame(canvas,mDotPaint);
        canvas.restore();
    }


    private void startViewMotion() {
        if (mRepeateAnimator != null && mRepeateAnimator.isRunning())
            return;
        mRepeateAnimator = ValueAnimator.ofFloat(0f, 1f);
        mRepeateAnimator.setDuration(10000);
        mRepeateAnimator.setRepeatCount(-1);
        mRepeateAnimator.setInterpolator(new LinearInterpolator());//需要随时间匀速变化
        mRepeateAnimator.start();
        mRepeateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPendulum.physicsUpdate();
                mPendulum.updateDotCollections();
                invalidate();

            }
        });


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRepeateAnimator != null&& mRepeateAnimator.isRunning()){
            mRepeateAnimator.cancel();
            mRepeateAnimator.removeAllListeners();
        }

    }

}
