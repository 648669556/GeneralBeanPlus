package com.zhiyi.test;

import com.zhiyi.generalbeanplus.support.FieldFilter;
import com.zhiyi.generalbeanplus.support.ObjGenerator;
import com.zhiyi.generalbeanplus.wrapper.LambdaUpdateWrapper;
import com.zhiyi.generalbeanplus.wrapper.QueryWrapper;
import com.zhiyi.generalbeanplus.wrapper.UpdateWrapper;
import com.zhiyi.generalbeanplus.util.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class UpdateWrapperTest {

    @Test
    public void update() {
        LambdaUpdateWrapper<User> wrapper = new UpdateWrapper<>(User.class).lambda();
        System.out.println(wrapper.getSqlSegment());
        System.out.println(wrapper.getTableName());
    }

    @Test
    public void query() {
        QueryWrapper<User> lambda = new QueryWrapper<>(User.class)
                .eq("a", null);
        System.out.println(StringUtils.isBlank(lambda.getSqlSegment()));
    }

    @Test
    public void test() {
        ObjGenerator<User> userObjGenerator = new ObjGenerator<>(User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", 1);
        map.put("user_name", "username");
        map.put("high", BigDecimal.ONE);
        User user = userObjGenerator.generatorObj(map);
        System.out.println(user);
    }

    @Test
    public void fieldFilter() {
        FieldFilter<User> userFieldFilter = new FieldFilter<>();
        userFieldFilter.filter(User::getUserId)
                .condition((item) -> userFieldFilter.getsFunctionSet().contains(item))
                .filter(User::getHigh)
                .filter(User::getUserName);
        System.out.println(userFieldFilter.test("userId"));
    }
}
