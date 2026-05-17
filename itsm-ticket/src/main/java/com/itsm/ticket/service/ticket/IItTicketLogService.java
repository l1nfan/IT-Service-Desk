package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketLog;

/**
 * 工单操作日志 服务层
 *
 * @author itsm
 */
public interface IItTicketLogService
{
    List<ItTicketLog> selectLogsByTicketId(Long ticketId);

    List<ItTicketLog> selectLogList(ItTicketLog log);

    int insertLog(ItTicketLog log);
}
