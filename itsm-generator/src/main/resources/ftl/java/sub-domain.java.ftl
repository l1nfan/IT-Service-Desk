package ${packageName}.domain;

<#list subImportList as imp>
import ${imp};
</#list>
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.itsm.common.annotation.Excel;
import com.itsm.common.core.domain.BaseEntity;

/**
 * ${subTable.functionName}对象 ${subTableName}
 *
 * @author ${author}
 * @date ${datetime}
 */
public class ${subClassName} extends BaseEntity
{
    private static final long serialVersionUID = 1L;

<#list subTable.columns as column>
<#if !table.isSuperColumn(column.javaField)>
    /** ${column.columnComment} -->
<#if column.list>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if parentheseIndex != -1>
    @Excel(name = "${comment}", readConverterExp = "${column.readConverterExp()}")
<#elseif column.javaType == 'Date'>
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "${comment}", width = 30, dateFormat = "yyyy-MM-dd")
<#else>
    @Excel(name = "${comment}")
</#if>
</#if>
    private ${column.javaType} ${column.javaField};

</#if>
</#list>
<#list subTable.columns as column>
<#if !table.isSuperColumn(column.javaField)>
<#if column.javaField.length() > 2 && column.javaField.substring(1,2)?matches("[A-Z]")>
<#assign AttrName=column.javaField>
<#else>
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
</#if>
    public void set${AttrName}(${column.javaType} ${column.javaField})
    {
        this.${column.javaField} = ${column.javaField};
    }

    public ${column.javaType} get${AttrName}()
    {
        return ${column.javaField};
    }
</#if>
</#list>

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
<#list subTable.columns as column>
<#if column.javaField.length() > 2 && column.javaField.substring(1,2)?matches("[A-Z]")>
<#assign AttrName=column.javaField>
<#else>
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
</#if>
            .append("${column.javaField}", get${AttrName}())
</#list>
            .toString();
    }
}
