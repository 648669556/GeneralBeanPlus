package com.zhiyi.generalbeanplus.segments;


import com.zhiyi.generalbeanplus.ISqlSegment;

import java.util.List;

import static com.zhiyi.generalbeanplus.enums.SqlKeyword.ORDER_BY;
import static java.util.stream.Collectors.joining;

/**
 * Order By SQL 片段
 *
 * @author chenjunhong
 * @since 2021-7-29
 */
@SuppressWarnings("serial")
public class OrderBySegmentList extends AbstractISegmentList {

    @Override
    protected boolean transformList(List<ISqlSegment> list, ISqlSegment firstSegment, ISqlSegment lastSegment) {
        list.remove(0);
        final String sql = list.stream().map(ISqlSegment::getSqlSegment).collect(joining(SPACE));
        list.clear();
        list.add(() -> sql);
        return true;
    }

    @Override
    protected String childrenSqlSegment() {
        if (isEmpty()) {
            return EMPTY;
        }
        return this.stream().map(ISqlSegment::getSqlSegment).collect(joining(COMMA, SPACE + ORDER_BY.getSqlSegment() + SPACE, EMPTY));
    }
}
