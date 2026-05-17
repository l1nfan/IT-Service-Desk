package com.itsm.ticket.domain.ticket;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.itsm.common.annotation.Excel;

/**
 * 工单操作日志表 it_ticket_log
 *
 * @author itsm
 */
public class ItTicketLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "日志ID", cellType = Excel.ColumnType.NUMERIC)
    private Long logId;

    @Excel(name = "工单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long ticketId;

    @Excel(name = "工单编号")
    private String ticketNo;

    @Excel(name = "操作类型")
    private String action;

    @Excel(name = "操作名称")
    private String actionName;

    @Excel(name = "原状态")
    private String fromStatus;

    @Excel(name = "目标状态")
    private String toStatus;

    @Excel(name = "操作人ID", cellType = Excel.ColumnType.NUMERIC)
    private Long operatorId;

    @Excel(name = "操作人姓名")
    private String operatorName;

    @Excel(name = "操作备注")
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

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

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public String getFromStatus()
    {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus)
    {
        this.fromStatus = fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public void setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }
}
