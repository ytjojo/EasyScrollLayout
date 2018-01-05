package com.github.ytjojo.easyscrolllayout.demo;

/**
 * Created by Administrator on 2018/1/5 0005.
 */

public class Pendulum {
    public float GRAVITY = 9.8f;            //重力加速度
    //角速度
    private float mAngularVelocity =0;
    //半径
    private int mRadus = 20;
    //x偏移量
    private int mXOffset;
    private int deltaTime=30;
    //最大x偏移量
    private int mMaxXOffset =15;
    private float mAngle;

    private void physicsUpdate(){
        //mXOffset为球距离中心点水平距离（p.x-c.x)

        float cosine = mXOffset/ mRadus;
        //求角加速度
        float acceleration = (cosine * GRAVITY)/ mRadus;
        //累计角速度
        mAngularVelocity += acceleration * deltaTime;
        //求角位移(乘以180/PI 是为了将弧度转换为角度)
        mAngle = (float) (mAngularVelocity * deltaTime*180.0f/Math.PI);
        mXOffset= (int) (Math.cos(mAngle /360*2*Math.PI)* mRadus);

        float max = (float) (Math.sqrt(2* GRAVITY *mRadus* Math.cos(mMaxXOffset/mRadus))/mRadus);
        if (mAngularVelocity >max) {
            mXOffset = -mXOffset;
        }

    }



}
