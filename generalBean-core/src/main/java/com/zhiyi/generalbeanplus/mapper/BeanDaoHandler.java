package com.zhiyi.generalbeanplus.mapper;

import com.zhiyi.generalbeanplus.model.PageSet;
import com.zhiyi.generalbeanplus.wrapper.AbstractWrapper;
import lombok.NonNull;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流数据库操作
 * 注意：一个数据源要指定一个Handler对象
 *
 * @author weixiaofeng chenjunhong
 */
@Mapper
public interface BeanDaoHandler {
    /**
     * 通用的插入
     *
     * @param map 参数键值对
     */
    @Insert({"<script>",
            "        insert into `${tableName}`",
            "<foreach collection='properties' item='item' open='(' separator=',' close=')'>",
            "            `${item.name}`",
            "</foreach>",
            "VALUES",
            "<foreach collection='properties' item='item' open='(' separator=',' close=')'>",
            "            #{item.value}",
            "</foreach>",
            "</script>"})
    @SelectKey(keyProperty = "id", before = false, resultType = Integer.class, statement = "select last_insert_id()")
    int add(Map<String, Object> map);

    /**
     * 通用的批量插入
     *
     * @param map 参数键值对
     */
    @Insert({"<script>",
            "        insert into `${tableName}`",
            "<foreach collection='properties' item='item' open='(' separator=',' close=')'>",
            "            `${item.name}`",
            "</foreach>",
            "VALUES",
            "<foreach collection='propertiesValueBatch' index='index' item='item' separator=','> ",
            "       <foreach collection='item' item='values' separator=',' open='(' close= ')'> ",
            "             #{values}",
            "       </foreach>",
            "</foreach>",
            "</script>"})
    int addList(Map<String, Object> map);

    /**
     * 通用的更新
     *
     * @param map 参数键值对
     */
    @Update({"<script>",
            " update `${tableName}`",
            "        set",
            "        <foreach collection='properties' item='item' separator=','>",
            "            `${item.name}`=#{item.value}",
            "        </foreach>",
            "        where `${idName}`=#{idValue}"
            , "</script>"})
    int update(Map<String, Object> map);

    /**
     * 根据条件选择器来更新
     *
     * @param map
     */
    @Update({"<script>",
            "UPDATE `${wrapper.getTableName()}`",
            "        <set>",
            "            <foreach collection='properties' item='item' separator=','>",
            "                `${item.name}` = #{item.value}",
            "            </foreach>",
            "        </set>",
            "        <where>",
            "               ${wrapper.getSqlSegment()}",
            "        </where>",
            "</script>"})
    int updateByWrapper(Map<String, Object> map);

    /**
     * 批量选择性更新
     */
    @Update({"<script>",
            " UPDATE `${tableName}`",
            "        <set>",
            "            <foreach collection='propertyList' item='item'>",
            "                `${item.name}` =",
            "                <trim prefix='case' suffix='end,'>",
            "                    <foreach collection='item.dataList' item='data'>",
            "                        WHEN `${idName}` = #{data.id} THEN #{data.value}",
            "                    </foreach>",
            "                </trim>",
            "            </foreach>",
            "        </set>",
            "        <where>",
            "            `${idName}` in",
            "            <foreach collection='idList' item='item' open='(' close=')' separator=','>",
            "                #{item}",
            "            </foreach>",
            "        </where>"
            , "</script>"})
    int batchUpdate(Map<String, Object> map);

    /**
     * 通用的删除
     *
     * @param map 参数键值对
     */
    @Delete({"<script>",
            " update `${tableName}`",
            "        set deleted_at = now()",
            "        WHERE `${idName}` = #{idValue}",
            "</script>"
    })
    int delete(Map<String, Object> map);

    /**
     * 通用的查询
     *
     * @param map 参数键值对
     * @return 结果集（map形式）
     */
    @NonNull
    @Select({
            "<script>",
            " select * from `${tableName}`",
            "        where 1 = 1",
            "        <if test='properties != null'>",
            "            <foreach collection='properties' item='item' separator=' '>",
            "                and `${item.name}`=#{item.value}",
            "            </foreach>",
            "        </if>",
            "        <if test='likeProperties != null'>",
            "            <foreach collection='likeProperties' item='item' separator=' '>",
            "                and `${item.name}` like CONCAT('%', #{item.value}, '%')",
            "            </foreach>",
            "        </if>",
            "        <if test='userPara != null'>",
            "            <foreach collection='userPara' item='item' separator=' '>",
            "                ${item}",
            "            </foreach>",
            "        </if>",
            "        <if test='limit != null and start != null and limit > 0 '>",
            "            limit #{start},#{limit}",
            "        </if>",
            "</script>"
    })
    List<Map<String, Object>> query(Map<String, Object> map);

    /**
     * 根据指定列的多个值查询
     *
     * @param table  表名
     * @param prop   属性名
     * @param values 值集合
     * @return 查询结果的map list
     */
    @NonNull
    @Select({
            "<script>",
            " SELECT",
            "        *",
            "        FROM `${table}`",
            "        WHERE `${prop}` IN",
            "        <foreach collection='values' item='value' open='(' close=')' separator=','>",
            "            #{value}",
            "        </foreach>",
            "</script>"
    })
    List<Map<String, Object>> queryByMultiValue(@Param("table") String table, @Param("prop") String prop, @Param("values") List values);

    /**
     * 获取结果的总数
     *
     * @param map
     * @return
     */
    @Select({
            "<script>",
            "  select count(*) from `${tableName}`",
            "        where 1 = 1",
            "        <if test='properties != null'>",
            "            <foreach collection='properties' item='item' separator=''>",
            "                and `${item.name}`=#{item.value}",
            "            </foreach>",
            "        </if>",
            "        <if test='likeProperties != null'>",
            "            <foreach collection='likeProperties' item='item' separator=' '>",
            "                and `${item.name}` like CONCAT('%', #{item.value}, '%')",
            "            </foreach>",
            "        </if>",
            "        <!--自定义参数-->",
            "        <if test='userPara != null'>",
            "            <foreach collection='userPara' item='item' separator=' '>",
            "                ${item}",
            "            </foreach>",
            "        </if>",
            "</script>"
    })
    int queryCount(Map<String, Object> map);

    @Select({
            "<script>",
            "select * from `${wrapper.getTableName()}`",
            "   <where>",
            "       1 = 1",
            "       AND ${wrapper.getSqlSegment()}",
            "   </where>",
            "    <if test='pageSet != null'>",
            "       <if test='pageSet.start != null and pageSet.pageSize != null'>",
            "                LIMIT #{pageSet.start}, #{pageSet.pageSize}",
            "       </if>",
            "    </if>",
            "</script>"
    })
    List<Map<String, Object>> queryList(@Param("wrapper") AbstractWrapper<?, ?, ?> wrapper, @Param("pageSet") PageSet<?> pageSet);

    @Select({
            "<script>",
            "select count(*) from `${wrapper.getTableName()}`",
            "   <where>",
            "       1 = 1",
            "       AND ${wrapper.getSqlSegment()}",
            "   </where>",
            "</script>",
    })
    int count(@Param("wrapper") AbstractWrapper<?, ?, ?> wrapper);
}
