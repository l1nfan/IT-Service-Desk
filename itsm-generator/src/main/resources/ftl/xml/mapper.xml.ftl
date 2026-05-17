<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${ClassName}Mapper">

    <resultMap type="${ClassName}" id="${ClassName}Result">
<#list columns as column>
        <result property="${column.javaField}"    column="${column.columnName}"    />
</#list>
    </resultMap>
<#if table.sub>

    <resultMap id="${ClassName}${subClassName}Result" type="${ClassName}" extends="${ClassName}Result">
        <collection property="${subclassName}List" ofType="${subClassName}" column="${pkColumn.columnName}" select="select${subClassName}List" />
    </resultMap>

    <resultMap type="${subClassName}" id="${subClassName}Result">
<#list subTable.columns as column>
        <result property="${column.javaField}"    column="${column.columnName}"    />
</#list>
    </resultMap>
</#if>

    <sql id="select${ClassName}Vo">
        select<#list columns as column> ${column.columnName}<#if column_has_next>,</#if></#list> from ${tableName}
    </sql>

    <select id="select${ClassName}List" parameterType="${ClassName}" resultMap="${ClassName}Result">
        <include refid="select${ClassName}Vo"/>
        <where>
<#list columns as column>
<#assign queryType=column.queryType>
<#assign javaField=column.javaField>
<#assign javaType=column.javaType>
<#assign columnName=column.columnName>
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
<#if column.query>
<#if column.queryType == "EQ">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} = #{${javaField}}</if>
<#elseif queryType == "NE">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} != #{${javaField}}</if>
<#elseif queryType == "GT">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} &gt; #{${javaField}}</if>
<#elseif queryType == "GTE">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} &gt;= #{${javaField}}</if>
<#elseif queryType == "LT">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} &lt; #{${javaField}}</if>
<#elseif queryType == "LTE">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} &lt;= #{${javaField}}</if>
<#elseif queryType == "LIKE">
            <if test="${javaField} != null <#if javaType == 'String' > and ${javaField}.trim() != ''</#if>"> and ${columnName} like concat('%', #{${javaField}}, '%')</if>
<#elseif queryType == "BETWEEN">
            <if test="params.begin${AttrName} != null and params.begin${AttrName} != '' and params.end${AttrName} != null and params.end${AttrName} != ''"> and ${columnName} between #{params.begin${AttrName}} and #{params.end${AttrName}}</if>
</#if>
</#if>
</#list>
        </where>
    </select>

    <select id="select${ClassName}By${pkColumn.capJavaField}" parameterType="${pkColumn.javaType}" resultMap="<#if table.sub>${ClassName}${subClassName}Result<#else>${ClassName}Result</#if>">
<#if table.crud || table.tree>
        <include refid="select${ClassName}Vo"/>
        where ${pkColumn.columnName} = #{${pkColumn.javaField}}
<#elseif table.sub>
        select<#list columns as column> ${column.columnName}<#if column_has_next>,</#if></#list>
        from ${tableName}
        where ${pkColumn.columnName} = #{${pkColumn.javaField}}
</#if>
    </select>
<#if table.sub>

    <select id="select${subClassName}List" resultMap="${subClassName}Result">
        select<#list subTable.columns as column> ${column.columnName}<#if column_has_next>,</#if></#list>
        from ${subTableName}
        where ${subTableFkName} = #{${subTableFkName}}
    </select>
</#if>

    <insert id="insert${ClassName}" parameterType="${ClassName}"<#if pkColumn.increment> useGeneratedKeys="true" keyProperty="${pkColumn.javaField}"</#if>>
        insert into ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
<#list columns as column>
<#if column.columnName != pkColumn.columnName || !pkColumn.increment>
            <if test="${column.javaField} != null<#if column.javaType == 'String' && column.required> and ${column.javaField} != ''</#if>">${column.columnName},</if>
</#if>
</#list>
         </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
<#list columns as column>
<#if column.columnName != pkColumn.columnName || !pkColumn.increment>
            <if test="${column.javaField} != null<#if column.javaType == 'String' && column.required> and ${column.javaField} != ''</#if>">#{${column.javaField}},</if>
</#if>
</#list>
         </trim>
    </insert>

    <update id="update${ClassName}" parameterType="${ClassName}">
        update ${tableName}
        <trim prefix="SET" suffixOverrides=",">
<#list columns as column>
<#if column.columnName != pkColumn.columnName>
            <if test="${column.javaField} != null<#if column.javaType == 'String' && column.required> and ${column.javaField} != ''</#if>">${column.columnName} = #{${column.javaField}},</if>
</#if>
</#list>
        </trim>
        where ${pkColumn.columnName} = #{${pkColumn.javaField}}
    </update>

    <delete id="delete${ClassName}By${pkColumn.capJavaField}" parameterType="${pkColumn.javaType}">
        delete from ${tableName} where ${pkColumn.columnName} = #{${pkColumn.javaField}}
    </delete>

    <delete id="delete${ClassName}By${pkColumn.capJavaField}s" parameterType="String">
        delete from ${tableName} where ${pkColumn.columnName} in
        <foreach item="${pkColumn.javaField}" collection="array" open="(" separator="," close=")">
            #{${pkColumn.javaField}}
        </foreach>
    </delete>
<#if table.sub>

    <delete id="delete${subClassName}By${subTableFkClassName}s" parameterType="String">
        delete from ${subTableName} where ${subTableFkName} in
        <foreach item="${subTableFkclassName}" collection="array" open="(" separator="," close=")">
            #{${subTableFkclassName}}
        </foreach>
    </delete>

    <delete id="delete${subClassName}By${subTableFkClassName}" parameterType="${pkColumn.javaType}">
        delete from ${subTableName} where ${subTableFkName} = #{${subTableFkclassName}}
    </delete>

    <insert id="batch${subClassName}">
        insert into ${subTableName}(<#list subTable.columns as column> ${column.columnName}<#if column_has_next>,</#if></#list>) values
        <foreach item="item" index="index" collection="list" separator=",">
            (<#list subTable.columns as column>#{item.${column.javaField}}<#if column_has_next>,</#if></#list>)
        </foreach>
    </insert>
</#if>
</mapper>
