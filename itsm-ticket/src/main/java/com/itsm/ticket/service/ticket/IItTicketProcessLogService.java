package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketProcessLog;

public interface IItTicketProcessLogService
{
    ItTicketProcessLog selectProcessLogById(Long processLogId);

    List<ItTicketProcessLog> selectProcessLogsByTicketId(Long ticketId);

    int insertProcessLog(ItTicketProcessLog processLog);
}
