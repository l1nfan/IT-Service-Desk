package com.itsm.ticket.domain.ticket;

import jakarta.validation.constraints.NotNull;

public class TicketTransitDTO
{
    @NotNull(message = "工单ID不能为空")
    private Long ticketId;

    @NotNull(message = "目标状态不能为空")
    private String targetStatus;

    private String remark;

    private Long assigneeId;

    public Long getTicketId()
    {
        return ticketId;
    }

    public void setTicketId(Long ticketId)
    {
        this.ticketId = ticketId;
    }

    public String getTargetStatus()
    {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus)
    {
        this.targetStatus = targetStatus;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Long getAssigneeId()
    {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId)
    {
        this.assigneeId = assigneeId;
    }
}
