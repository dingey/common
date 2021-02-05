package com.github.dingey.common.context;

import com.github.dingey.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局上下文，会在远程调用中传递上下文信息
 */
public class GlobalContext {
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
        map.putIfAbsent(key, value);
    }

    public static String getValue(String key) {
        Map<String, String> map = getContextMap();
        Objects.requireNonNull(map, "全局上下文变量未设置，请先设置在使用");
        return map.get(key);
    }

    public static void clear() {
        contextMap.remove();
    }

    static final String HEADER_NAME = "g-c";

    public static boolean hasContext() {
        return contextMap.get() != null && !contextMap.get().isEmpty();
    }

    static String toJsonString() {
        return JsonUtil.toJson(getContextMap());
    }

    static void setByJsonString(String jsonString) {
        HashMap map = JsonUtil.parseJson(jsonString, HashMap.class);
        setContextMap(map);
    }
}
