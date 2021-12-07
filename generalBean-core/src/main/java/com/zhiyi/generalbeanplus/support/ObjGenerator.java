package com.zhiyi.generalbeanplus.support;

import com.zhiyi.generalbeanplus.exception.GeneralBeanException;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.util.StringUtils;
import org.springframework.util.ReflectionUtils;

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

    public T generatorObj(Map<String, Object> map) {
        T targetObj = null;
        try {
            targetObj = entityClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException instantiationException) {
            throw new GeneralBeanException(String.format("%s 对象生成失败", entityClazz.getName()));
        }
        Set<String> keySet = map.keySet();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(entityClazz);
        for (String aKeySet : keySet) {
            Object value = map.get(aKeySet);
            // 值是空,则不映射到对象
            if (!isBasicType(value)) {
                continue;
            }
            String originalName = tableInfo.getOriginalName(aKeySet);
            Field field = null;
            if (!originalName.equals(aKeySet)) {
                field = ReflectionUtils.findField(entityClazz, originalName);
            } else {
                field = ReflectionUtils.findField(entityClazz, StringUtils.underlineToCamel(aKeySet));
            }
            if (field == null) continue;

            String name = field.getName();
            String setMethodName = StringUtils.propertyToSetMethodName(name);
            Method method = null;
            method = tableInfo.getMethod(setMethodName, field.getType());

            if (method == null) {
                throw new GeneralBeanException(String.format("方法[%s()]未找到 所属类%s", setMethodName, entityClazz.getName()));
            }

            try {
                if (field.getType().isEnum()) {
                    // 枚举类型
                    Object[] objects = field.getType().getEnumConstants();
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
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            value = 1;
                        } else {
                            value = 0;
                        }
                    }
                    method.invoke(targetObj, Integer.valueOf(String.valueOf(value)));
                } else if (field.getType().toString().contains("Long")) {
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            value = 1;
                        } else {
                            value = 0;
                        }
                    }
                    method.invoke(targetObj, Long.valueOf(String.valueOf(value)));
                } else {
                    if (field.getType().toString().contains("String") && value instanceof Timestamp) {
                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = sdf.format(value);
                        method.invoke(targetObj, format);
                    } else {
                        method.invoke(targetObj, value);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new GeneralBeanException("对象映射失败", e);
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
