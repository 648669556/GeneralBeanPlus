package com.zhiyi.generalbeanplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjunhong
 * 批量更新时候使用的对象
 */
public class PropertyList {
    private String name;

    private List<PropertyData> dataList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PropertyData> getDataList() {
        return dataList;
    }

    public void setDataList(List<PropertyData> dataList) {
        this.dataList = dataList;
    }

    public PropertyList() {
    }

    public PropertyList(String name) {
        this.name = name;
        dataList = new ArrayList<>();
    }
}
