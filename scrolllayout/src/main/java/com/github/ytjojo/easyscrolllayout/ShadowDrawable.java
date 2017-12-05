package com.github.ytjojo.easyscrolllayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.animation.Interpolator;

import static com.github.ytjojo.easyscrolllayout.ScrimUtil.constrain;

/**
 * https://github.com/MasayukiSuda/EasingInterpolator
 */
public class ShadowDrawable extends Drawable {


    private Paint mPaint;

    private RectF mBounds;

    private int mWidth;
    private int mHeight;

    private int mShadowOffset;


    /**
     * 阴影颜色
     */
    private int mShadowColor = 0x88000000;
    /**
     * 阴影半径
     */
    private float mShadowRadius;


    private int mShadowGravity = -1;
    Interpolator mInterpolator;
    int mNumStops = 4;

    public ShadowDrawable(Context context, int gravity) {
        mShadowOffset = 0;
        mShadowGravity = gravity;
        setShadowRadius(dip2px(30, context));
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        /**
         * 解决旋转时的锯齿问题
         */
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mShadowColor);

//        mPaint.setStrokeWidth(mLineWidth);
        /**
         * 设置阴影
         */
//        mPaint.setShadowLayer(getShadowRadius(), 0, 0, getShadowColor());

        mBounds = new RectF();
    }

    SparseArray<LinearGradient> mGradientSparseArray;

    private void setGravity(int gravity) {
        if (mShadowGravity == gravity&&mBounds.isEmpty()) {
            return;
        }
        mShadowGravity = gravity;
        if (mGradientSparseArray == null) {
            mGradientSparseArray = new SparseArray<>(4);
        }
        final int[] stopColors = new int[mNumStops];

        int red = Color.red(mShadowColor);
        int green = Color.green(mShadowColor);
        int blue = Color.blue(mShadowColor);
        int alpha = Color.alpha(mShadowColor);

        for (int i = 0; i < mNumStops; i++) {
            float x = i * 1f / (mNumStops - 1);
            float opacity = 1f;
            if (mInterpolator == null) {
                opacity = constrain((float) Math.pow(x, 3), 0, 1);

            } else {
                constrain(mInterpolator.getInterpolation(x), 0, 1);
            }
            stopColors[i] = Color.argb((int) (alpha * opacity), red, green, blue);
        }

        final float x0, x1, y0, y1;
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                x0 = mShadowRadius;
                x1 = 0;
                break;
            case Gravity.RIGHT:
                x0 = 0;
                x1 = mShadowRadius;
                break;
            default:
                x0 = 0;
                x1 = 0;
                break;
        }
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                y0 = mShadowRadius;
                y1 = 0;
                break;
            case Gravity.BOTTOM:
                y0 = 0;
                y1 = mShadowRadius;
                break;
            default:
                y0 = 0;
                y1 = 0;
                break;
        }
//        ArgbEvaluator evaluator = new ArgbEvaluator();
        LinearGradient gradient= mGradientSparseArray.get(mShadowGravity);
        if(gradient ==null){
            gradient =new LinearGradient(
                    x0,
                    y0,
                    x1,
                    y1,
                    stopColors, null,
                    Shader.TileMode.CLAMP);
            mGradientSparseArray.put(mShadowGravity,gradient);
        }
        mPaint.setShader(gradient);

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mBounds.left = bounds.left;
        this.mBounds.right = bounds.right;
        this.mBounds.top = bounds.top;
        this.mBounds.bottom = bounds.bottom;
        mWidth = (int) (this.mBounds.right - this.mBounds.left);
        mHeight = (int) (this.mBounds.bottom - this.mBounds.top);
        setGravity(mShadowGravity);
        invalidateSelf();
    }


    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mBounds);
        switch (mShadowGravity) {
            case Gravity.LEFT:
                canvas.translate(mBounds.left,0);
                canvas.drawRect(0, mBounds.top, mShadowRadius, mBounds.bottom, mPaint);
                break;
            case Gravity.TOP:
                canvas.translate(0,mBounds.top);
                canvas.drawRect(mBounds.left, 0, mBounds.right, mShadowRadius, mPaint);
                break;
            case Gravity.RIGHT:
                canvas.translate(mBounds.right- mShadowRadius,0);
                canvas.drawRect(0, mBounds.top,mShadowRadius, mBounds.bottom, mPaint);
                break;
            case Gravity.BOTTOM:
                canvas.translate(0,mBounds.bottom -mShadowRadius);
                canvas.drawRect(mBounds.left, 0, mBounds.right, mShadowRadius, mPaint);
                break;
        }
//        canvas.drawRect(0,0,400,400,mPaint);
        canvas.restore();
    }

    public ShadowDrawable setColor(int color) {
        mPaint.setColor(color);
        return this;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public int getNumStops() {
        return mNumStops;
    }

    public void setNumStops(int numStops) {
        this.mNumStops = numStops;
    }

    public int getShadowOffset() {
        return mShadowOffset;
    }

    public void setShadowOffset(int shadowOffset) {
        this.mShadowOffset = shadowOffset;
    }


    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    public float getShadowRadius() {
        return mShadowRadius;
    }

    public void setShadowRadius(float shadowRadius) {
        this.mShadowRadius = shadowRadius;
    }


    public int getShadowDirection() {
        return mShadowGravity;
    }

    public void setmShadowGravity(int gravity) {
        setGravity(gravity);
    }

    public static float dip2px(float dipValue, float scale) {
        return (dipValue * scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param context
     * @return
     */
    public static float dip2px(float dipValue, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dip2px(dipValue, scale);
    }
}