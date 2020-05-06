package com.github.dingey.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dingey.common.exception.JsonException;

import java.io.IOException;

@SuppressWarnings("unused")
public class JsonUtil {

    private static ObjectMapper objectMapper;

    JsonUtil(ObjectMapper objectMapper) {
        JsonUtil.objectMapper = objectMapper;
    }

    /**
     * 转换成json字符串
     *
     * @param value 对象
     * @param <T>   泛型类型
     * @return json字符串
     * @throws JsonException json异常
     */
    public static <T> String toJson(T value) throws JsonException {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 转换成java对象
     *
     * @param json      json字符串
     * @param valueType json字符对应的简单类
     * @param <T>       泛型类型
     * @return 实例对象
     * @throws JsonException json异常
     */
    public static <T> T parseJson(String json, Class<T> valueType) throws JsonException {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 转换成java包装类对象
     * <pre>
     * List<Person> pp3 = mapper.parseJson(json, new TypeReference<List<Person>>() {});
     * </pre>
     *
     * @param json         json字符串
     * @param valueTypeRef json字符对应的包装类
     * @param <T>          泛型类型
     * @return 实例对象
     * @throws JsonException json异常
     */
    public static <T> T parseJson(String json, TypeReference<T> valueTypeRef) throws JsonException {
        try {
            return objectMapper.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static void main(String[] args) {

    }
}
