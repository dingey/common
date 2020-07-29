package com.github.dingey.common.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author d
 */
@SuppressWarnings("unused")
public class HttpUtil {
    public static String get(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * 发送json POST请求
     *
     * @param url  地址
     * @param json 参数
     * @return 响应
     */
    public static String postJson(String url, String json) {
        return postJson(url, json, null);
    }

    /**
     * 发送json POST请求
     *
     * @param url        地址
     * @param json       参数
     * @param headersMap 请求头
     * @return 响应
     */
    public static String postJson(String url, String json, MultiValueMap<String, String> headersMap) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        setHeaders(headersMap, headers);
        HttpEntity<String> formEntity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(url, formEntity, String.class).getBody();
    }

    /**
     * 发送application/x-www-form-urlencoded POST请求
     *
     * @param url    地址
     * @param params 参数
     * @return 响应
     */
    public static String post(String url, MultiValueMap<String, String> params) {
        return post(url, params, null);
    }

    /**
     * 发送application/x-www-form-urlencoded POST请求
     *
     * @param url        地址
     * @param params     参数
     * @param headersMap 请求头
     * @return 响应
     */
    public static String post(String url, MultiValueMap<String, String> params, MultiValueMap<String, String> headersMap) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        setHeaders(headersMap, headers);
        HttpEntity<MultiValueMap<String, String>> requestBody = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(url, requestBody, String.class);
    }

    private static void setHeaders(MultiValueMap<String, String> headersMap, HttpHeaders headers) {
        if (headersMap != null) {
            headers.addAll(headersMap);
        }
    }

    /**
     * 发送multipart/form-data格式的post请求，使用InputStreamResource上传文件时，需要重写该类的两个方法，contentLength和getFilename
     * <pre>
     * {@code MultiValueMap<String, Object> resultMap = new LinkedMultiValueMap<>();}
     *
     * //文件形式
     * {@code Resource resource = new FileSystemResource(file);}
     * {@code param.put("file1", resource);}
     *
     * //流形式
     * {@code Resource resource = new InputStreamResource(inputStream);}
     * {@code param.put("file2", resource);}
     * </pre>
     *
     * @param url    地址
     * @param params 参数
     * @return 响应
     */
    public static String postForm(String url, MultiValueMap<String, Object> params) {
        return postForm(url, params, null);
    }

    /**
     * 发送multipart/form-data格式的post请求，使用InputStreamResource上传文件时，需要重写该类的两个方法，contentLength和getFilename
     * <pre>
     * {@code MultiValueMap<String, Object> resultMap = new LinkedMultiValueMap<>();}
     *
     * //文件形式
     * {@code Resource resource = new FileSystemResource(file);}
     * {@code param.put("file1", resource);}
     *
     * //流形式
     * {@code Resource resource = new InputStreamResource(inputStream);}
     * {@code param.put("file2", resource);}
     * </pre>
     *
     * @param url        地址
     * @param params     参数
     * @param headersMap 请求头
     * @return 响应
     */
    public static String postForm(String url, MultiValueMap<String, Object> params, MultiValueMap<String, String> headersMap) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        setHeaders(headersMap, headers);
        HttpEntity<MultiValueMap<String, Object>> requestBody = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(url, requestBody, String.class);
    }
}
