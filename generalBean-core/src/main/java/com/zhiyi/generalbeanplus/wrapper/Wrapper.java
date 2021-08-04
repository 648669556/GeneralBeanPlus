package com.zhiyi.generalbeanplus.wrapper;


import com.zhiyi.generalbeanplus.ISqlSegment;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.util.CollectionUtils;

/**
 * 条件构造抽象类
 *
 * @author chenjunhong
 * @since 2021-7-29
 */
@SuppressWarnings("all")
public abstract class Wrapper<T> implements ISqlSegment {

    /**
     * 实体对象（子类实现）
     *
     * @return 泛型 T
     */
    protected abstract T getEntity();

    /**
     * 获取 MergeSegments
     */
    protected abstract MergeSegments getExpression();

    /**
     * 查询条件为空(包含entity)
     */
    protected boolean isEmptyOfWhere() {
        return isEmptyOfNormal() && isEmptyOfEntity();
    }

    /**
     * 查询条件不为空(包含entity)
     */
    protected boolean nonEmptyOfWhere() {
        return !isEmptyOfWhere();
    }

    /**
     * 查询条件为空(不包含entity)
     */
    protected boolean isEmptyOfNormal() {
        return CollectionUtils.isEmpty(getExpression().getNormal());
    }

    /**
     * 查询条件为空(不包含entity)
     */
    protected boolean nonEmptyOfNormal() {
        return !isEmptyOfNormal();
    }

    /**
     * 深层实体判断属性
     *
     * @return true 不为空
     */
    protected boolean nonEmptyOfEntity() {
        T entity = getEntity();
        if (entity == null) {
            return false;
        }
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
        if (tableInfo == null) {
            return false;
        }
        return tableInfo.isNullEntity();
    }


    /**
     * 深层实体判断属性
     *
     * @return true 为空
     */
    protected boolean isEmptyOfEntity() {
        return !nonEmptyOfEntity();
    }

    /**
     * 条件清空
     *
     * @since 3.3.1
     */
    abstract public void clear();
}
