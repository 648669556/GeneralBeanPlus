package com.zhiyi.generalbeanplus.enums;

import com.zhiyi.generalbeanplus.ISqlSegment;
import com.zhiyi.generalbeanplus.util.StringPool;
import lombok.AllArgsConstructor;


/**
 * SQL 保留关键字枚举
 *
 * @author chenjunhong
 * @date 2021-7-29
 */
@AllArgsConstructor
public enum SqlKeyword implements ISqlSegment {
    DELETE_AT("deleted_at"),
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    IN("IN"),
    NOT_IN("NOT IN"),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    EQ(StringPool.EQUALS),
    NE("<>"),
    GT(StringPool.RIGHT_CHEV),
    GE(">="),
    LT(StringPool.LEFT_CHEV),
    LE("<="),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL"),
    GROUP_BY("GROUP BY"),
    HAVING("HAVING"),
    ORDER_BY("ORDER BY"),
    EXISTS("EXISTS"),
    NOT_EXISTS("NOT EXISTS"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),
    ASC("ASC"),
    DESC("DESC");

    private final String keyword;

    @Override
    public String getSqlSegment() {
        return this.keyword;
    }
}
