package com.itsm.ticket.enums;

public enum SlaStatus
{
    NORMAL("NORMAL", "正常"),
    WARNING("WARNING", "预警"),
    BREACHED("BREACHED", "超时"),
    RESOLVED("RESOLVED", "已解决");

    private final String code;
    private final String desc;

    SlaStatus(String code, String desc)
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

    public static SlaStatus fromCode(String code)
    {
        for (SlaStatus status : values())
        {
            if (status.code.equals(code))
            {
                return status;
            }
        }
        return null;
    }
}
