package com.zhiyi.generalbeanplus.segments;

import com.zhiyi.generalbeanplus.ISqlSegment;
import com.zhiyi.generalbeanplus.enums.SqlKeyword;
import com.zhiyi.generalbeanplus.util.StringPool;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 合并 SQL 片段
 *
 * @author miemie
 * @since 2018-06-27
 */
@Getter
@SuppressWarnings("serial")
public class MergeSegments implements ISqlSegment {

    private final NormalSegmentList normal = new NormalSegmentList();
    private final OrderBySegmentList orderBy = new OrderBySegmentList();

    @Getter(AccessLevel.NONE)
    private String sqlSegment = StringPool.EMPTY;
    @Getter(AccessLevel.NONE)
    private boolean cacheSqlSegment = true;

    public void add(ISqlSegment... iSqlSegments) {
        List<ISqlSegment> list = Arrays.asList(iSqlSegments);
        ISqlSegment firstSqlSegment = list.get(0);
        if (MatchSegment.ORDER_BY.match(firstSqlSegment)) {
            orderBy.addAll(list);
        } else {
            normal.addAll(list);
        }
        cacheSqlSegment = false;
    }

    @Override
    public String getSqlSegment() {
        return getSqlSegment(false);
    }

    public String getSqlSegment(boolean haveDeleted) {
        if (!haveDeleted) {
            add(SqlKeyword.DELETE_AT, SqlKeyword.IS_NULL);
        }
        if (cacheSqlSegment) {
            return sqlSegment;
        }
        cacheSqlSegment = true;
        if (normal.isEmpty()) {
            if (!orderBy.isEmpty()) {
                sqlSegment = orderBy.getSqlSegment();
            }
        } else {
            sqlSegment = normal.getSqlSegment() + orderBy.getSqlSegment();
        }
        return sqlSegment;
    }

    /**
     * 清理
     *
     * @since 3.3.1
     */
    public void clear() {
        sqlSegment = StringPool.EMPTY;
        cacheSqlSegment = true;
        normal.clear();
        orderBy.clear();
    }
}
