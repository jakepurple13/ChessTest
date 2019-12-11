package com.crestron.aurora;

import com.crestron.aurora.showapi.ShowApi;
import com.crestron.aurora.showapi.ShowInfo;
import com.crestron.aurora.showapi.ShowSource;
import com.crestron.aurora.showapi.Source;

import org.junit.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Pair;

public class ExampleJavaTest {

    @Test
    public void test1() {

        TestStuff testStuff = new TestStuff() { };

        System.out.println("Test stuff test is " + testStuff.Test());

        TestStuff testStuff1 = new TestStuff() {

            @Override
            public boolean Test() {
                return true;
            }
        };

        System.out.println("Test stuff test is " + testStuff1.Test());
    }

    @Test
    public void test5() {
        List<String> f1 = getEnums(ShowSource.class, ShowSource::name);
        System.out.println(f1);
        List<List<ShowInfo>> f2 = getEnums(Source.class, v -> new ShowApi(v).getShowInfoList());
        System.out.println(f2.stream().map(v -> new Pair(ShowSource.Companion.getSourceType(v.get(0).getUrl()), v.size())).collect(Collectors.toList()));
    }

    @FunctionalInterface
    interface EnumTransformer<T, V> {
        V transform(T item);
    }

    private <T extends Enum<T>, V> List<V> getEnums(Class<T> clazz, EnumTransformer<T, V> transformer) {
        return EnumSet.allOf(clazz).stream().map(transformer::transform).collect(Collectors.toList());
    }

    //inline fun <reified T: Enum<T>> getAllEnumValues() = T::class.java.enumConstants!!.map { it }

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
