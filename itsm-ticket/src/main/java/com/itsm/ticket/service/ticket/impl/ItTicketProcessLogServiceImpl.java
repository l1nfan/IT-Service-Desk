package com.itsm.ticket.service.ticket.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.ticket.ItTicketProcessLog;
import com.itsm.ticket.mapper.ticket.ItTicketProcessLogMapper;
import com.itsm.ticket.service.ticket.IItTicketProcessLogService;

@Service
public class ItTicketProcessLogServiceImpl implements IItTicketProcessLogService
{
    @Autowired
    private ItTicketProcessLogMapper processLogMapper;

    @Override
    public ItTicketProcessLog selectProcessLogById(Long processLogId)
    {
        return processLogMapper.selectProcessLogById(processLogId);
    }

    @Override
    public List<ItTicketProcessLog> selectProcessLogsByTicketId(Long ticketId)
    {
        return processLogMapper.selectProcessLogsByTicketId(ticketId);
    }

    @Override
    public int insertProcessLog(ItTicketProcessLog processLog)
    {
        return processLogMapper.insertProcessLog(processLog);
    }
}
