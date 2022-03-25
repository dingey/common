package com.github.dingey.common.util;

import com.github.dingey.mybatis.mapper.exception.MapperException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@SuppressWarnings("unused")
public class MapUtil {

    private static ConversionService conversionService;
    private static boolean spring = false;

    @PostConstruct
    public void init() {
        spring = true;
    }

    /**
     * 转换 {@code List<Map>}格式为 {@code List<T>},spring环境自动转换类型，非spring环境忽略值拷贝
     *
     * @param list   入参
     * @param tClass 类
     * @param <T>    泛型
     * @return list
     */
    public static <T> List<T> toObjects(List<Map<String, Object>> list, Class<T> tClass) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : list) {
            T t = BeanUtils.instantiateClass(tClass);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String prop = lineToHump(entry.getKey());
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(tClass, prop);
                if (pd == null) {
                    continue;
                }
                Object o = entry.getValue();
                if (o == null) {
                    continue;
                }
                if (o.getClass() != pd.getPropertyType() && !spring) {
                    continue;
                } else if (o.getClass() != pd.getPropertyType() && spring) {
                    o = conversionService.convert(o, pd.getPropertyType());
                }

                try {
                    pd.getWriteMethod().invoke(t, o);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new MapperException(e.getMessage(), e);
                }
            }
            res.add(t);
        }
        return res;
    }

    @Autowired
    public void setConversionService(ConversionService conversionService) {
        MapUtil.conversionService = conversionService;
    }

    private static final Pattern linePattern = Pattern.compile("_(\\w)");

    private static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
