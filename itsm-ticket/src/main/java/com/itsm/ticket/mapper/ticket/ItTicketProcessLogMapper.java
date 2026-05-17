package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketProcessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItTicketProcessLogMapper
{
    ItTicketProcessLog selectProcessLogById(Long processLogId);

    List<ItTicketProcessLog> selectProcessLogsByTicketId(Long ticketId);

    int insertProcessLog(ItTicketProcessLog processLog);
}
