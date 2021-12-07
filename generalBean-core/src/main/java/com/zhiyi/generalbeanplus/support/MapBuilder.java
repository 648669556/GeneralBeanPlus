package com.zhiyi.generalbeanplus.support;

import com.zhiyi.generalbeanplus.exception.GeneralBeanException;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.model.Property;
import com.zhiyi.generalbeanplus.model.PropertyData;
import com.zhiyi.generalbeanplus.model.PropertyList;
import com.zhiyi.generalbeanplus.util.StringUtils;
import com.zhiyi.generalbeanplus.wrapper.AbstractWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * map 属性参数建造工厂
 */
public class MapBuilder {
    /**
     * 需要被跳过的属性名称
     * 与pojo 属性 名称一致
     */
    private static Set<String> needPass;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 属性列表
     */
    private List<Property> properties;
    /**
     * id
     */
    private Integer id;
    /**
     * id列表
     */
    private List<Object> idList;
    /**
     * 批量更新时的属性
     */
    private List<PropertyList> propertyList;

    private Map<String, PropertyList> propertyListMap;

    private List<List<Object>> propertiesValueBatch;

    private String idName;

    private Object idValue;

    private AbstractWrapper<?, ?, ?> wrapper;

    public MapBuilder setIdValue(Object value) {
        this.idValue = value;
        return this;
    }

    public MapBuilder setWrapper(AbstractWrapper<?, ?, ?> wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public MapBuilder setIdName(String idName) {
        this.idName = idName;
        return this;
    }

    public MapBuilder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public MapBuilder setProperties(List<Property> properties) {
        this.properties = properties;
        return this;
    }

    public MapBuilder setId(Integer id) {
        this.id = id;
        return this;
    }

    public MapBuilder setIdList(List<Object> idList) {
        this.idList = idList;
        return this;
    }

    public MapBuilder setPropertyList(List<PropertyList> propertyList) {
        this.propertyList = propertyList;
        return this;
    }

    public Map<String, Object> build() {
        Map<String, Object> para = new HashMap<>();
        if (tableName != null) {
            para.put("tableName", tableName);
        }
        if (properties != null) {
            para.put("properties", properties);
        }
        if (id != null) {
            para.put("id", id);
        }
        if (propertyListMap != null) {
            List<PropertyList> propertyList = getPropertyList();
            propertyList.addAll(propertyListMap.values());
        }
        if (propertyList != null) {
            para.put("propertyList", propertyList);
        }
        if (idList != null) {
            para.put("idList", idList);
        }
        if (idName != null) {
            para.put("idName", idName);
        }
        if (idValue != null) {
            para.put("idValue", idValue);
        }
        if (!CollectionUtils.isEmpty(propertiesValueBatch)) {
            para.put("propertiesValueBatch", propertiesValueBatch);
        }
        if (wrapper != null) {
            para.put("wrapper", wrapper);
        }
        return para;
    }

    public Field handleObject(Object object, boolean containNull) {
        //获取表信息
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        //表名称
        String keyColumnName = tableInfo.getKeyColumnName();
        //被跳过的属性名称
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        //设置属性列表
        List<Property> pros = new ArrayList<>();
        this.setProperties(pros);
        AtomicReference<Field> idField = new AtomicReference<>();

        ReflectionUtils.doWithFields(clazz, field -> {
            Property property = new Property();
            String name = field.getName();
            if (passFieldsName.contains(name)) {
                //跳过不需要的
                return;
            }
            if (name.equalsIgnoreCase("start") || name.equalsIgnoreCase("limit"))
                return;
            if (keyColumnName.equalsIgnoreCase(name)) {
                //如果为主键
                idField.set(field);
                return;
            }
            //获取属性的get方法
            String getMethodName = StringUtils.propertyToGetMethodName(name);
            Method getMethod;
            getMethod = tableInfo.getMethod(getMethodName);
            if (getMethod == null) {
                return;
            }
            Object value = null;
            try {
                value = getMethod.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                //get方法不是public
                throw new GeneralBeanException(getMethodName + " 方法无法访问");
            }
            // value为空或者不是基础数据
            if (!containNull && value == null || !isBasicType(value)) {
                return;
            }
            String alias = tableInfo.getAlias(name);
            if (alias != null) {
                property.setName(alias);
            } else {
                property.setName(StringUtils.camelToUnderline(name));
            }
            property.setValue(value);
            pros.add(property);
        });

        return idField.get();
    }

    public MapBuilder handleObject(Collection<?> objects, boolean containNull, FieldFilter<?> fieldFilter) {
        if (objects.isEmpty()) {
            return this;
        }
        Set<String> properties = new HashSet<>();
        Class<?> clazz = CollectionUtils.findCommonElementType(objects);
        if (clazz == null) throw new GeneralBeanException("数组对象内含有不同的对象class类型");
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        String keyColumnName = tableInfo.getKeyColumnName();
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        //设置表名
        this.tableName = tableInfo.getTableName();
        for (Object next : objects) {
            List<Object> objectList = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                String fieldName = field.getName();
                if (passFieldsName.contains(fieldName)) {
                    return;
                }
                if (needPass.contains(fieldName))
                    return;
                if (keyColumnName.equalsIgnoreCase(fieldName)) {
                    //如果为主键
                    return;
                }
                if (fieldFilter != null && fieldFilter.test(fieldName)) {
                    return;
                }
                String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
                Method getMethod;
                getMethod = tableInfo.getMethod(getMethodName);
                if (getMethod == null) {
                    return;
                }
                Object value = null;
                try {
                    value = getMethod.invoke(next);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    //get方法不是public
                    throw new GeneralBeanException(getMethodName + " 方法无法访问");
                }
                // value为空或者不是基础数据
                if (!containNull && value == null || value != null && !isBasicType(value)) {
                    return;
                }
                objectList.add(value);
                boolean added = properties.add(fieldName);
                if (added) {
                    Property property = new Property();
                    String alias = tableInfo.getAlias(fieldName);
                    if (alias != null) {
                        property.setName(alias);
                    } else {
                        property.setName(StringUtils.camelToUnderline(fieldName));
                    }
                    getProperties().add(property);
                }
            });
            getPropertiesValueBatch().add(objectList);
        }
        return this;
    }

    public void handleObjectForBatch(Object object, boolean containNull, FieldFilter<?> fieldFilter) {
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        //主键名称
        String keyColumnName = tableInfo.getKeyColumnName();
        //被跳过的属性
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        if (tableName == null) {
            tableName = tableInfo.getTableName();
        }
        //获取id
        Object id;
        Method getId;
        String getIdMethodName = StringUtils.propertyToGetMethodName(keyColumnName);
        String keyAlias = tableInfo.getAlias(keyColumnName);
        if (keyAlias != null) {
            idName = keyAlias;
        } else {
            idName = StringUtils.camelToUnderline(keyColumnName);
        }
        getId = tableInfo.getMethod(getIdMethodName);
        if (getId == null) {
            throw new GeneralBeanException("do类不存在方法" + getIdMethodName);
        }
        try {
            //获得类的id
            id = Optional.ofNullable(getId.invoke(object)).orElseThrow(() -> new GeneralBeanException("do类id不得为空"));
            getIdList().add(id);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GeneralBeanException("do类" + getIdMethodName + "方法不能访问");
        }
        Object finalId = id;
        ReflectionUtils.doWithFields(clazz, field -> {
            PropertyData propertyData = new PropertyData();
            String fieldName = field.getName();
            if (passFieldsName.contains(fieldName)) {
                return;
            }
            if (needPass.contains(fieldName))
                return;
            if (keyColumnName.equalsIgnoreCase(fieldName)) {
                //跳过id主键的更新
                return;
            }
            if (fieldFilter != null && fieldFilter.test(fieldName)) {
                //跳过过滤器中的哪些属性
                return;
            }
            String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
            Method getMethod;
            Object value = null;
            getMethod = tableInfo.getMethod(getMethodName);
            if (getMethod == null) {
                return;
            }
            try {
                //获取值
                value = getMethod.invoke(object);
                if (!containNull && value == null || value != null && !isBasicType(value)) {
                    return;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new GeneralBeanException("do类" + getMethodName + "方法不能访问");
            }
            String alias = tableInfo.getAlias(fieldName);
            PropertyList list;
            if (alias != null) {
                list = getPropertyListMap().getOrDefault(fieldName, new PropertyList(alias));
            } else {
                list = getPropertyListMap().getOrDefault(fieldName, new PropertyList(StringUtils.camelToUnderline(fieldName)));
            }
            propertyData.setId(finalId);
            propertyData.setValue(value);
            list.getDataList().add(propertyData);
            getPropertyListMap().put(fieldName, list);
        });
    }

    public MapBuilder handleObjectForBatch(Collection<?> objects, boolean containNull, FieldFilter<?> fieldFilter) {
        for (Object object : objects) {
            handleObjectForBatch(object, containNull, fieldFilter);
        }
        return this;
    }

    private Map<String, PropertyList> getPropertyListMap() {
        if (propertyListMap == null) {
            propertyListMap = new HashMap<>();
        }
        return propertyListMap;
    }

    private List<PropertyList> getPropertyList() {
        if (propertyList == null) {
            propertyList = new ArrayList<>();
        }
        return propertyList;
    }

    private List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    private List<List<Object>> getPropertiesValueBatch() {
        if (propertiesValueBatch == null) {
            propertiesValueBatch = new ArrayList<>();
        }
        return propertiesValueBatch;
    }

    private List<Object> getIdList() {
        if (idList == null) {
            idList = new ArrayList<>();
        }
        return idList;
    }

    private boolean isBasicType(Object o) {
        return o instanceof String || o instanceof Integer || o instanceof Long || o instanceof Date || o instanceof Double || o instanceof Enum || o instanceof Boolean || o instanceof BigDecimal;
    }

    public MapBuilder handleUpdateObject(Object object, boolean containNull) {
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        if (idName == null) {
            idName = Optional.ofNullable(tableInfo.getAlias(tableInfo.getKeyColumnName())).orElse(tableInfo.getKeyColumnName());
        }
        ReflectionUtils.doWithFields(clazz, field -> {
            String fieldName = field.getName();
            if (passFieldsName.contains(fieldName)) {
                return;
            }
            Property property = new Property();
            String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
            Method getMethod = null;
            getMethod = tableInfo.getMethod(getMethodName);
            if (getMethod == null) {
                return;
            }
            Object value = null;
            try {
                value = getMethod.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new GeneralBeanException("方法" + getMethodName + "无法访问");
            }
            if (value != null && !isBasicType(value))
                return;
            // idName对应的值
            String keyAlias = tableInfo.getAlias(fieldName);
            if (idName.equalsIgnoreCase(Optional.ofNullable(keyAlias).orElse(fieldName))) {
                if (value == null) {
                    throw new GeneralBeanException(String.format("更新的 (%s) 主键为空,更新失败", fieldName));
                }
                idValue = value;
                return;
            }
            if (!containNull && value == null)
                return;
            String alias = tableInfo.getAlias(fieldName);
            if (alias != null) {
                property.setName(alias);
            } else {
                property.setName(StringUtils.camelToUnderline(fieldName));
            }
            property.setValue(value);
            properties.add(property);
        });
        return this;
    }

    public static void setNeedPass(List<String> passName) {
        needPass = new HashSet<>(passName);
    }
}
