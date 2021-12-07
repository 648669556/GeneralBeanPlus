package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.exception.GeneralBeanException;
import com.zhiyi.generalbeanplus.interfaces.SelectColumn;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.support.FieldFilter;
import com.zhiyi.generalbeanplus.util.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * Entity 对象封装操作类
 *
 * @author hubin miemie HCL
 * @since 2018-05-25
 */
@SuppressWarnings("serial")
public class QueryWrapper<T> extends AbstractWrapper<T, String, QueryWrapper<T>> implements SelectColumn {
    public QueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
        boxDO(entity);
    }

    public QueryWrapper(Class<T> clazz) {
        super.setEntityClazz(clazz);
        super.initNeed();
    }

    /**
     * 非对外公开的构造方法,只用于生产嵌套 sql
     *
     * @param entityClass 本不应该需要的
     */
    private QueryWrapper(T entity, Class<T> entityClass,
                         MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.setEntityClazz(entityClass);
        this.expression = mergeSegments;
    }

    public QueryWrapper<T> selectFieldFilter(FieldFilter<T> fieldFilter) {
        this.fieldFilter = fieldFilter;
        return typedThis;
    }

    @Override
    public String getColumn() {
        if (fieldFilter == null) {
            return "*";
        }
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(super.getEntityClazz());
        Field[] fields = super.getEntityClazz().getDeclaredFields();
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (passFieldsName.contains(fieldName)) {
                continue;
            }
            if (fieldFilter.test(fieldName)) {
                continue;
            }
            String alias = Optional.ofNullable(tableInfo.getAlias(fieldName)).orElse(fieldName);
            columns.add(StringUtils.camelToUnderline(alias));
        }
        return String.join(",", columns);
    }

    @Override
    public String getTableName() {
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(getEntityClazz());
        return Optional.ofNullable(tableInfo).map(TableInfo::getTableName).orElse(null);
    }

    @Override
    public String getSqlSegment() {
        return getExpression().getSqlSegment(false);
    }

    @Override
    protected String columnSqlInjectFilter(String column) {
        return StringUtils.replaceBlank(column);
    }

    /**
     * 返回一个支持 lambda 函数写法的 wrapper
     */
    public LambdaQueryWrapper<T> lambda() {
        return new LambdaQueryWrapper<>(getEntity(), getEntityClazz(), expression, fieldFilter);
    }

    /**
     * 用于生成嵌套 sql
     * <p>
     * 故 sqlSelect 不向下传递
     * </p>
     */
    @Override
    protected QueryWrapper<T> instance() {
        return new QueryWrapper<>(getEntity(), getEntityClazz(), new MergeSegments());
    }

    @Override
    public void clear() {
        super.clear();
    }

    private boolean isBasicType(Object o) {
        return o instanceof String || o instanceof Integer || o instanceof Long || o instanceof Date || o instanceof Double || o instanceof Enum || o instanceof Boolean || o instanceof BigDecimal;
    }

    /**
     * 将do类的属性作为eq条件
     *
     * @param target
     */
    public void boxDO(T target) {
        Class<?> clazz = target.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(clazz);
        Set<String> passFieldsName = tableInfo.getPassFieldsName();
        ReflectionUtils.doWithFields(clazz, field -> {
            String fieldName = field.getName();
            String alias = Optional.ofNullable(tableInfo.getAlias(fieldName)).orElse(fieldName);
            if (passFieldsName.contains(fieldName)) {
                return;
            }
            String getMethodName = StringUtils.propertyToGetMethodName(fieldName);
            Method getMethod = null;
            getMethod = tableInfo.getMethod(getMethodName);
            if (getMethod == null) return;
            Object value = null;
            try {
                value = getMethod.invoke(target);
            } catch (IllegalAccessException e) {
                throw new GeneralBeanException("无法访问" + getMethodName);
            } catch (InvocationTargetException targetException) {
                targetException.printStackTrace();
            }
            if (!isBasicType(value)) {
                return;
            }
            eq(alias, value);
        });
    }
}
