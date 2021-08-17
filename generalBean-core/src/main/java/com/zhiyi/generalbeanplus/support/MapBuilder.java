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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * map 属性参数建造工厂
 */
public class MapBuilder {
    private static Set<String> needPass;

    private String tableName;

    private List<Property> properties;

    private Integer id;

    private List<Object> idList;

    private List<PropertyList> propertyList;

    private Map<String, PropertyList> propertyListMap;

    private List<List<Object>> propertiesValueBatch;

    private String idName;

    private Object idValue;

    private AbstractWrapper<?, ?, ?> wrapper;

    public MapBuilder MapBuilder() {
        return this;
    }

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
            para.put("idName", StringUtils.camelToUnderline(idName));
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

        Field[] fields = clazz.getDeclaredFields();

        Field idField = null;

        for (Field field : fields) {
            Property property = new Property();
            String name = field.getName();
            if (passFieldsName.contains(name)) {
                //跳过不需要的
                continue;
            }
            if (name.equalsIgnoreCase("start") || name.equalsIgnoreCase("limit"))
                continue;
            if (keyColumnName.equalsIgnoreCase(name)) {
                //如果为主键
                idField = field;
                continue;
            }
            //获取属性的get方法
            String getMethodName = StringUtils.propertyToGetMethodName(name);
            Method getMethod;
            try {
                getMethod = clazz.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                //如果没有找到get方法，则跳过此属性
                continue;
            }
            Object value = null;
            try {
                value = getMethod.invoke(object);
            } catch (IllegalAccessException e) {
                //get方法不是public
                throw new GeneralBeanException(getMethodName + " 方法无法访问");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            // value为空或者不是基础数据
            if (!containNull && value == null || containNull && value != null && !isBasicType(value))
                continue;
            String alias = tableInfo.getAlias(name);
            if (!alias.equals(name)) {
                property.setName(alias);
            } else {
                property.setName(StringUtils.camelToUnderline(alias));
            }
            property.setValue(value);
            pros.add(property);
        }

        return idField;
    }

    public MapBuilder handleObject(Collection<?> objects, boolean containNull) {
        if (objects.isEmpty()) {
            return this;
        }
        Set<String> properties = new HashSet<>();
        TableInfo tableInfo = null;
        String keyColumnName = null;
        Class<?> clazz = null;
        Set<String> passFieldsName = null;
        for (Object next : objects) {
            List<Object> objectList = new ArrayList<>();
            if (tableInfo == null) {
                tableInfo = TableInfoHelper.getTableInfo(next);
                tableName = tableInfo.getTableName();
                keyColumnName = tableInfo.getKeyColumnName();
                clazz = next.getClass();
                passFieldsName = tableInfo.getPassFieldsName();
            }
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                if (passFieldsName.contains(fieldName)) {
                    continue;
                }
                if (needPass.contains(fieldName))
                    continue;
                if (keyColumnName.equalsIgnoreCase(fieldName)) {
                    //如果为主键
                    continue;
                }
                String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
                Method getMethod;
                try {
                    getMethod = clazz.getMethod(getMethodName);
                } catch (NoSuchMethodException e) {
                    //如果没有找到get方法，则跳过此属性
                    continue;
                }
                Object value = null;
                try {
                    value = getMethod.invoke(next);
                } catch (IllegalAccessException e) {
                    //get方法不是public
                    throw new GeneralBeanException(getMethodName + " 方法无法访问");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                // value为空或者不是基础数据
                if (!containNull && value == null || value != null && !isBasicType(value))
                    continue;
                objectList.add(value);
                boolean added = properties.add(fieldName);
                if (added) {
                    Property property = new Property();
                    String alias = tableInfo.getAlias(fieldName);
                    if (!alias.equals(fieldName)) {
                        property.setName(alias);
                    } else {
                        property.setName(StringUtils.camelToUnderline(fieldName));
                    }
                    getProperties().add(property);
                }
            }
            getPropertiesValueBatch().add(objectList);
        }
        return this;
    }

    public MapBuilder handleObjectForBatch(Object object, boolean containNull) {
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        Field[] fields = clazz.getDeclaredFields();
        //主键名称
        String keyColumnName = tableInfo.getKeyColumnName();
        //被跳过的属性
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        if (tableName == null) {
            tableName = tableInfo.getTableName();
        }
        //获取id
        Object id = null;
        Method getId;
        String getIdMethodName = StringUtils.propertyToGetMethodName(keyColumnName);
        String keyAlias = tableInfo.getAlias(keyColumnName);
        if (!keyAlias.equals(keyColumnName)) {
            idName = keyAlias;
        } else {
            idName = StringUtils.camelToUnderline(keyColumnName);
        }
        try {
            getId = clazz.getMethod(getIdMethodName);
        } catch (NoSuchMethodException e) {
            throw new GeneralBeanException("do类不存在方法" + getIdMethodName);
        }
        try {
            //获得类的id
            id = Optional.ofNullable(getId.invoke(object)).orElseThrow(() -> new GeneralBeanException("do类id不得为空"));
            getIdList().add(id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("do类" + getIdMethodName + "方法不能访问");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        for (Field field : fields) {
            PropertyData propertyData = new PropertyData();
            String fieldName = field.getName();
            if (passFieldsName.contains(fieldName)) {
                continue;
            }
            if (needPass.contains(fieldName))
                continue;
            if (keyColumnName.equalsIgnoreCase(fieldName)) {
                //跳过id主键的更新
                continue;
            }
            String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
            Method getMethod;
            Object value = null;
            try {
                getMethod = clazz.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                //跳过没有get方法的属性
                continue;
            }
            try {
                //获取值
                value = getMethod.invoke(object);
                if (!containNull && value == null || value != null && !isBasicType(value)) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new GeneralBeanException("do类" + getMethodName + "方法不能访问");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            String alias = tableInfo.getAlias(fieldName);
            PropertyList list;
            if (!alias.equals(fieldName)) {
                list = getPropertyListMap().getOrDefault(fieldName, new PropertyList(alias));
            } else {
                list = getPropertyListMap().getOrDefault(fieldName, new PropertyList(StringUtils.camelToUnderline(fieldName)));
            }
            propertyData.setId(id);
            propertyData.setValue(value);
            list.getDataList().add(propertyData);
            getPropertyListMap().put(fieldName, list);
        }
        return this;
    }

    public MapBuilder handleObjectForBatch(Collection<?> objects, boolean containNull) {
        for (Object object : objects) {
            handleObjectForBatch(object, containNull);
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
        Field[] fields = clazz.getDeclaredFields();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        if (idName == null) {
            idName = tableInfo.getAlias(tableInfo.getKeyColumnName());
        }
        // 设置更新字段
        for (Field field : fields) {
            String fieldName = field.getName();
            if (passFieldsName.contains(fieldName)) {
                continue;
            }
            Property property = new Property();
            String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
            Method getMethod = null;
            try {
                getMethod = clazz.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                continue;
            }
            Object value = null;
            try {
                value = getMethod.invoke(object);
            } catch (IllegalAccessException e) {
                throw new GeneralBeanException("方法" + getMethodName + "无法访问");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (value != null && !isBasicType(value))
                continue;
            // idName对应的值
            if (idName.equalsIgnoreCase(tableInfo.getAlias(fieldName))) {
                if (value == null) {
                    throw new GeneralBeanException(String.format("更新的 (%s) 主键为空,更新失败", fieldName));
                }
                idValue = value;
                continue;
            }
            if (!containNull && value == null)
                continue;
            String alias = tableInfo.getAlias(fieldName);
            if (!alias.equals(fieldName)) {
                property.setName(alias);
            } else {
                property.setName(StringUtils.camelToUnderline(fieldName));
            }
            property.setValue(value);
            properties.add(property);
        }
        return this;
    }

    public static void setNeedPass(List<String> passName) {
        needPass = new HashSet<>(passName);
    }
}
