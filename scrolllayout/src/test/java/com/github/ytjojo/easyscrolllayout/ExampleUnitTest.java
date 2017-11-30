package com.github.ytjojo.easyscrolllayout;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        int a =0x1;
        int b = 0x2;
        int c= 0x4;
        int d= 0x8;

        System.out.println(" " + (b|a) );
        System.out.println(" " + (b&b) );
        System.out.println(" " + ((0|a)|a|b|c) );
        System.out.println(" " + (((0|a)|a|b|c)&c) );

    }
}