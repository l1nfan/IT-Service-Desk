package com.itsm.ticket.domain.workflow;

import java.io.Serializable;
import com.itsm.common.annotation.Excel;

public class WfTransition implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "流转ID", cellType = Excel.ColumnType.NUMERIC)
    private Long transitionId;

    @Excel(name = "流程ID", cellType = Excel.ColumnType.NUMERIC)
    private Long workflowId;

    @Excel(name = "源节点标识")
    private String fromNodeKey;

    @Excel(name = "目标节点标识")
    private String toNodeKey;

    @Excel(name = "动作标识")
    private String action;

    @Excel(name = "流转名称")
    private String transitionName;

    private String conditionExpr;

    private String permission;

    private Integer orderNum;

    public Long getTransitionId()
    {
        return transitionId;
    }

    public void setTransitionId(Long transitionId)
    {
        this.transitionId = transitionId;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getFromNodeKey()
    {
        return fromNodeKey;
    }

    public void setFromNodeKey(String fromNodeKey)
    {
        this.fromNodeKey = fromNodeKey;
    }

    public String getToNodeKey()
    {
        return toNodeKey;
    }

    public void setToNodeKey(String toNodeKey)
    {
        this.toNodeKey = toNodeKey;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getTransitionName()
    {
        return transitionName;
    }

    public void setTransitionName(String transitionName)
    {
        this.transitionName = transitionName;
    }

    public String getConditionExpr()
    {
        return conditionExpr;
    }

    public void setConditionExpr(String conditionExpr)
    {
        this.conditionExpr = conditionExpr;
    }

    public String getPermission()
    {
        return permission;
    }

    public void setPermission(String permission)
    {
        this.permission = permission;
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
