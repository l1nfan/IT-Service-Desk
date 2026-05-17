package com.itsm.ticket.event;

import org.springframework.context.ApplicationEvent;
import com.itsm.ticket.domain.ticket.ItTicket;

public class TicketEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    private final ItTicket ticket;
    private final String action;
    private final String fromStatus;
    private final String toStatus;
    private final Long operatorId;
    private final String operatorName;

    public TicketEvent(Object source, ItTicket ticket, String action,
                       String fromStatus, String toStatus,
                       Long operatorId, String operatorName)
    {
        super(source);
        this.ticket = ticket;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }

    public ItTicket getTicket()
    {
        return ticket;
    }

    public String getAction()
    {
        return action;
    }

    public String getFromStatus()
    {
        return fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }
}
