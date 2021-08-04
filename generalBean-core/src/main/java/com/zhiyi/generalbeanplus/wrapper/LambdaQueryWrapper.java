package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;

import java.util.Optional;


/**
 * Lambda 语法使用 Wrapper
 *
 * @author hubin miemie HCL
 * @since 2017-05-26
 */
@SuppressWarnings("serial")
public class LambdaQueryWrapper<T> extends AbstractLambdaWrapper<T, LambdaQueryWrapper<T>> {


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

    LambdaQueryWrapper(T entity, Class<T> entityClass, MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.setEntityClazz(entityClass);
        this.expression = mergeSegments;
    }

    @Override
    public String getTableName() {
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(getEntityClazz());
        return Optional.ofNullable(tableInfo).map(TableInfo::getTableName).orElse(null);
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected LambdaQueryWrapper<T> instance() {
        return new LambdaQueryWrapper<>(getEntity(), getEntityClazz(), new MergeSegments());
    }

    @Override
    public void clear() {
        super.clear();
    }
}
