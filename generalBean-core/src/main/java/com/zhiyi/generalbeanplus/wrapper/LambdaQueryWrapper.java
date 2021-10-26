package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.interfaces.SelectColumn;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.support.FieldFilter;
import com.zhiyi.generalbeanplus.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Lambda 语法使用 Wrapper
 *
 * @author hubin miemie HCL
 * @since 2017-05-26
 */
@SuppressWarnings("serial")
public class LambdaQueryWrapper<T> extends AbstractLambdaWrapper<T, LambdaQueryWrapper<T>> implements SelectColumn {


    public LambdaQueryWrapper() {
        this((T) null);
    }

    public LambdaQueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public LambdaQueryWrapper(Class<T> entityClass) {
        super.setEntityClazz(entityClass);
        super.initNeed();
    }

    LambdaQueryWrapper(T entity, Class<T> entityClass, MergeSegments mergeSegments, FieldFilter<T> fieldFilter) {
        super.setEntity(entity);
        super.setEntityClazz(entityClass);
        this.expression = mergeSegments;
        super.setFieldFilter(fieldFilter);
    }

    @Override
    public String getTableName() {
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(getEntityClazz());
        return Optional.ofNullable(tableInfo).map(TableInfo::getTableName).orElse(null);
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

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected LambdaQueryWrapper<T> instance() {
        return new LambdaQueryWrapper<>(getEntity(), getEntityClazz(), new MergeSegments(), fieldFilter);
    }

    @Override
    public void clear() {
        super.clear();
    }
}
