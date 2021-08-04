package com.zhiyi.generalbeanplus.model;

/**
 * @author chenjunhong
 * 用以批量更新的 id 对应的值
 */
public class PropertyData {
    private Object id;
    private Object value;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
