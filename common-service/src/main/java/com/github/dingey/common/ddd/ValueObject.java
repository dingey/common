package com.github.dingey.common.ddd;

import java.io.Serializable;
import java.util.Objects;

/**
 * 值对象
 *
 * @param <T> 值类型
 */
public abstract class ValueObject<T extends Serializable> {

    T value;

    public ValueObject(T value) {
        this.value = value;
    }

    public void setId(T value) {
        this.value = value;
    }

    public T getId() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueObject<?> that = (ValueObject<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
