/*
 * Copyright (c) 2011-2021, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhiyi.generalbeanplus.wrapper;

import com.zhiyi.generalbeanplus.metadata.TableInfo;
import com.zhiyi.generalbeanplus.metadata.TableInfoHelper;
import com.zhiyi.generalbeanplus.segments.MergeSegments;
import com.zhiyi.generalbeanplus.util.StringPool;
import com.zhiyi.generalbeanplus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Update 条件封装
 *
 * @author chenjunhong
 * @date 2021-7-29
 */
@SuppressWarnings("serial")
public class UpdateWrapper<T> extends AbstractWrapper<T, String, UpdateWrapper<T>>
        implements StringPool {

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;


    public UpdateWrapper(T entity, List<String> sqlSet, MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = sqlSet;
        this.expression = mergeSegments;
    }

    public UpdateWrapper(Class<T> entityClazz) {
        super.setEntityClazz(entityClazz);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }


    @Override
    public String getTableName() {
        TableInfo tableInfo = TableInfoHelper.getTableInfoByClazz(getEntityClazz());
        return Optional.ofNullable(tableInfo).map(TableInfo::getTableName).orElse(null);
    }

    /**
     * 返回一个支持 lambda 函数写法的 wrapper
     */
    public LambdaUpdateWrapper<T> lambda() {
        return new LambdaUpdateWrapper<>(getEntity(), getEntityClazz(), sqlSet, getExpression());
    }

    @Override
    protected String columnSqlInjectFilter(String column) {
        return StringUtils.replaceBlank(column);
    }

    @Override
    protected UpdateWrapper<T> instance() {
        return new UpdateWrapper<T>(getEntity(), null, new MergeSegments());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}
