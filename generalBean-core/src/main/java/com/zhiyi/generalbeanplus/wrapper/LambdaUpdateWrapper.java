package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lambda 更新封装
 *
 * @author hubin miemie HCL
 * @since 2018-05-30
 */
@SuppressWarnings("serial")
public class LambdaUpdateWrapper<T> extends AbstractLambdaWrapper<T, LambdaUpdateWrapper<T>> {

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;

    public LambdaUpdateWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    public LambdaUpdateWrapper(Class<T> entityClazz) {
        super.setEntityClazz(entityClazz);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    LambdaUpdateWrapper(T entity, Class<T> entityClass, List<String> sqlSet,
                        MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.setEntityClazz(entityClass);
        this.sqlSet = sqlSet;
        this.expression = mergeSegments;
    }

    public LambdaUpdateWrapper(T entity, List<String> sqlSet, MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = sqlSet;
        this.expression = mergeSegments;
    }

    @Override
    protected LambdaUpdateWrapper<T> instance() {
        return new LambdaUpdateWrapper<T>(getEntity(), null, new MergeSegments());
    }

    @Override
    public String getTableName() {
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(getEntityClazz());
        return Optional.ofNullable(tableInfo).map(TableInfo::getTableName).orElse(null);
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}
