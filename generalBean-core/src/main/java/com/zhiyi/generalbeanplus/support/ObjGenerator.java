package com.zhiyi.generalbeanplus.support;

import com.zhiyi.generalbeanplus.exception.GeneralBeanException;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author chenjunhong
 * 对象生成器，将数据库中查询出来的数据转换为实体对象
 */
public class ObjGenerator<T> {

    private final Class<T> entityClazz;

    @SuppressWarnings("unchecked")
    public T generatorObj(Map<String, Object> map) {
        T targetObj = null;
        try {
            targetObj = (T) entityClazz.newInstance();
        } catch (InstantiationException instantiationException) {
            throw new GeneralBeanException(String.format("%s 对象生成失败", entityClazz.getName()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Set<String> keySet = map.keySet();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(entityClazz);
        for (String aKeySet : keySet) {
            Object value = map.get(aKeySet);
            // 值是空,则不映射到对象
            if (!isBasicType(value)) {
                continue;
            }
            String originalName = tableInfo.getOriginalName(aKeySet);
            Field field = null;
            try {
                if (!originalName.equals(aKeySet)) {
                    field = entityClazz.getDeclaredField(originalName);
                } else {
                    field = entityClazz.getDeclaredField(StringUtils.underlineToCamel(aKeySet));
                }
            } catch (NoSuchFieldException e) {
                //如果没有找到元素则跳过
                continue;
            }

            String name = field.getName();
            String setMethodName = StringUtils.propertyToSetMethodName(name);
            Method method = null;
            try {
                method = entityClazz.getDeclaredMethod(setMethodName, field.getType());
            } catch (NoSuchMethodException e) {
                throw new GeneralBeanException(String.format("方法[%s()]未找到 所属类%s", setMethodName, entityClazz.getName()));
            }
            try {
                if (field.getType().isEnum()) {
                    // 枚举类型
                    Object objects[] = field.getType().getEnumConstants();
                    for (Object o : objects) {
                        if (value.toString().equalsIgnoreCase(o.toString())) {
                            method.invoke(targetObj, o);
                        }
                    }

                }
                if (field.getType().toString().contains("Boolean")) {
                    if ((int) value == 0)
                        method.invoke(targetObj, Boolean.FALSE);
                    else
                        method.invoke(targetObj, Boolean.TRUE);
                } else if (field.getType().toString().contains("Integer")) {
                    method.invoke(targetObj, Integer.valueOf(String.valueOf(value)));
                } else if (field.getType().toString().contains("Long")) {
                    method.invoke(targetObj, Long.valueOf(String.valueOf(value)));
                } else {
                    if (field.getType().toString().contains("String") && value instanceof Timestamp) {
                        String format = sdf.format(value);
                        method.invoke(targetObj, format);
                    } else {
                        method.invoke(targetObj, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new GeneralBeanException("对象映射失败", e);
            } catch (InvocationTargetException targetException) {
                targetException.printStackTrace();
            }
        }
        return targetObj;
    }

    public List<T> generatorObj(List<Map<String, Object>> maps) {
        List<T> list = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            list.add(this.generatorObj(map));
        }
        return list;
    }

    private boolean isBasicType(Object o) {
        return o instanceof String || o instanceof Integer || o instanceof Long || o instanceof Date || o instanceof Double || o instanceof Enum || o instanceof Boolean || o instanceof BigDecimal;
    }

    public ObjGenerator(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }
}