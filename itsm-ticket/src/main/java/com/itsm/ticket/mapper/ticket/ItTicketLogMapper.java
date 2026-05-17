package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketLog;

/**
 * 工单操作日志 数据层
 *
 * @author itsm
 */
public interface ItTicketLogMapper
{
    List<ItTicketLog> selectLogsByTicketId(Long ticketId);

    List<ItTicketLog> selectLogList(ItTicketLog log);

    int insertLog(ItTicketLog log);
}
