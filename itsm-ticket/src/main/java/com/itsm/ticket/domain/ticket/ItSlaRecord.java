package com.itsm.ticket.domain.ticket;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.itsm.common.annotation.Excel;

public class ItSlaRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "记录ID", cellType = Excel.ColumnType.NUMERIC)
    private Long recordId;

    @Excel(name = "工单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long ticketId;

    private Long slaConfigId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "SLA开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date slaStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date responseDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date resolutionDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date responseActual;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date resolutionActual;

    @Excel(name = "状态", readConverterExp = "NORMAL=正常,WARNING=预警,BREACHED=超时,RESOLVED=已解决")
    private String status;

    private Integer warningCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date warningTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date breachedTime;

    public ItSlaRecord()
    {
        this.status = "NORMAL";
        this.warningCount = 0;
    }

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getTicketId()
    {
        return ticketId;
    }

    public void setTicketId(Long ticketId)
    {
        this.ticketId = ticketId;
    }

    public Long getSlaConfigId()
    {
        return slaConfigId;
    }

    public void setSlaConfigId(Long slaConfigId)
    {
        this.slaConfigId = slaConfigId;
    }

    public Date getSlaStartTime()
    {
        return slaStartTime;
    }

    public void setSlaStartTime(Date slaStartTime)
    {
        this.slaStartTime = slaStartTime;
    }

    public Date getResponseDeadline()
    {
        return responseDeadline;
    }

    public void setResponseDeadline(Date responseDeadline)
    {
        this.responseDeadline = responseDeadline;
    }

    public Date getResolutionDeadline()
    {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(Date resolutionDeadline)
    {
        this.resolutionDeadline = resolutionDeadline;
    }

    public Date getResponseActual()
    {
        return responseActual;
    }

    public void setResponseActual(Date responseActual)
    {
        this.responseActual = responseActual;
    }

    public Date getResolutionActual()
    {
        return resolutionActual;
    }

    public void setResolutionActual(Date resolutionActual)
    {
        this.resolutionActual = resolutionActual;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount)
    {
        this.warningCount = warningCount;
    }

    public Date getWarningTime()
    {
        return warningTime;
    }

    public void setWarningTime(Date warningTime)
    {
        this.warningTime = warningTime;
    }

    public Date getBreachedTime()
    {
        return breachedTime;
    }

    public void setBreachedTime(Date breachedTime)
    {
        this.breachedTime = breachedTime;
    }
}
