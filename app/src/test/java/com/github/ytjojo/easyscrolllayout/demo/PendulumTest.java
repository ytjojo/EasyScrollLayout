package com.github.ytjojo.easyscrolllayout.demo;

import com.github.ytjojo.easyscrolllayout.Pendulum;

import org.junit.Test;

/**
 * Created by Administrator on 2018/1/6 0006.
 */

public class PendulumTest {

    @Test
    public void testMax(){

        float value= pendulum.max();
        System.out.print(value+"            ");

    }
    Pendulum pendulum =new Pendulum();
    long maxTime = 30000;
    long start;
    @Test
    public void updateTest() throws InterruptedException {
        start = System.currentTimeMillis();
        System.out.println(pendulum.time()+"            ");
        while ( System.currentTimeMillis() - start < maxTime){
            pendulum.physicsUpdate();
            Thread.sleep(pendulum.DELTA_TIME);
        }
    }
}
