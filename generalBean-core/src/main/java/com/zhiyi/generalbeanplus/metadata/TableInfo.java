package com.zhiyi.generalbeanplus.metadata;

import com.zhiyi.generalbeanplus.annotation.TargetColumnName;
import com.zhiyi.generalbeanplus.annotation.TargetDBOut;
import com.zhiyi.generalbeanplus.annotation.TargetKeyColumn;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@SuppressWarnings("serial")
public class TableInfo {
    /**
     * 实体的clazz
     */
    private Class<?> entityType;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 要被跳过的属性名称
     */
    private Set<String> passFieldsName = new HashSet<>();
    /**
     * 目标列的名称
     */
    private Map<String, String> targetColumnNameMap;
    /**
     * 根据别名查询原来的参数名称
     */
    private Map<String, String> originalColumnNameMap;
    /**
     * 主键id 默认为 id
     */
    private String keyColumnName = "id";

    public TableInfo(String tableName, Class<?> entityType) {
        this.entityType = entityType;
        this.tableName = tableName;
        Field[] fields = entityType.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (field.isAnnotationPresent(TargetDBOut.class)) {
                //如果被跳过了则不接着处理
                passFieldsName.add(fieldName);
                continue;
            }
            if (field.isAnnotationPresent(TargetKeyColumn.class)) {
                //如果是主键则设置主键
                keyColumnName = fieldName;
            }
            if (field.isAnnotationPresent(TargetColumnName.class)) {
                //如果有别名
                TargetColumnName targetColumnName = field.getAnnotation(TargetColumnName.class);
                String alienName = targetColumnName.value();
                getTargetColumnNameMap().put(fieldName, alienName);
                getOriginalColumnNameMap().put(alienName, fieldName);
            }
        }
    }

    private Map<String, String> getOriginalColumnNameMap() {
        if (originalColumnNameMap == null) {
            originalColumnNameMap = new HashMap<>();
        }
        return originalColumnNameMap;
    }

    private Map<String, String> getTargetColumnNameMap() {
        if (targetColumnNameMap == null) {
            targetColumnNameMap = new HashMap<>();
        }
        return targetColumnNameMap;
    }

    public boolean isNullEntity() {
        return entityType == null || tableName == null || keyColumnName == null;
    }

    public String getAlias(String name) {
        return getTargetColumnNameMap().getOrDefault(name, name);
    }

    public String getOriginalName(String name) {
        return getOriginalColumnNameMap().getOrDefault(name, name);
    }
}
