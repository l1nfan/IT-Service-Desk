package com.itsm.ticket.domain.workflow;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.itsm.common.annotation.Excel;

public class WfInstance implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "实例ID", cellType = Excel.ColumnType.NUMERIC)
    private Long instanceId;

    @Excel(name = "流程ID", cellType = Excel.ColumnType.NUMERIC)
    private Long workflowId;

    @Excel(name = "业务ID", cellType = Excel.ColumnType.NUMERIC)
    private Long businessId;

    private String businessType;

    @Excel(name = "当前节点标识")
    private String currentNodeKey;

    @Excel(name = "状态")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public WfInstance()
    {
        this.businessType = "TICKET";
        this.status = "RUNNING";
    }

    public Long getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Long instanceId)
    {
        this.instanceId = instanceId;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public String getCurrentNodeKey()
    {
        return currentNodeKey;
    }

    public void setCurrentNodeKey(String currentNodeKey)
    {
        this.currentNodeKey = currentNodeKey;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
