package com.zhiyi.generalbeanplus;

import com.zhiyi.generalbeanplus.exception.GeneralBeanException;
import com.zhiyi.generalbeanplus.mapper.BeanDaoHandler;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.model.*;
import com.zhiyi.generalbeanplus.support.MapBuilder;
import com.zhiyi.generalbeanplus.support.ObjGenerator;
import com.zhiyi.generalbeanplus.util.StringUtils;
import com.zhiyi.generalbeanplus.wrapper.AbstractWrapper;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通用DAO层
 *
 * @param
 * @author weixiaofeng
 */
public class GeneralBeanService {

    private static final Logger logger = LoggerFactory.getLogger(GeneralBeanService.class);

    public static final List<String> DELETED_AT_IS_NULL = Collections.singletonList("AND deleted_at IS NULL");

    final BeanDaoHandler handleDao;

    public GeneralBeanService(BeanDaoHandler handleDao) {
        this.handleDao = handleDao;
    }

    /**
     * 当id为空的时候add记录 当id不为空的时候update
     *
     * @param object
     */
    public void save(@NonNull Object object) {
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        String keyColumnName = tableInfo.getKeyColumnName();
        String getMethodName = StringUtils.propertyToGetMethodName(keyColumnName);
        Method getMethod;
        try {
            getMethod = clazz.getMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            throw new GeneralBeanException("未找到 " + getMethodName + " 方法");
        }
        Object value = null;
        try {
            value = getMethod.invoke(object);
        } catch (IllegalAccessException e) {
            throw new GeneralBeanException(getMethodName + "方法无法访问", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (value == null) {
            add(object);
        } else {
            update(object);
        }
    }

    public void add(Object object) {
        add(object, false);
    }

    /**
     * 传入对象,保存进数据库 对象和表名的关系ClothFlaw 对应 表 clothFlaw 第一个字母变小写,如果表名不同,需要指定表名
     * 插入的列名和要对象的属性名称一样
     *
     * @param object 待添加的实体
     */
    public void add(@NonNull Object object, boolean containNull) {
        MapBuilder mapBuilder = new MapBuilder();
        //获取表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(object);
        String tableName = tableInfo.getTableName();
        Class<?> clazz = object.getClass();
        Field idField = mapBuilder.setTableName(tableName).handleObject(object, containNull);
        Map<String, Object> para = mapBuilder.build();
        handleDao.add(para);
        // 设置回Id
        if (idField != null) {
            //获取主键设置方法
            String setMethodName = StringUtils.propertyToSetMethodName(tableInfo.getKeyColumnName());
            Method setMethod = null;
            try {
                setMethod = clazz.getMethod(setMethodName, idField.getType());
            } catch (NoSuchMethodException e) {
                throw new GeneralBeanException("找不到" + setMethodName + "方法");
            }
            try {
                // int主键
                try {
                    setMethod.invoke(object, para.get("id"));
                } catch (Exception e) {
                    // long主键
                    setMethod.invoke(object, ((Integer) para.get("id")).longValue());
                }
            } catch (IllegalAccessException e) {
                throw new GeneralBeanException("无法访问" + setMethodName + "方法");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据map插入数据库,返回的map中有id信息
     *
     * @param para 参数键值对
     * @return para
     * @throws Exception
     */
    public Map<String, Object> add(Map<String, Object> para) {
        handleDao.add(para);
        return para;
    }

    /**
     * 传入对象,保存进数据库 对象和表名的关系ClothFlaw 对应 表 clothFlaw 第一个字母变小写,如果表名不同,需要指定表名
     * 插入的列名和要对象的属性名称一样
     *
     * @param oList 待添加的实体对象列表
     * @throws Exception
     */
    public void add(Collection<?> oList, boolean containNull) {
        if (CollectionUtils.isEmpty(oList)) {
            return;
        }
        Map<String, Object> para = new MapBuilder()
                .handleObject(oList, containNull).build();
        handleDao.addList(para);
    }

    public void add(Collection<?> oList) {
        add(oList, true);
    }

    public boolean isBasicType(Object o) {
        return o instanceof String || o instanceof Integer || o instanceof Long || o instanceof Date || o instanceof Double || o instanceof Enum || o instanceof Boolean || o instanceof BigDecimal;
    }

    /**
     * 通用的更新 对象和表名的关系ClothFlaw 对应 表 clothFlaw 第一个字母变小写,如果表名不同,需要指定表名
     * 更新的列名和要对象的属性名称一样
     *
     * @param object    待更新的实体
     * @param tableName 表名
     * @param idName    根据idName进行更新 , 即where idName = xxx idName必须为object的一个属性
     * @throws SecurityException
     * @throws Exception
     */
    public void update(Object object, String tableName, String idName, boolean containNull) {
        if (object == null)
            throw new GeneralBeanException("object不能为空");
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        if (tableName == null) {
            tableName = tableInfo.getTableName();
        }
        List<Property> pros = new ArrayList<>();
        MapBuilder mapBuilder = new MapBuilder();
        Map<String, Object> para = mapBuilder.setTableName(tableName)
                .setProperties(pros)
                .setIdName(idName)
                .handleUpdateObject(object, containNull).build();
        // 设置更新字段
        handleDao.update(para);
    }

    /**
     * 通用的更新 对象和表名的关系ClothFlaw 对应 表 clothFlaw 第一个字母变小写,如果表名不同,需要指定表名
     * 更新的列名和要对象的属性名称一样
     *
     * @param object 待更新的实体
     * @param idName 根据idName进行更新 , 即where idName = xxx idName必须为object的一个属性
     * @throws Exception
     */
    public void update(Object object, String idName) {
        update(object, null, idName, false);
    }

    /**
     * 根据id字段update
     *
     * @param object
     */
    public void update(Object object) {
        update(object, null, null, false);
    }

    public void update(Object object, boolean containNull) {
        update(object, null, null, containNull);
    }

    public void update(Object object, AbstractWrapper<?, ?, ?> wrapper) {
        MapBuilder mapBuilder = new MapBuilder();
        mapBuilder.handleObject(object, false);
        if(StringUtils.isBlank(wrapper.getSqlSegment())){
            throw new GeneralBeanException("在更新时，不允许更新条件为空！");
        }
        Map<String, Object> para = mapBuilder.setWrapper(wrapper).build();
        handleDao.updateByWrapper(para);
    }

    /**
     * 批量选择性更新
     */
    public void batchUpdate(Collection<?> objectList) {
        batchUpdate(objectList, false);
    }

    /**
     * 全量更新，即使属性为null
     *
     * @param objectList
     */
    public void batchUpdate(Collection<?> objectList, boolean containNull) {
        if (objectList == null || objectList.size() == 0) {
            throw new GeneralBeanException("objectList不能为空");
        }
        Map<String, Object> para = new MapBuilder().handleObjectForBatch(objectList, containNull).build();
        handleDao.batchUpdate(para);
    }

    /**
     * 通用的删除 对象和表名的关系ClothFlaw 对应 表 clothFlaw 第一个字母变小写,如果表名不同,需要指定表名
     *
     * @param object    待删除的实体
     * @param tableName 表名
     * @param idName    删除条件名称,根据idName进行删除, dName必须为object的一个属性
     * @throws Exception
     */
    public void delete(@Nullable Object object, String tableName, String idName) {
        if (object == null)
            throw new RuntimeException("object和idName不能为空");

        TableInfo tableInfo = TableInfoHelper.getTableInfo(object);
        // 表名为空,按照默认规则生成表名
        if (StringUtils.isEmpty(tableName)) {
            tableName = tableInfo.getTableName();
        }
        if (StringUtils.isEmpty(idName)) {
            idName = tableInfo.getKeyColumnName();
        }
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(idName);
            String name = field.getName();
            String getMethodName = StringUtils.propertyToGetMethodName(name);
            Method getMethod = clazz.getMethod(getMethodName);
            Object idValue = getMethod.invoke(object);
            Map<String, Object> para = new MapBuilder()
                    .setTableName(tableName)
                    .setIdValue(idValue)
                    .setIdName(idName)
                    .build();
            handleDao.delete(para);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param object 待删除实体
     * @throws Exception
     */
    public void delete(Object object) {
        delete(object, null, null);
    }

    /**
     * @param object 待删除实体
     * @param idName 主键字段名
     * @throws Exception
     */
    public void delete(Object object, String idName) {
        delete(object, null, idName);
    }

    /**
     * 删除
     *
     * @param clazz
     * @param name
     * @param value
     */
    public <T> void quickDelete(Class<T> clazz, String name, Object value) {
        try {
            T object = (T) clazz.newInstance();
            Field field = clazz.getDeclaredField(name);
            String fieldName = field.getName();
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String setMethodName = "set" + firstLetter + fieldName.substring(1);
            Method method = clazz.getDeclaredMethod(setMethodName, field.getType());
            method.invoke(object, value);

            delete(object, null, name);
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }


    @SuppressWarnings("unchecked")
    public <T> PageSet<T> query(@Nullable T object, String tableName, @Nullable List<String> likeProperties, @Nullable List<String> userPara, Integer start, Integer limit) {
        return query(object, tableName, likeProperties, userPara, start, limit, false);
    }

    /**
     * 通用的分页查询 根据object中的不为空的属性进行查询 对象和表名的关系ClothFlaw 对应 表 clothFlaw
     * 第一个字母变小写,如果表名不同,需要指定表名 支持like查询方式,需要like查询的属性以likeProperties字符串链表的方式传入,
     * 传入的属性要在object中存在 支持自定义sql片段查询. List<String> userPara = new ArrayList<>();
     * userPara.add("and name like '%time%'");
     * <p>
     * 带分页的查询 start,limit
     *
     * @param <T>
     * @param <T>
     * @param object         等值查询条件
     * @param tableName      表名
     * @param likeProperties 相似查询条件
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> PageSet<T> query(@Nullable T object, String tableName, @Nullable List<String> likeProperties, @Nullable List<String> userPara, Integer start, Integer limit, boolean includeDeletedData) {
        if (likeProperties == null)
            likeProperties = new ArrayList<>();

        if (includeDeletedData) {
            if (CollectionUtils.isEmpty(userPara)) {
                userPara = new ArrayList<>();
            }
        } else {
            List<String> deletedAtIsNull = DELETED_AT_IS_NULL;
            if (CollectionUtils.isEmpty(userPara)) {
                userPara = deletedAtIsNull;
            } else {
                userPara = new ArrayList<>(userPara);
                userPara.addAll(deletedAtIsNull);
            }
        }

        PageSet<T> pageSet = new PageSet<>();
        List<T> resultList = new ArrayList<>();
        pageSet.setResultList(resultList);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(object);
        if (object == null)
            throw new RuntimeException("object不能为空");

        Map<String, Object> para = new HashMap<>();

        // 表名为空,按照默认规则生成表名
        if (StringUtils.isEmpty(tableName)) {
            tableName = tableInfo.getTableName();
        }
        para.put("tableName", tableName);

        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        // 普通属性
        List<Property> pros = new ArrayList<>();
        // like属性
        List<Property> likePros = new ArrayList<>();
        para.put("properties", pros);
        para.put("likeProperties", likePros);
        // 用户自己的sql语句片段
        para.put("userPara", userPara);
        para.put("start", start);
        para.put("limit", limit);
        // 设置查询字段
        for (Field field : fields) {
            try {
                Property property = new Property();
                String name = field.getName();
                // 因为mybatis映射Object里的list时用的是get方法，而如果get得到一个null会报空指针，所以全都返回一个空list，这里会产生冲突，所以过滤掉list类型属性
                Type type = field.getType();
                if (type.toString().equals("interface java.util.List")) {
                    continue;
                }
                String firstLetter = name.substring(0, 1).toUpperCase();
                String getMethodName = "get" + firstLetter + name.substring(1);
                Method getMethod = clazz.getMethod(getMethodName);

                Object value = getMethod.invoke(object);
                if (value == null)
                    continue;
                String alias = tableInfo.getAlias(name);
                if (!alias.equals(name)) {
                    property.setName(alias);
                } else {
                    property.setName(StringUtils.camelToUnderline(name));
                }
                property.setValue(value);
                if (likeProperties.contains(name))
                    likePros.add(property);
                else
                    pros.add(property);
            } catch (Exception e) {
                // 跳过没有get的属性
            }

        }

        List<Map<String, Object>> result = handleDao.query(para);

        // 时间类型转换为String
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 查询结果封装为Object
        for (Map<String, Object> map : result) {

            T resultObject = null;
            try {
                resultObject = (T) object.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }

            resultList.add(resultObject);

            Set<String> keySet = map.keySet();
            for (String aKeySet : keySet) {
                try {
                    Object value = map.get(aKeySet);
                    // 值是空,则不映射到对象
                    if (value == null || !isBasicType(value)) {
                        continue;
                    }

                    Field field = clazz.getDeclaredField(StringUtils.underlineToCamelWithMatcher(aKeySet));
                    if (field == null) {
                        continue;
                    }

                    String name = field.getName();
                    String firstLetter = name.substring(0, 1).toUpperCase();
                    String setMethodName = "set" + firstLetter + name.substring(1);
                    Method method = clazz.getDeclaredMethod(setMethodName, field.getType());

                    if (field.getType().isEnum()) {
                        // 枚举类型
                        Object objects[] = field.getType().getEnumConstants();
                        for (Object o : objects) {
                            if (value.toString().equalsIgnoreCase(o.toString())) {
                                method.invoke(resultObject, o);
                            }
                        }

                    }
                    if (field.getType().toString().contains("Boolean")) {
                        if ((int) value == 0)
                            method.invoke(resultObject, Boolean.FALSE);
                        else
                            method.invoke(resultObject, Boolean.TRUE);
                    } else if (field.getType().toString().contains("Integer")) {
                        method.invoke(resultObject, Integer.valueOf(String.valueOf(value)));
                    } else if (field.getType().toString().contains("Long")) {
                        method.invoke(resultObject, Long.valueOf(String.valueOf(value)));
                    } else {
                        if (field.getType().toString().contains("String") && value instanceof Timestamp) {
                            String format = sdf.format(value);
                            method.invoke(resultObject, format);
                        } else {
                            method.invoke(resultObject, value);
                        }
                    }
                } catch (Exception ignored) {
                    logger.error(ignored.getMessage());
                }

            }
        }

        int count = handleDao.queryCount(para);
        pageSet.setResultCount(count);
        return pageSet;
    }

    /**
     * 通用的分页查询 根据object中的不为空的属性进行查询 对象和表名的关系ClothFlaw 对应 表 clothFlaw
     * 第一个字母变小写,如果表名不同,需要指定表名 支持like查询方式,需要like查询的属性以likeProperties字符串链表的方式传入,
     * 传入的属性要在object中存在 支持自定义sql片段查询. List<String> userPara = new ArrayList<>();
     * userPara.add("and name like '%time%'"); 不需要分页
     *
     * @param <T>
     * @param <T>
     * @param object         等值查询条件
     * @param tableName      表名
     * @param likeProperties 相似查询条件
     * @throws Exception
     */
    public <T> PageSet<T> query(@Nullable T object, String tableName, @Nullable List<String> likeProperties, @Nullable List<String> userPara) {
        return query(object, tableName, likeProperties, userPara, null, null);
    }

    /**
     * 通用的分页查询 根据object中的不为空的属性进行查询 对象和表名的关系ClothFlaw 对应 表 clothFlaw
     * 第一个字母变小写,如果表名不同,需要指定表名
     *
     * @param <T>
     * @param object    查询条件
     * @param tableName 表名
     * @throws Exception
     */
    public <T> PageSet<T> query(T object, String tableName) {
        return query(object, tableName, null, null);
    }

    /**
     * 默认表名,不分页
     *
     * @param <T>
     * @param object 查询条件
     * @return 查询结果
     * @throws Exception
     */
    public <T> PageSet<T> query(T object) {
        return query(object, null, null, null);
    }

    public <T> PageSet<T> query(T object, PageSet pageSet) {
        return query(object, null, null, null, pageSet.getStart(), pageSet.getPageSize());
    }

    /**
     * 默认表名,不分页 , 包含like属性
     *
     * @param <T>
     * @param object 查询条件
     * @return 查询结果
     * @throws Exception
     */
    public <T> PageSet<T> query(T object, List<String> likeProperties) {

        return query(object, null, likeProperties, null);
    }


    /**
     * 查询单条结果
     *
     * @param object 查询条件
     * @return 查询结果
     */
    @Nullable
    public <T> T queryOne(T object) {

        try {
            return query(object, null, null, null).getResultList().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询单条结果,包含like属性
     *
     * @param object 查询条件
     * @return 查询结果
     */
    @Nullable
    public <T> T queryOne(T object, String tableName, List<String> likeProperties) {

        try {
            return query(object, tableName, likeProperties, null).getResultList().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询单条结果,包含like属性
     *
     * @param <T>
     * @param object 查询条件
     * @return 单条结果（查不到就null）
     */
    @Nullable
    public <T> T queryOne(T object, List<String> likeProperties) {
        try {
            return query(object, null, likeProperties, null).getResultList().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询单条结果
     *
     * @param object 查询条件
     * @return 查询结果
     */
    @Nullable
    public <T> T queryOne(T object, String tableName) {

        try {
            return query(object, tableName, null, null).getResultList().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 快速查询只有一个查询参数的对象
     *
     * @param clazz 待查询的实体类型
     * @param name  参数名
     * @param value 参数值
     * @return 单条结果
     * @throws Exception
     */
    @Nullable
    public <T> T quickQueryOne(Class<T> clazz, String name, Object value) {
        if (value == null)
            throw new RuntimeException("参数值不能为空");

        try {
            T object = (T) clazz.newInstance();
            Field field = clazz.getDeclaredField(name);
            String fieldName = field.getName();
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String setMethodName = "set" + firstLetter + fieldName.substring(1);
            Method method = clazz.getDeclaredMethod(setMethodName, field.getType());
            method.invoke(object, value);

            return queryOne(object);
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * 查询list结果,不分页
     *
     * @param <T>
     * @param object         查询条件
     * @param likeProperties 相似查询条件
     * @return 结果列表
     */
    @Nullable
    public <T> List<T> queryList(T object, List<String> likeProperties) {
        try {
            return query(object, null, likeProperties, null).getResultList();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 查询list结果,不分页,包含like属性
     *
     * @param <T>
     * @param object 查询条件
     * @return 结果列表
     */
    @Nullable
    public <T> List<T> queryList(T object) {
        try {
            return query(object, null, null, null).getResultList();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 根据过滤器查询
     *
     * @param wrapper
     * @param <T>
     * @return
     */
    public <T> List<T> queryList(AbstractWrapper<T, ?, ?> wrapper, PageSet<?> pageSet) {
        List<Map<String, Object>> maps = handleDao.queryList(wrapper, pageSet);
        ObjGenerator<T> tObjGenerator = new ObjGenerator<T>(wrapper.getEntityClazz());
        return tObjGenerator.generatorObj(maps);
    }

    /**
     * 根据过滤器查询
     *
     * @param wrapper
     * @param <T>
     * @return
     */
    public <T> List<T> queryList(AbstractWrapper<T, ?, ?> wrapper) {
        return queryList(wrapper, (PageSet<?>) null);
    }

    /**
     * 根据条件查询数量
     *
     * @param wrapper
     * @return
     */
    public Integer count(AbstractWrapper<?, ?, ?> wrapper) {
        return handleDao.count(wrapper);
    }

    /**
     * 快速查询只有一个参数的列表
     *
     * @param <T>
     * @param clazz 待查询的实体类型
     * @param name  参数名
     * @param value 参数值
     * @return 结果列表
     * @throws Exception
     */
    @Nullable
    public <T> List<T> quickQueryList(Class<T> clazz, String name, Object value) {
        try {
            T object = clazz.newInstance();
            Field field = clazz.getDeclaredField(name);
            String fieldName = field.getName();
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String setMethodName = "set" + firstLetter + fieldName.substring(1);
            Method method = clazz.getDeclaredMethod(setMethodName, field.getType());
            method.invoke(object, value);

            return queryList(object);
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询list结果,不分页
     *
     * @param <T>
     * @param object   查询条件
     * @param userPara 自定义查询条件
     * @return 结果列表
     */
    @Nullable
    public <T> List<T> queryListByPara(T object, List<String> userPara) {
        return queryListByPara(object, null, userPara);
    }

    /**
     * 可以自己拼写sql条件. exp: List<String> userPara = new ArrayList<>();
     * userPara.add("and name like '%time%'");
     *
     * @param <T>
     * @param object   查询条件
     * @param userPara 自定义sql片段
     * @return 结果列表
     */
    @Nullable
    public <T> List<T> queryListByPara(T object, String tableName, List<String> userPara) {
        try {
            return query(object, tableName, null, userPara).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据属性的多个值查询指定对象
     *
     * @param cls       目标对象的类
     * @param tableName 表名
     * @param propName  属性名
     * @param values    多个值
     * @return 查询到的对象列表
     */
    public <T> List<T> queryByMultiValue(Class<T> cls, String tableName, String propName, List values) {
        List<Map<String, Object>> dbRows = handleDao.queryByMultiValue(tableName, propName, values);

        List<T> result = new ArrayList<>();

        if (dbRows.isEmpty())
            return result;

        /*
         * for (Map<String, Object> row : dbRows) { try { T t =
         * cls.newInstance(); BeanUtils.populate(t, row); result.add(t); } catch
         * (InstantiationException | IllegalAccessException |
         * InvocationTargetException e) { logger.error(e.getMessage()); throw
         * new RuntimeException(e); } }
         */
        return result;
    }

}
