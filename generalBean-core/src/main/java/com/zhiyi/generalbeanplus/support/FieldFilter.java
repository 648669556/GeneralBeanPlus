package com.zhiyi.generalbeanplus.support;

import com.zhiyi.generalbeanplus.util.LambdaUtils;
import com.zhiyi.generalbeanplus.util.SFunction;

import java.util.*;
import java.util.function.Predicate;

public class FieldFilter<T> {
    Set<String> sFunctionSet;
    Predicate<String> predicate;

    public FieldFilter() {
        sFunctionSet = new HashSet<>();
        predicate = (e) -> sFunctionSet.contains(e);
    }

    public Set<String> getsFunctionSet() {
        return sFunctionSet;
    }

    public FieldFilter<T> filter(SFunction<T, ?> sFunction) {
        sFunctionSet.add(LambdaUtils.convertToFieldName(sFunction));
        return this;
    }

    public FieldFilter<T> condition(Predicate<String> predicate) {
        this.predicate = predicate;
        return this;
    }

    public boolean test(String name) {
        return predicate.test(name);
    }
}
