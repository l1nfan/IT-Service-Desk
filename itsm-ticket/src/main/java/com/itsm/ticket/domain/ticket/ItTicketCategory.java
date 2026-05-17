package com.itsm.ticket.domain.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.itsm.common.annotation.Excel;
import com.itsm.common.core.domain.BaseEntity;

/**
 * 工单分类表 it_ticket_category
 *
 * @author itsm
 */
public class ItTicketCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "分类ID", cellType = Excel.ColumnType.NUMERIC)
    private Long categoryId;

    @Excel(name = "分类名称")
    private String categoryName;

    private Long parentId;

    private String ancestors;

    @Excel(name = "排序号")
    private Integer orderNum;

    private Long workflowId;

    private Long slaConfigId;

    private String icon;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    /** 父分类名称（非数据库字段） */
    private String parentName;

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 0, max = 100, message = "分类名称不能超过100个字符")
    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public Long getSlaConfigId()
    {
        return slaConfigId;
    }

    public void setSlaConfigId(Long slaConfigId)
    {
        this.slaConfigId = slaConfigId;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("categoryId", getCategoryId())
            .append("categoryName", getCategoryName())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
