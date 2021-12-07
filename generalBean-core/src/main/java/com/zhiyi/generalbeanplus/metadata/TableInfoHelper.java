package com.zhiyi.generalbeanplus.metadata;

import com.zhiyi.generalbeanplus.annotation.TargetTableName;
import com.zhiyi.generalbeanplus.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableInfoHelper {
    /**
     * 使用 concurrentHashMap 避免多线程问题
     */
    public static final Map<Class<?>, TableInfo> tableInfoMap = new ConcurrentHashMap<>();

    public static TableInfo getTableInfoByClazz(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        TableInfo tableInfo = tableInfoMap.get(clazz);
        //如果缓存中可以找到表信息则立马返回
        if (tableInfo != null) {
            return tableInfo;
        } else {
            String tableName = getTableName(clazz);
            tableInfo = new TableInfo(tableName, clazz);
            tableInfoMap.put(clazz, tableInfo);
        }
        return tableInfo;
    }

    public static TableInfo getTableInfo(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> clazz = object.getClass();
        return getTableInfoByClazz(clazz);
    }

    public static String getTableName(Class<?> clazz) {
        final TargetTableName targetTableName = AnnotationUtils.findAnnotation(clazz, TargetTableName.class);
        if (targetTableName != null) {
            if (!StringUtils.isEmpty(targetTableName.value())) {
                return targetTableName.value();
            }
        }
        String className = clazz.getName();
        String tableName = className.substring(className.lastIndexOf(".") + 1);
        char[] chs = tableName.toCharArray();
        if (chs.length > 0) {
            chs[0] = Character.toLowerCase(chs[0]);
        }
        tableName = new String(chs);
        return StringUtils.camelToUnderline(tableName);
    }
}
