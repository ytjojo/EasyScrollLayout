package com.github.ytjojo.easyscrolllayout;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2018/1/5 0005.
 * <p>
 * http://gad.qq.com/article/detail/23616%20target=
 */

public class Pendulum {
    private static final int DOTS_COUNT = 7;
    private static final int OUTER_DOTS_POSITION_ANGLE = 51;
    public float GRAVITY = 250f;            //重力加速度
    //角速度
    private float mAngularVelocity = 0;
    //半径
    private final static float RADIUS = 20;
    public static final int DELTA_TIME = 16;
    //最大x偏移量
    private float mMaxXOffset = 10;
    private float mAngle;
    private float mMaxAngle = (float) (Math.PI / 2 - (float) Math.acos(mMaxXOffset / RADIUS));
    long cur = System.currentTimeMillis();
    float mOffsetX;
    float mOffsetY;

    public void physicsUpdate() {
        //mXOffset为球距离中心点水平距离（p.x-c.x)
        float cosine;
        if (mAngle < mMaxAngle) {
            cosine = (float) Math.sin(mMaxAngle - mAngle);
        } else {
            cosine = -(float) Math.sin(mAngle - mMaxAngle);
        }
        //求角加速度
        float acceleration = (cosine * GRAVITY) / RADIUS;
        //累计角速度
        mAngularVelocity += acceleration * DELTA_TIME / 1000;

        //求角位移(乘以180/PI 是为了将弧度转换为角度)
        mAngle += (float) (mAngularVelocity * DELTA_TIME) / 1000;
//        System.out.println(Math.toDegrees(mAngle) );
        if (mAngle < 0) {
            System.out.println("cur  " + (System.currentTimeMillis() - cur));
        }
        mOffsetX = cosine * RADIUS;
        mOffsetY = (float) ((Math.sqrt(1 - cosine * cosine) * RADIUS - RADIUS));
//        System.out.println(mOffsetX  + " " + mOffsetY);


    }

    /**
     * w^2 = (2 * g( 1- cosθ)/L
     *
     * @return
     */
    public float max() {
        return (float) (Math.sqrt(2 * GRAVITY * (1 - mMaxXOffset / RADIUS)) / RADIUS);
    }

    public float time() {
        return (float) (2 * Math.PI * Math.sqrt(RADIUS / GRAVITY));
    }


    public float getOffsetX() {
        return mOffsetX;
    }

    public float getOffsetY() {
        return mOffsetY;
    }


    int mMaxFrameCount = 80;
    DotCollection mDotCollection = new DotCollection();

    int mProgressStep;

    public void initDotCollections() {
    }

    public static class DotCollection {
        public int alpha;
        public int color;
        public float positionX;
        public float positionY;
        public float curRadius ;
        public float dotSize ;
        public float progress = -1f;
    }

    int mMaxDotsRadius = 60;
    int mMaxDotSize = 8;

    public void updateDotCollections() {
        if(mProgressStep % mMaxFrameCount ==0){
            mDotCollection.progress = 0f;
            mDotCollection.positionX = getOffsetX() * 25;
            mDotCollection.positionY = getOffsetY() * 25;
            mDotCollection.curRadius = 0f;
            mDotCollection.dotSize = 0f;
        }else {
            mDotCollection.progress += 1f / (mMaxFrameCount - 1f);
            mDotCollection.positionX = getOffsetX() * 25;
            mDotCollection.positionY = getOffsetY() * 25;
        }

        float progress = (float) Utils.clamp(mDotCollection.progress, 0.6f, 1f);
        int alpha = (int) Utils.mapValueFromRangeToRange(progress, 0.6f, 1f, 255, 0);
        mDotCollection.alpha = alpha;
        mDotCollection.color = 0xFFFF9800;
        if(mDotCollection.progress<0.75f){

            mDotCollection.curRadius = 30+  mDotCollection.progress* mMaxDotsRadius;
        }else {
            mDotCollection.curRadius = (float) Utils.mapValueFromRangeToRange(mDotCollection.progress, 0.75f, 1f, 0, mMaxDotsRadius);
        }
        mDotCollection.dotSize = mMaxDotSize - mMaxDotSize *mDotCollection.progress ;

        mProgressStep++;
        if (mProgressStep == 2 * mMaxFrameCount) {
            mProgressStep = mMaxFrameCount;
        }
    }

    public void drawInnerDotsFrame(Canvas canvas, Paint paint) {
        DotCollection dotCollection = mDotCollection;
        if (dotCollection.progress >= 0) {
            paint.setColor(dotCollection.color);
            paint.setAlpha(dotCollection.alpha);
            for (int j = 0; j < DOTS_COUNT; j++) {
                int cX = (int) (dotCollection.curRadius * Math.cos((j * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
                int cY = (int) (dotCollection.curRadius * Math.sin((j * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180));
                Logger.e("cx" + cX + " cy" + cY);
                canvas.drawCircle(cX + dotCollection.positionX, cY + dotCollection.positionY, dotCollection.dotSize, paint);
            }
        }
    }

}
