package com.itsm.ticket.service.ticket.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.ticket.ItTicketLog;
import com.itsm.ticket.mapper.ticket.ItTicketLogMapper;
import com.itsm.ticket.service.ticket.IItTicketLogService;

/**
 * 工单操作日志 服务层实现
 *
 * @author itsm
 */
@Service
public class ItTicketLogServiceImpl implements IItTicketLogService
{
    @Autowired
    private ItTicketLogMapper logMapper;

    @Override
    public List<ItTicketLog> selectLogsByTicketId(Long ticketId)
    {
        return logMapper.selectLogsByTicketId(ticketId);
    }

    @Override
    public List<ItTicketLog> selectLogList(ItTicketLog log)
    {
        return logMapper.selectLogList(log);
    }

    @Override
    public int insertLog(ItTicketLog log)
    {
        return logMapper.insertLog(log);
    }
}
