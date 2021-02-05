package com.github.dingey.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"unchecked", "unused"})
public abstract class BaseFallback {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final HashMap<Class<?>, PropertyDescriptor> genericProp = new HashMap<>();

    public <T> T fallback(Object... args) {
        error(args);
        return (T) empty();
    }

    public StackTraceElement getTrace() {
        return getTrace(5);
    }

    public StackTraceElement getErrorTrace() {
        return getTrace(6);
    }

    public StackTraceElement getTrace(int index) {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        return traces[index];
    }

    public void error(Object... args) {
        error(null, args);
    }

    public void error(Throwable cause, Object... args) {
        StackTraceElement trace = getErrorTrace();
        String methodName = trace.getMethodName();
        Class<Object> existingClass = ClassUtils.getExistingClass(this.getClass().getClassLoader(), trace.getClassName());
        if (log.isDebugEnabled()) {
            log.error("服务被熔断，类名：{} 方法：{} 参数：{} 原因：{}", getName(existingClass), methodName, args, cause == null ? "" : cause.getMessage());
        }
    }

    private String getName(Class<?> clazz) {
        if (StringUtils.isEmpty(clazz.getSimpleName())) {
            return clazz.getName();
        } else {
            return clazz.getSimpleName();
        }
    }

    public Object empty() {
        StackTraceElement trace = getTrace();
        String methodName = trace.getMethodName();
        Class<Object> existingClass = ClassUtils.getExistingClass(this.getClass().getClassLoader(), trace.getClassName());
        Object o = null;
        for (Method m : existingClass.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                ParameterizedType parameterizedType = (ParameterizedType) m.getGenericReturnType();
                if (parameterizedType.getActualTypeArguments() == null || parameterizedType.getActualTypeArguments().length < 1) {
                    return ClassUtils.newInstance(m.getReturnType());
                }
                Type type = parameterizedType.getActualTypeArguments()[0];
                if (type instanceof ParameterizedType) {
                    ParameterizedType subType = (ParameterizedType) type;
                    if (subType.getRawType() == List.class) {
                        o = Collections.emptyList();
                    } else {
                        o = ClassUtils.newInstance(m.getReturnType());
                        Object pa = ClassUtils.newInstance((Class<?>) subType.getRawType());
                        setGenericProp(o, o.getClass(), pa);
                    }
                } else if (type instanceof List) {
                    o = Collections.emptyList();
                } else {
                    o = ClassUtils.newInstance((Class<?>) type);
                }
                break;
            }
        }
        return o;
    }

    private void setGenericProp(Object o, Class<?> valueClass, Object value) {
        if (genericProp.containsKey(o.getClass())) {
            PropertyDescriptor propertyDescriptor = genericProp.get(o.getClass());
            if (propertyDescriptor != null) {
                setValue(propertyDescriptor, o, value);
            }
        } else {
            PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(o.getClass());
            for (PropertyDescriptor pd : propertyDescriptors) {
                if (pd.getPropertyType() == valueClass) {
                    genericProp.put(o.getClass(), pd);
                    setValue(pd, o, value);
                    break;
                }
            }
        }
    }

    private void setValue(PropertyDescriptor propertyDescriptor, Object o, Object value) {
        try {
            propertyDescriptor.getWriteMethod().invoke(o, value);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }
}