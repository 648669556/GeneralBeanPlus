package com.zhiyi.generalbeanplus.wrapper;

import com.zhiyi.generalbeanplus.util.LambdaUtils;
import com.zhiyi.generalbeanplus.util.SFunction;
import com.zhiyi.generalbeanplus.util.StringPool;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * Lambda 语法使用 Wrapper
 * <p>统一处理解析 lambda 获取 column</p>
 *
 * @author chenjunhong
 * @date 2021-07-30
 */
@SuppressWarnings("serial")
public abstract class AbstractLambdaWrapper<T, Children extends AbstractLambdaWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {

    @SuppressWarnings("unchecked")
    protected String columnsToString(boolean onlyColumn, SFunction<T, ?>... columns) {
        return Arrays.stream(columns).map(i -> columnToString(i, onlyColumn)).collect(joining(StringPool.COMMA));
    }

    @Override
    protected String columnToString(SFunction<T, ?> column) {
        return columnToString(column, true);
    }

    protected String columnToString(SFunction<T, ?> column, boolean onlyColumn) {
        return LambdaUtils.convertToFieldName(column);
    }
}
