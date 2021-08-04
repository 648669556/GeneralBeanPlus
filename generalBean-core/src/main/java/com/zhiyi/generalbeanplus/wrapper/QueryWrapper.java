package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.util.StringUtils;

import java.util.Optional;

/**
 * Entity 对象封装操作类
 *
 * @author hubin miemie HCL
 * @since 2018-05-25
 */
@SuppressWarnings("serial")
public class QueryWrapper<T> extends AbstractWrapper<T, String, QueryWrapper<T>> {
    public QueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
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
        return new LambdaQueryWrapper<>(getEntity(), getEntityClazz(), expression);
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
}
