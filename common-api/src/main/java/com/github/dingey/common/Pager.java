package com.github.dingey.common;

import com.github.dingey.common.exception.CommonException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public class Pager<T> {
    private List<T> list;
    private long total;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public static <P, T> Pager<T> of(P pager) {
        try {
            Pager<T> p = new Pager<>();
            if (pager == null) {
                p.setList(Collections.emptyList());
                return p;
            }
            PropertyDescriptor listProperty = new PropertyDescriptor("list", pager.getClass());
            @SuppressWarnings("unchecked")
            List<T> invoke = (List<T>) listProperty.getReadMethod().invoke(pager, (Object[]) null);
            p.setList(invoke);
            PropertyDescriptor totalProperty = new PropertyDescriptor("total", pager.getClass());
            Long total = (Long) totalProperty.getReadMethod().invoke(pager, (Object[]) null);
            p.setTotal(total);
            return p;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new CommonException(e);
        }
    }
}
