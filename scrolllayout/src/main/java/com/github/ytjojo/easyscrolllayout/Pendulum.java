package com.github.ytjojo.easyscrolllayout;

/**
 * Created by Administrator on 2018/1/5 0005.
 *
 * http://gad.qq.com/article/detail/23616%20target=
 */

public class Pendulum {
    public float GRAVITY = 250f;            //重力加速度
    //角速度
    private float mAngularVelocity = 0;
    //半径
    private final static float RADIUS = 20;
    public static final int DELTA_TIME = 16;
    //最大x偏移量
    private float mMaxXOffset = 10;
    private float mAngle;
    private float mMaxAngle = (float) (Math.PI/2-(float) Math.acos(mMaxXOffset/ RADIUS));
    long cur = System.currentTimeMillis();
    float mOffsetX;
    float mOffsetY;
    public void physicsUpdate(){
        //mXOffset为球距离中心点水平距离（p.x-c.x)
        float cosine;
        if(mAngle < mMaxAngle){
            cosine = (float) Math.sin(mMaxAngle - mAngle);
        }else {
            cosine = -(float) Math.sin(mAngle - mMaxAngle);
        }
        //求角加速度
        float acceleration = (cosine * GRAVITY)/ RADIUS;
        //累计角速度
        mAngularVelocity += acceleration * DELTA_TIME /1000;

        //求角位移(乘以180/PI 是为了将弧度转换为角度)
        mAngle += (float) (mAngularVelocity * DELTA_TIME)/1000;
//        System.out.println(Math.toDegrees(mAngle) );
        if(mAngle <0){
            System.out.println( "cur  " + (System.currentTimeMillis()-cur));
        }
        mOffsetX = cosine* RADIUS;
        mOffsetY = (float) (( Math.sqrt(1 - cosine*cosine)* RADIUS - RADIUS));
//        System.out.println(mOffsetX  + " " + mOffsetY);



    }

    /**
     * w^2 = (2 * g( 1- cosθ)/L
     * @return
     */
    public float max(){
       return (float) (Math.sqrt(2* GRAVITY *  (1 - mMaxXOffset/RADIUS))/ RADIUS);
    }
    public float time(){
        return (float) (2* Math.PI*Math.sqrt(RADIUS /GRAVITY));
    }


    public float getOffsetX(){
        return mOffsetX;
    }
    public float getOffsetY(){
        return mOffsetY;
    }

}
