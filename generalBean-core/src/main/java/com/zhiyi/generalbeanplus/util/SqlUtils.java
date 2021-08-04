package com.zhiyi.generalbeanplus.util;

import com.zhiyi.generalbeanplus.enums.SqlLike;

public class SqlUtils implements StringPool{
    /**
     * 用%连接like
     *
     * @param str 原字符串
     * @return like 的值
     */
    public static String concatLike(Object str, SqlLike type) {
        switch (type) {
            case LEFT:
                return PERCENT + str;
            case RIGHT:
                return str + PERCENT;
            default:
                return PERCENT + str + PERCENT;
        }
    }
}
