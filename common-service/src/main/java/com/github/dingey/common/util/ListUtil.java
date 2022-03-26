package com.github.dingey.common.util;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ListUtil {
    private ListUtil() {
    }

    /**
     * 拆分成固定大小的列表
     *
     * @param list  参数
     * @param limit 单个列表大小
     * @param <T>   泛型
     * @return 多个列表
     */
    public static <T> List<List<T>> splitFixed(List<T> list, int limit) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        int i = 0;
        List<List<T>> temps = new ArrayList<>();
        while (i * limit < list.size()) {
            if ((i + 1) * limit > list.size()) {
                temps.add(list.subList(i * limit, list.size()));
            } else {
                temps.add(list.subList(i * limit, (i + 1) * limit));
            }
            i++;
        }
        return temps;
    }

    /**
     * 拆分成num个子列表
     *
     * @param list 参数
     * @param num  几个列表
     * @param <T>  泛型
     * @return 多个列表
     */
    public static <T> List<List<T>> splitSeveral(List<T> list, int num) {
        List<List<T>> temps = new ArrayList<>();
        int length = list.size() / num + (list.size() % num == 0 ? 0 : 1);
        if (list.size() > 0) {
            for (int i = 0; i < num; i++) {
                List<T> ts;
                if ((i + 1) * length < list.size()) {
                    ts = list.subList(i * length, (i + 1) * length);
                } else {
                    ts = list.subList(i * length, list.size());
                }
                if (!ts.isEmpty())
                    temps.add(ts);
            }
        }
        return temps;
    }

    /**
     * 求交集
     *
     * @param ts   目标列表
     * @param ss   比较列表
     * @param tFun 目标方法
     * @param sFun 比较方法
     * @param <T>  目标类型
     * @param <S>  比较类型
     * @return 交集
     */
    public static <T, S> List<T> intersection(List<T> ts, List<S> ss, Function<T, ?> tFun, Function<S, ?> sFun) {
        if (ts == null || ts.isEmpty()) {
            return Collections.emptyList();
        }
        if (ss == null || ss.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> res = new ArrayList<>();
        for (T t : ts) {
            boolean contains = false;
            Object tv;
            if (tFun != null) {
                tv = tFun.apply(t);
            } else {
                tv = t;
            }
            for (S s : ss) {
                Object sv;
                if (sFun != null) {
                    sv = sFun.apply(s);
                } else {
                    sv = s;
                }
                if (Objects.equals(tv, sv)) {
                    res.add(t);
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 求差集
     *
     * @param ts   目标列表
     * @param ss   比较列表
     * @param tFun 目标方法
     * @param sFun 比较方法
     * @param <T>  目标类型
     * @param <S>  比较类型
     * @return 交集
     */
    public static <T, S> List<T> difference(List<T> ts, List<S> ss, Function<T, ?> tFun, Function<S, ?> sFun) {
        if (ts == null || ts.isEmpty()) {
            return Collections.emptyList();
        }
        if (ss == null || ss.isEmpty()) {
            return ts;
        }
        List<T> res = new ArrayList<>();
        for (T t : ts) {
            boolean contains = false;
            Object tv;
            if (tFun != null) {
                tv = tFun.apply(t);
            } else {
                tv = t;
            }
            for (S s : ss) {
                Object sv;
                if (sFun != null) {
                    sv = sFun.apply(s);
                } else {
                    sv = s;
                }
                if (Objects.equals(tv, sv)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                res.add(t);
            }
        }
        return res;
    }

//    public static void main(String[] args) {
//        List<Long> s1 = Arrays.asList(1L, 2L, 3L);
//        List<String> s2 = Arrays.asList("1", "2");
//        List<Long> list = intersection(s1, s2, Object::toString, t -> t);
//        List<Long> difference = difference(s1, s2, Object::toString, t -> t);
//        System.out.println();
//    }
}
