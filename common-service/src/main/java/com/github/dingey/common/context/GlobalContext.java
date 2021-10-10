package com.github.dingey.common.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dingey.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局上下文，会在远程调用中传递上下文信息
 */
@SuppressWarnings("unused")
public class GlobalContext {
    public static final String HEADER_NAME = "g-c";
    private static final ThreadLocal<Map<String, String>> contextMap = new ThreadLocal<>();

    public static Map<String, String> getContextMap() {
        return contextMap.get();
    }

    public static void setContextMap(Map<String, String> map) {
        contextMap.set(map);
    }

    public static void addKeyValue(String key, String value) {
        Map<String, String> map = contextMap.get();
        if (map == null) {
            map = new HashMap<>();
            setContextMap(map);
        }
        map.put(key, value);
    }

    public static String getValue(String key) {
        Map<String, String> map = getContextMap();
        return map == null ? null : map.get(key);
    }

    public static String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : value;
    }

    public static Long getLong(String key) {
        String value = getValue(key);
        return (value == null || value.isEmpty()) ? null : Long.parseLong(value);
    }

    public static Long getLong(String key, long defaultValue) {
        String value = getValue(key);
        return (value == null || value.isEmpty()) ? defaultValue : Long.parseLong(value);
    }

    public static Integer getInteger(String key) {
        String value = getValue(key);
        return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
    }

    public static Integer getInteger(String key, int defaultValue) {
        String value = getValue(key);
        return (value == null || value.isEmpty()) ? defaultValue : Integer.parseInt(value);
    }

    public static void clear() {
        contextMap.remove();
    }


    public static boolean hasContext() {
        return contextMap.get() != null && !contextMap.get().isEmpty();
    }

    static String toJsonString() {
        return JsonUtil.toJson(getContextMap());
    }

    static void setByJsonString(String jsonString) {
        HashMap<String, String> map = JsonUtil.parseJson(jsonString, new TypeReference<HashMap<String, String>>() {
        });
        setContextMap(map);
    }
}
