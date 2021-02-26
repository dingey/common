package com.github.dingey.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ding
 * @since 2021/2/26
 */
@SuppressWarnings("unused")
public class BeanUtil {
    private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
    private static final Map<Class<?>, SerializedLambda> lambdaClassCache = new ConcurrentHashMap<>();

    /***
     * 获取get方法引用的方法名
     * @param fn 函数
     * @param <T> 对象类
     * @return 属性名
     */
    public static <T> String convertToFieldName(IGetter<T> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        String prefix = "";
        if (methodName.startsWith("get")) {
            prefix = "get";
        } else if (methodName.startsWith("is")) {
            prefix = "is";
        }
        return firstLower(methodName.replace(prefix, ""));
    }

    /**
     * 获取set方法引用的方法名
     *
     * @param fn  函数
     * @param <T> 对象类
     * @param <U> 方法参数值
     * @return 属性名
     */
    public static <T, U> String convertToFieldName(ISetter<T, U> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        if (!methodName.startsWith("set")) {
            log.warn("无效的setter方法：" + methodName);
        }
        return firstLower(methodName.replace("set", ""));
    }

    private static String firstLower(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    /**
     * 关键在于这个方法
     */
    public static SerializedLambda getSerializedLambda(Serializable fn) {
        SerializedLambda lambda = lambdaClassCache.get(fn.getClass());
        if (lambda == null) {
            try {
                Method method = fn.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                lambda = (SerializedLambda) method.invoke(fn);
                lambdaClassCache.put(fn.getClass(), lambda);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
        return lambda;
    }

    /**
     * Serializable的函数式接口的实例都可以获取一个属于它的SerializedLambda实例，并且通过它获取到方法的名称
     */
    @FunctionalInterface
    public interface IGetter<T> extends Serializable {
        Object get(T source);
    }

    @FunctionalInterface
    public interface ISetter<T, U> extends Serializable {
        void set(T t, U u);
    }
}
