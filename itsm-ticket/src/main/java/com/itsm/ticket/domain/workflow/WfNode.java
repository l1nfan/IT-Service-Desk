package com.itsm.ticket.domain.workflow;

import java.io.Serializable;
import com.itsm.common.annotation.Excel;

public class WfNode implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "节点ID", cellType = Excel.ColumnType.NUMERIC)
    private Long nodeId;

    @Excel(name = "流程ID", cellType = Excel.ColumnType.NUMERIC)
    private Long workflowId;

    @Excel(name = "节点标识")
    private String nodeKey;

    @Excel(name = "节点名称")
    private String nodeName;

    @Excel(name = "节点类型")
    private String nodeType;

    private String assigneeType;

    private String assigneeValue;

    private Integer positionX;

    private Integer positionY;

    private Integer orderNum;

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getNodeKey()
    {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey)
    {
        this.nodeKey = nodeKey;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public String getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(String assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public String getAssigneeValue()
    {
        return assigneeValue;
    }

    public void setAssigneeValue(String assigneeValue)
    {
        this.assigneeValue = assigneeValue;
    }

    public Integer getPositionX()
    {
        return positionX;
    }

    public void setPositionX(Integer positionX)
    {
        this.positionX = positionX;
    }

    public Integer getPositionY()
    {
        return positionY;
    }

    public void setPositionY(Integer positionY)
    {
        this.positionY = positionY;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }
}
