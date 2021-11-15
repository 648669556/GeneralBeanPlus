package com.zhiyi.generalbeanplus.wrapper;

import com.zhiyi.generalbeanplus.ISqlSegment;
import com.zhiyi.generalbeanplus.enums.SqlKeyword;
import com.zhiyi.generalbeanplus.enums.SqlLike;
import com.zhiyi.generalbeanplus.enums.WrapperKeyword;
import com.zhiyi.generalbeanplus.interfaces.Compare;
import com.zhiyi.generalbeanplus.interfaces.Func;
import com.zhiyi.generalbeanplus.interfaces.Nested;
import com.zhiyi.generalbeanplus.interfaces.SelectColumn;
import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.support.FieldFilter;
import com.zhiyi.generalbeanplus.util.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.zhiyi.generalbeanplus.enums.SqlKeyword.*;
import static com.zhiyi.generalbeanplus.util.StringPool.NULL;
import static com.zhiyi.generalbeanplus.util.StringPool.SINGLE_QUOTE;
import static java.util.stream.Collectors.joining;

/**
 * @param <T>        do类 数据库表映射实体类
 * @param <R>        条件
 * @param <Children> 过滤器本身
 * @author chenjunhong
 * 条件过滤器的 基类
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class AbstractWrapper<T, R, Children extends AbstractWrapper<T, R, Children>> extends Wrapper<T>
        implements Compare<Children, R>, Nested<Children, Children>, Func<Children, R>, SelectColumn {
    /**
     * 占位符
     */
    protected final Children typedThis = (Children) this;

    /**
     * 数据库表映射实体类
     */
    private T entity;
    protected MergeSegments expression;

    private Class<T> entityClazz;

    protected FieldFilter<T> fieldFilter;

    public Class<T> getEntityClazz() {
        if (entityClazz == null && entity != null) {
            entityClazz = (Class<T>) entity.getClass();
        }
        return entityClazz;
    }

    public void setFieldFilter(FieldFilter<T> fieldFilter) {
        this.fieldFilter = fieldFilter;
    }

    public void setEntityClazz(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    @Override
    public String getColumn() {
        return "*";
    }

    @Override
    public Children isNull(boolean condition, R column) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), IS_NULL));
    }

    @Override
    public Children isNotNull(boolean condition, R column) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), IS_NOT_NULL));
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll) {
        return maybeDo(!condition || !checkNull(coll), () -> appendSqlSegments(columnToSqlSegment(column), IN, inExpression(coll)));
    }

    @Override
    public Children in(boolean condition, R column, Object... values) {
        return maybeDo(!condition || !checkNull(values), () -> appendSqlSegments(columnToSqlSegment(column), IN, inExpression(values)));
    }

    @Override
    public Children notIn(boolean condition, R column, Collection<?> coll) {
        return maybeDo(!condition || !checkNull(coll), () -> appendSqlSegments(columnToSqlSegment(column), NOT_IN, inExpression(coll)));
    }

    @Override
    public Children notIn(boolean condition, R column, Object... values) {
        return maybeDo(!condition || !checkNull(values), () -> appendSqlSegments(columnToSqlSegment(column), NOT_IN, inExpression(values)));
    }

    @Override
    public Children inSql(boolean condition, R column, String inValue) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), IN,
                () -> String.format("(%s)", inValue)));
    }

    @Override
    public Children notInSql(boolean condition, R column, String inValue) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), NOT_IN,
                () -> String.format("(%s)", inValue)));
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, R column) {
        return maybeDo(condition, () -> appendSqlSegments(ORDER_BY, columnToSqlSegment(columnSqlInjectFilter(column)),
                isAsc ? ASC : DESC));
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, List<R> columns) {
        return maybeDo(condition, () -> columns.forEach(c -> appendSqlSegments(ORDER_BY,
                columnToSqlSegment(columnSqlInjectFilter(c)), isAsc ? ASC : DESC)));
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        return maybeDo(condition, () -> appendSqlSegments(AND));
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        return or(condition).addNestedCondition(condition, consumer);
    }

    public Children or() {
        return or(true);
    }

    public Children or(boolean condition) {
        return maybeDo(condition, () -> appendSqlSegments(OR));
    }


    @Override
    public Children nested(boolean condition, Consumer<Children> consumer) {
        return addNestedCondition(condition, consumer);
    }

    @Override
    public Children not(boolean condition, Consumer<Children> consumer) {
        return not(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public <V> Children allEq(boolean condition, Map<R, V> params, boolean null2IsNull) {
        if (condition && CollectionUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (StringUtils.checkValNotNull(v)) {
                    eq(k, v);
                } else {
                    if (null2IsNull) {
                        isNull(k);
                    }
                }
            });
        }
        return typedThis;
    }

    @Override
    public <V> Children allEq(boolean condition, BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull) {
        if (condition && CollectionUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (filter.test(k, v)) {
                    if (StringUtils.checkValNotNull(v)) {
                        eq(k, v);
                    } else {
                        if (null2IsNull) {
                            isNull(k);
                        }
                    }
                }
            });
        }
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, R column, Object val) {
        return addCondition(condition, column, EQ, val);
    }

    @Override
    public Children ne(boolean condition, R column, Object val) {
        return addCondition(condition, column, NE, val);
    }

    @Override
    public Children gt(boolean condition, R column, Object val) {
        return addCondition(condition, column, GT, val);
    }

    @Override
    public Children ge(boolean condition, R column, Object val) {
        return addCondition(condition, column, GE, val);
    }

    @Override
    public Children lt(boolean condition, R column, Object val) {
        return addCondition(condition, column, LT, val);
    }

    @Override
    public Children le(boolean condition, R column, Object val) {
        return addCondition(condition, column, LE, val);
    }

    @Override
    public Children between(boolean condition, R column, Object val1, Object val2) {
        return maybeDo(!condition || !checkNull(val1, val2), () -> appendSqlSegments(columnToSqlSegment(column), BETWEEN,
                () -> formatParam(val1), AND, () -> formatParam(val2)));
    }

    @Override
    public Children notBetween(boolean condition, R column, Object val1, Object val2) {
        return maybeDo(!condition || !checkNull(val1, val2), () -> appendSqlSegments(columnToSqlSegment(column), NOT_BETWEEN,
                () -> formatParam(val1), AND, () -> formatParam(val2)));
    }

    @Override
    public Children like(boolean condition, R column, Object val) {
        return likeValue(!condition || !checkNull(val), LIKE, column, val, SqlLike.DEFAULT);
    }

    @Override
    public Children notLike(boolean condition, R column, Object val) {
        return likeValue(!condition || !checkNull(val), NOT_LIKE, column, val, SqlLike.DEFAULT);
    }

    @Override
    public Children likeLeft(boolean condition, R column, Object val) {
        return likeValue(!condition || !checkNull(val), LIKE, column, val, SqlLike.LEFT);
    }

    @Override
    public Children likeRight(boolean condition, R column, Object val) {
        return likeValue(!condition || !checkNull(val), LIKE, column, val, SqlLike.RIGHT);
    }

    @Override
    public String getSqlSegment() {
        return getExpression().getSqlSegment();
    }

    private boolean checkNull(Object... objects) {
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }

    private boolean checkNull(Collection<?> coll) {
        if (CollectionUtils.isEmpty(coll)) {
            return true;
        }
        return coll.stream().anyMatch(this::checkNull);
    }

    @Override
    protected T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    @Override
    protected MergeSegments getExpression() {
        return expression;
    }

    @Override
    public void clear() {
        entity = null;
        expression.clear();
    }

    /**
     * 字段 SQL 注入过滤处理，子类重写实现过滤逻辑
     *
     * @param column 字段内容
     * @return
     */
    protected R columnSqlInjectFilter(R column) {
        return column;
    }

    protected final Children maybeDo(boolean condition, DoSomething something) {
        if (condition) {
            something.doIt();
        }
        return typedThis;
    }

    /**
     * 获取 columnName
     */
    protected final ISqlSegment columnToSqlSegment(R column) {
        return () -> {
            String columnName = columnToString(column);
            String aliasName = getAliasName(columnName);
            if (!aliasName.equals(columnName)) {
                return aliasName;
            } else {
                return StringUtils.camelToUnderline(columnName);
            }
        };
    }

    /**
     * 获取 columnName
     */
    protected String columnToString(R column) {
        return (String) column;
    }

    /**
     * 获取列表别名
     */
    protected String getAliasName(String name) {
        return Optional.ofNullable(TableInfoHelper.getTableInfoByClazz(getEntityClazz())).map(item -> Optional.ofNullable(item.getAlias(name)).orElse(name)).orElse(name);
    }

    /**
     * 获取in表达式 包含括号
     *
     * @param value 集合
     */
    protected ISqlSegment inExpression(Collection<?> value) {
        if (CollectionUtils.isEmpty(value)) {
            return () -> "()";
        }
        return () -> value.stream().map(this::formatParam)
                .collect(joining(StringPool.COMMA, StringPool.LEFT_BRACKET, StringPool.RIGHT_BRACKET));
    }

    /**
     * 内部自用
     * <p>NOT 关键词</p>
     */
    protected Children not(boolean condition) {
        return maybeDo(condition, () -> appendSqlSegments(NOT));
    }

    /**
     * 内部自用
     * <p>拼接 AND</p>
     */
    protected Children and(boolean condition) {
        return maybeDo(condition, () -> appendSqlSegments(AND));
    }

    /**
     * 处理入参
     *
     * @param param 参数
     * @return value
     */
    protected final String formatParam(Object param) {
        if (param == null) {
            return NULL;
        } else if (param instanceof String) {
            return SINGLE_QUOTE + StringUtils.replaceSingle(param.toString()) + SINGLE_QUOTE;
        } else if (param instanceof Integer || param instanceof Long || param instanceof Double || param instanceof BigDecimal) {
            return param.toString();
        } else if (param instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatParam(simpleDateFormat.format(param));
        }
        return param.toString();
    }

    /**
     * 获取in表达式 包含括号
     *
     * @param values 数组
     */
    protected ISqlSegment inExpression(Object[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return () -> "()";
        }
        return () -> Arrays.stream(values).map(this::formatParam)
                .collect(joining(StringPool.COMMA, StringPool.LEFT_BRACKET, StringPool.RIGHT_BRACKET));
    }

    /**
     * 多重嵌套查询条件
     *
     * @param condition 查询条件值
     */
    protected Children addNestedCondition(boolean condition, Consumer<Children> consumer) {
        return maybeDo(condition, () -> {
            final Children instance = instance();
            consumer.accept(instance);
            appendSqlSegments(WrapperKeyword.APPLY, instance);
        });
    }

    /**
     * 普通查询条件
     *
     * @param condition  是否执行
     * @param column     属性
     * @param sqlKeyword SQL 关键词
     * @param val        条件值
     */
    protected Children addCondition(boolean condition, R column, SqlKeyword sqlKeyword, Object val) {
        return maybeDo(!condition || !checkNull(val), () -> appendSqlSegments(columnToSqlSegment(column), sqlKeyword,
                () -> formatParam(val)));
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     */
    protected Children likeValue(boolean condition, SqlKeyword keyword, R column, Object val, SqlLike sqlLike) {
        return maybeDo(!condition || !checkNull(val), () -> appendSqlSegments(columnToSqlSegment(column), keyword,
                () -> formatParam(SqlUtils.concatLike(val, sqlLike))));
    }


    /**
     * 子类返回一个自己的新对象
     */
    protected abstract Children instance();

    /**
     * 添加 where 片段
     *
     * @param sqlSegments ISqlSegment 数组
     */
    protected void appendSqlSegments(ISqlSegment... sqlSegments) {
        expression.add(sqlSegments);
    }

    /**
     * 必要的初始化
     */
    protected void initNeed() {
        expression = new MergeSegments();
    }

    @Override
    public String toString() {
        return getSqlSegment();
    }

    public abstract String getTableName();


    /**
     * 做事函数
     */
    @FunctionalInterface
    public interface DoSomething {
        void doIt();
    }
}
