import type { PageDomain, BaseEntity } from "../common";

/** ${functionName}配置分页查询参数 */
export interface ${BusinessName}QueryParams extends PageDomain {
<#list columns as column>
<#if column.query>
<#assign type = "string">
<#if column.javaType == "Long" || column.javaType == "Integer">
  <#assign type = "number">
<#elseif column.javaType == "Boolean">
  <#assign type = "boolean">
</#if>
  /** ${column.columnComment} */
  ${column.javaField}?: ${type};
</#if>
</#list>
}

/** ${functionName}配置信息 */
export interface ${ClassName} extends BaseEntity {
<#list columns as column>
<#assign type = "string">
<#if column.javaType == "Long" || column.javaType == "Integer">
  <#assign type = "number">
<#elseif column.javaType == "Boolean">
  <#assign type = "boolean">
</#if>
  /** ${column.columnComment} */
  ${column.javaField}?: ${type};
</#list>
<#if table.sub>
  /** ${table.subTable.functionName}信息 */
  ${subclassName}List?: ${subClassName}[];
</#if>
}
<#if table.sub>

/** ${subTable.functionName}配置信息 */
export interface ${subClassName} extends BaseEntity {
<#list subTable.columns as column>
<#assign type = "string">
<#if column.javaType == "Long" || column.javaType == "Integer">
  <#assign type = "number">
<#elseif column.javaType == "Boolean">
  <#assign type = "boolean">
</#if>
  /** ${column.columnComment} */
  ${column.javaField}?: ${type};
</#list>
}
</#if>
