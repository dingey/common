package com.github.dingey.common;

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

    public static <T> Pager<T> of(List<T> list, long total) {
        Pager<T> pager = new Pager<>();
        pager.setList(list);
        pager.setTotal(total);
        return pager;
    }
}
