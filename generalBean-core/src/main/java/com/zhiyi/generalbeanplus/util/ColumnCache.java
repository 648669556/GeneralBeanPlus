package com.zhiyi.generalbeanplus.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author miemie
 * @since 2018-12-30
 */
@Data
@AllArgsConstructor
public class ColumnCache implements Serializable {

    private static final long serialVersionUID = -4586291538088403456L;

    /**
     * 使用 column
     */
    private String column;
    /**
     * 查询 column
     */
    private String columnSelect;
    /**
     * mapping
     */
    private String mapping;

    public ColumnCache(String column, String columnSelect) {
        this.column = column;
        this.columnSelect = columnSelect;
    }
}
