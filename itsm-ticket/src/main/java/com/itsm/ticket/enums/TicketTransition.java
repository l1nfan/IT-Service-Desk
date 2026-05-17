package com.itsm.ticket.enums;

public enum TicketTransition
{
    SUBMIT(TicketStatus.DRAFT, TicketStatus.SUBMITTED, "itsm:ticket:edit", "提交工单"),
    APPROVE(TicketStatus.SUBMITTED, TicketStatus.APPROVED, "itsm:ticket:approve", "审批通过"),
    REJECT(TicketStatus.SUBMITTED, TicketStatus.REJECTED, "itsm:ticket:approve", "审批驳回"),
    ASSIGN(TicketStatus.APPROVED, TicketStatus.ASSIGNED, "itsm:ticket:assign", "分配工单"),
    START_PROCESS(TicketStatus.ASSIGNED, TicketStatus.PROCESSING, "itsm:ticket:edit", "开始处理"),
    RESOLVE(TicketStatus.PROCESSING, TicketStatus.RESOLVED, "itsm:ticket:edit", "解决工单"),
    VERIFY(TicketStatus.RESOLVED, TicketStatus.VERIFIED, "itsm:ticket:approve", "验证通过"),
    REOPEN(TicketStatus.RESOLVED, TicketStatus.PROCESSING, "itsm:ticket:approve", "重新处理"),
    CLOSE(TicketStatus.VERIFIED, TicketStatus.CLOSED, "itsm:ticket:approve", "关闭工单"),
    REDRAFT(TicketStatus.REJECTED, TicketStatus.DRAFT, "itsm:ticket:edit", "重新编辑"),
    CANCEL_FROM_DRAFT(TicketStatus.DRAFT, TicketStatus.CANCELLED, "itsm:ticket:edit", "取消工单"),
    CANCEL_FROM_SUBMITTED(TicketStatus.SUBMITTED, TicketStatus.CANCELLED, "itsm:ticket:edit", "取消工单"),
    CANCEL_FROM_APPROVED(TicketStatus.APPROVED, TicketStatus.CANCELLED, "itsm:ticket:assign", "取消工单"),
    CANCEL_FROM_ASSIGNED(TicketStatus.ASSIGNED, TicketStatus.CANCELLED, "itsm:ticket:assign", "取消工单"),
    CANCEL_FROM_PROCESSING(TicketStatus.PROCESSING, TicketStatus.CANCELLED, "itsm:ticket:edit", "取消工单"),
    REASSIGN_ASSIGNED(TicketStatus.ASSIGNED, TicketStatus.ASSIGNED, "itsm:ticket:assign", "转派"),
    REASSIGN_PROCESSING(TicketStatus.PROCESSING, TicketStatus.ASSIGNED, "itsm:ticket:assign", "转派");

    private final TicketStatus from;
    private final TicketStatus to;
    private final String permission;
    private final String action;

    TicketTransition(TicketStatus from, TicketStatus to, String permission, String action)
    {
        this.from = from;
        this.to = to;
        this.permission = permission;
        this.action = action;
    }

    public TicketStatus getFrom()
    {
        return from;
    }

    public TicketStatus getTo()
    {
        return to;
    }

    public String getPermission()
    {
        return permission;
    }

    public String getAction()
    {
        return action;
    }

    public static TicketTransition findTransition(TicketStatus from, TicketStatus to)
    {
        for (TicketTransition transition : values())
        {
            if (transition.from == from && transition.to == to)
            {
                return transition;
            }
        }
        return null;
    }

    public static boolean isValidTransition(TicketStatus from, TicketStatus to)
    {
        return findTransition(from, to) != null;
    }
}
