package com.itsm.ticket.enums;

public enum TicketStatus
{
    DRAFT("DRAFT", "草稿"),
    SUBMITTED("SUBMITTED", "已提交"),
    APPROVED("APPROVED", "已审批"),
    ASSIGNED("ASSIGNED", "已分配"),
    PROCESSING("PROCESSING", "处理中"),
    RESOLVED("RESOLVED", "已解决"),
    VERIFIED("VERIFIED", "已验证"),
    CLOSED("CLOSED", "已关闭"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    TicketStatus(String code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public String getCode()
    {
        return code;
    }

    public String getDesc()
    {
        return desc;
    }

    public static TicketStatus fromCode(String code)
    {
        for (TicketStatus status : values())
        {
            if (status.code.equals(code))
            {
                return status;
            }
        }
        return null;
    }
}
