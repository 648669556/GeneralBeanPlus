package com.zhiyi.test;

import com.zhiyi.generalbeanplus.annotation.TargetColumnName;
import com.zhiyi.generalbeanplus.annotation.TargetTableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TargetTableName("tb_test")
public class User {
    @TargetColumnName("co_user_id")
    Integer userId;
    @TargetColumnName("co_user_name")
    String userName;

    BigDecimal high;

}
