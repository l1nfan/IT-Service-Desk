package com.itsm.ticket.domain.ticket;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.itsm.common.annotation.Excel;
import com.itsm.common.core.domain.BaseEntity;

public class ItSlaConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "SLA ID", cellType = Excel.ColumnType.NUMERIC)
    private Long slaId;

    @Excel(name = "SLA名称")
    private String slaName;

    private Long categoryId;

    @Excel(name = "优先级")
    private String priority;

    @Excel(name = "响应时限(分钟)")
    private Integer responseTime;

    @Excel(name = "解决时限(分钟)")
    private Integer resolutionTime;

    @Excel(name = "预警阈值(%)")
    private Integer warningThreshold;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    public ItSlaConfig()
    {
        this.status = "0";
        this.delFlag = "0";
        this.warningThreshold = 80;
    }

    public Long getSlaId()
    {
        return slaId;
    }

    public void setSlaId(Long slaId)
    {
        this.slaId = slaId;
    }

    public String getSlaName()
    {
        return slaName;
    }

    public void setSlaName(String slaName)
    {
        this.slaName = slaName;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public Integer getResponseTime()
    {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime)
    {
        this.responseTime = responseTime;
    }

    public Integer getResolutionTime()
    {
        return resolutionTime;
    }

    public void setResolutionTime(Integer resolutionTime)
    {
        this.resolutionTime = resolutionTime;
    }

    public Integer getWarningThreshold()
    {
        return warningThreshold;
    }

    public void setWarningThreshold(Integer warningThreshold)
    {
        this.warningThreshold = warningThreshold;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("slaId", getSlaId())
            .append("slaName", getSlaName())
            .append("categoryId", getCategoryId())
            .append("priority", getPriority())
            .append("responseTime", getResponseTime())
            .append("resolutionTime", getResolutionTime())
            .append("warningThreshold", getWarningThreshold())
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
