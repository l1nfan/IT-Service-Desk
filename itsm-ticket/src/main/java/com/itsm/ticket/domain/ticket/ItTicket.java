package com.itsm.ticket.domain.ticket;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.itsm.common.annotation.Excel;
import com.itsm.common.core.domain.BaseEntity;

/**
 * 工单主表 it_ticket
 *
 * @author itsm
 */
public class ItTicket extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "工单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long ticketId;

    @Excel(name = "工单编号")
    private String ticketNo;

    @Excel(name = "工单标题")
    @NotBlank(message = "工单标题不能为空")
    @Size(min = 1, max = 200, message = "工单标题长度必须在1到200个字符之间")
    private String title;

    private String description;

    @Excel(name = "分类ID")
    private Long categoryId;

    private Long subCategoryId;

    @Excel(name = "优先级", readConverterExp = "1=紧急,2=高,3=中,4=低")
    private Integer priority;

    @Excel(name = "状态")
    private String status;

    private String impactScope;

    @Excel(name = "来源", readConverterExp = "WEB=Web端,EMAIL=邮件,PHONE=电话,API=API")
    private String source;

    private Long workflowId;

    private Long workflowInstanceId;

    private String currentNode;

    private Long creatorId;

    private Long creatorDeptId;

    private Long assigneeId;

    private Long assigneeDeptId;

    private Long approverId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedResolveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualResolveTime;

    private Long resolvedBy;

    private Long closedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    private BigDecimal totalHours;

    private String delFlag;

    private String oldStatus;

    /** 非数据库字段 - 分类名称 */
    private String categoryName;

    /** 非数据库字段 - 创建人姓名 */
    private String creatorName;

    /** 非数据库字段 - 处理人姓名 */
    private String assigneeName;

    public Long getTicketId()
    {
        return ticketId;
    }

    public void setTicketId(Long ticketId)
    {
        this.ticketId = ticketId;
    }

    public String getTicketNo()
    {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo)
    {
        this.ticketNo = ticketNo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public Long getSubCategoryId()
    {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId)
    {
        this.subCategoryId = subCategoryId;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getImpactScope()
    {
        return impactScope;
    }

    public void setImpactScope(String impactScope)
    {
        this.impactScope = impactScope;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public Long getWorkflowInstanceId()
    {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId)
    {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(String currentNode)
    {
        this.currentNode = currentNode;
    }

    public Long getCreatorId()
    {
        return creatorId;
    }

    public void setCreatorId(Long creatorId)
    {
        this.creatorId = creatorId;
    }

    public Long getCreatorDeptId()
    {
        return creatorDeptId;
    }

    public void setCreatorDeptId(Long creatorDeptId)
    {
        this.creatorDeptId = creatorDeptId;
    }

    public Long getAssigneeId()
    {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId)
    {
        this.assigneeId = assigneeId;
    }

    public Long getAssigneeDeptId()
    {
        return assigneeDeptId;
    }

    public void setAssigneeDeptId(Long assigneeDeptId)
    {
        this.assigneeDeptId = assigneeDeptId;
    }

    public Long getApproverId()
    {
        return approverId;
    }

    public void setApproverId(Long approverId)
    {
        this.approverId = approverId;
    }

    public Date getExpectedResolveTime()
    {
        return expectedResolveTime;
    }

    public void setExpectedResolveTime(Date expectedResolveTime)
    {
        this.expectedResolveTime = expectedResolveTime;
    }

    public Date getActualResolveTime()
    {
        return actualResolveTime;
    }

    public void setActualResolveTime(Date actualResolveTime)
    {
        this.actualResolveTime = actualResolveTime;
    }

    public Long getResolvedBy()
    {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy)
    {
        this.resolvedBy = resolvedBy;
    }

    public Long getClosedBy()
    {
        return closedBy;
    }

    public void setClosedBy(Long closedBy)
    {
        this.closedBy = closedBy;
    }

    public Date getCloseTime()
    {
        return closeTime;
    }

    public void setCloseTime(Date closeTime)
    {
        this.closeTime = closeTime;
    }

    public BigDecimal getTotalHours()
    {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours)
    {
        this.totalHours = totalHours;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getOldStatus()
    {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus)
    {
        this.oldStatus = oldStatus;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getCreatorName()
    {
        return creatorName;
    }

    public void setCreatorName(String creatorName)
    {
        this.creatorName = creatorName;
    }

    public String getAssigneeName()
    {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName)
    {
        this.assigneeName = assigneeName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("ticketId", getTicketId())
            .append("ticketNo", getTicketNo())
            .append("title", getTitle())
            .append("categoryId", getCategoryId())
            .append("priority", getPriority())
            .append("status", getStatus())
            .append("source", getSource())
            .append("creatorId", getCreatorId())
            .append("assigneeId", getAssigneeId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
