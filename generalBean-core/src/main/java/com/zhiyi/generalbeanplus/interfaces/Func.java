package com.zhiyi.generalbeanplus.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 查询条件封装
 *
 */
@SuppressWarnings("unchecked")
public interface Func<Children, R> extends Serializable {

    /**
     * ignore
     */
    default Children isNull(R column) {
        return isNull(true, column);
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNull(boolean condition, R column);

    /**
     * ignore
     */
    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNotNull(boolean condition, R column);

    /**
     * ignore
     */
    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！集合为空若存在逻辑错误，请在 condition 条件中判断 </li>
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children in(boolean condition, R column, Collection<?> coll);

    /**
     * ignore
     */
    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in("id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 注意！数组为空若存在逻辑错误，请在 condition 条件中判断 </li>
     * <li> 如果动态数组为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    Children in(boolean condition, R column, Object... values);

    /**
     * ignore
     */
    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children notIn(boolean condition, R column, Collection<?> coll);

    /**
     * ignore
     */
    default Children notIn(R column, Object... value) {
        return notIn(true, column, value);
    }

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    Children notIn(boolean condition, R column, Object... values);

    /**
     * ignore
     */
    default Children inSql(R column, String inValue) {
        return inSql(true, column, inValue);
    }

    /**
     * 字段 IN ( sql语句 )
     * <p>!! sql 注入方式的 in 方法 !!</p>
     * <p>例1: inSql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: inSql("id", "select id from table where id &lt; 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   sql语句
     * @return children
     */
    Children inSql(boolean condition, R column, String inValue);

    /**
     * ignore
     */
    default Children notInSql(R column, String inValue) {
        return notInSql(true, column, inValue);
    }

    /**
     * 字段 NOT IN ( sql语句 )
     * <p>!! sql 注入方式的 not in 方法 !!</p>
     * <p>例1: notInSql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: notInSql("id", "select id from table where id &lt; 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   sql语句 ---&gt; 1,2,3,4,5,6 或者 select id from table where id &lt; 3
     * @return children
     */
    Children notInSql(boolean condition, R column, String inValue);



    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc(true, "id")</p>
     *
     * @param condition 执行条件
     * @param column    单个字段
     * @return children
     */
    default Children orderByAsc(boolean condition, R column) {
        return orderBy(condition, true, column);
    }

    default Children orderByAsc(R column) {
        return orderByAsc(true, column);
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc(true, Arrays.asList("id", "name"))</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    default Children orderByAsc(boolean condition, List<R> columns) {
        return orderBy(condition, true, columns);
    }

    default Children orderByAsc(List<R> columns) {
        return orderByAsc(true, columns);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc(true, "id")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    default Children orderByDesc(boolean condition, R column) {
        return orderBy(condition, false, column);
    }

    default Children orderByDesc(R column) {
        return orderByDesc(true, column);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc(true, Arrays.asList("id", "name"))</p>
     *
     * @param condition 执行条件
     * @param columns   字段列表
     * @return children
     */
    default Children orderByDesc(boolean condition, List<R> columns) {
        return orderBy(condition, false, columns);
    }

    default Children orderByDesc(List<R> columns) {
        return orderByDesc(true, columns);
    }


    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, "id")</p>
     *
     * @param condition 执行条件
     * @param isAsc     是否是 ASC 排序
     * @param column    单个字段
     * @return children
     */
    Children orderBy(boolean condition, boolean isAsc, R column);

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, Arrays.asList("id", "name"))</p>
     *
     * @param condition 执行条件
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段列表
     * @return children
     */
    Children orderBy(boolean condition, boolean isAsc, List<R> columns);
}
