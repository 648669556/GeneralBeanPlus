package com.zhiyi.generalbeanplus.segments;

import com.zhiyi.generalbeanplus.ISqlSegment;
import com.zhiyi.generalbeanplus.enums.SqlKeyword;
import com.zhiyi.generalbeanplus.enums.WrapperKeyword;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;

/**
 * 匹配片段 从plus搬过来的判断方法
 *
 * @author miemie
 * @since 2018-06-27
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum MatchSegment {
    GROUP_BY(i -> i == SqlKeyword.GROUP_BY),
    ORDER_BY(i -> i == SqlKeyword.ORDER_BY),
    NOT(i -> i == SqlKeyword.NOT),
    AND(i -> i == SqlKeyword.AND),
    OR(i -> i == SqlKeyword.OR),
    AND_OR(i -> i == SqlKeyword.AND || i == SqlKeyword.OR),
    EXISTS(i -> i == SqlKeyword.EXISTS),
    HAVING(i -> i == SqlKeyword.HAVING),
    APPLY(i -> i == WrapperKeyword.APPLY);

    private final Predicate<ISqlSegment> predicate;

    public boolean match(ISqlSegment segment) {
        return getPredicate().test(segment);
    }
}
