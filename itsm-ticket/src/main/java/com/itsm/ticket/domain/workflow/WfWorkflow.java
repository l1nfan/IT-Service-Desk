package com.itsm.ticket.domain.workflow;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.itsm.common.annotation.Excel;
import com.itsm.common.core.domain.BaseEntity;

public class WfWorkflow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "流程ID", cellType = Excel.ColumnType.NUMERIC)
    private Long workflowId;

    @Excel(name = "流程名称")
    private String workflowName;

    @Excel(name = "流程标识")
    private String workflowKey;

    private String description;

    @Excel(name = "版本号")
    private Integer version;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String isDefault;

    private String delFlag;

    private transient List<WfNode> nodes;

    private transient List<WfTransition> transitions;

    public WfWorkflow()
    {
        this.version = 1;
        this.status = "0";
        this.isDefault = "0";
        this.delFlag = "0";
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    public String getWorkflowKey()
    {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey)
    {
        this.workflowKey = workflowKey;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<WfNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<WfNode> nodes)
    {
        this.nodes = nodes;
    }

    public List<WfTransition> getTransitions()
    {
        return transitions;
    }

    public void setTransitions(List<WfTransition> transitions)
    {
        this.transitions = transitions;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("workflowId", getWorkflowId())
            .append("workflowName", getWorkflowName())
            .append("workflowKey", getWorkflowKey())
            .append("description", getDescription())
            .append("version", getVersion())
            .append("status", getStatus())
            .append("isDefault", getIsDefault())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
