package com.zhiyi.generalbeanplus;


import java.io.Serializable;

/**
 * SQL 片段接口
 * @author chenjunhong
 * @date 2021-7-29
 */
@FunctionalInterface
public interface ISqlSegment extends Serializable {

    /**
     * SQL 片段
     */
    String getSqlSegment();
}
