package com.crestron.aurora;

import org.junit.Test;

public class ExampleJavaTest {

    @Test
    public void test1() {

        TestStuff testStuff = new TestStuff() {};

        System.out.println("Test stuff test is " + testStuff.Test());

        TestStuff testStuff1 = new TestStuff() {
            @Override
            public boolean Test() {
                return true;
            }
        };

        System.out.println("Test stuff test is " + testStuff1.Test());

    }

    interface TestStuff {
        default boolean Test() {
            return false;
        }
    }

    @Test
    public void test2() {
        boolean check = true;
        do {
            int i = 0;
            while (true) {
                //get input
                if (i == 4) {
                    break;
                } else if (i == 5) {
                    check = false;
                } else {
                    i++;
                }
            }
        } while (check);
    }
}
