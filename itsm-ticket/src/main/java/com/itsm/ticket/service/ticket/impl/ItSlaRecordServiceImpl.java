package com.itsm.ticket.service.ticket.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.ticket.ItSlaRecord;
import com.itsm.ticket.mapper.ticket.ItSlaRecordMapper;
import com.itsm.ticket.service.ticket.IItSlaRecordService;

@Service
public class ItSlaRecordServiceImpl implements IItSlaRecordService
{
    @Autowired
    private ItSlaRecordMapper slaRecordMapper;

    @Override
    public ItSlaRecord selectSlaRecordById(Long recordId)
    {
        return slaRecordMapper.selectSlaRecordById(recordId);
    }

    @Override
    public ItSlaRecord selectSlaRecordByTicketId(Long ticketId)
    {
        return slaRecordMapper.selectSlaRecordByTicketId(ticketId);
    }

    @Override
    public List<ItSlaRecord> selectSlaRecordList(ItSlaRecord slaRecord)
    {
        return slaRecordMapper.selectSlaRecordList(slaRecord);
    }

    @Override
    public List<ItSlaRecord> selectActiveSlaRecords()
    {
        return slaRecordMapper.selectActiveSlaRecords();
    }

    @Override
    public int insertSlaRecord(ItSlaRecord slaRecord)
    {
        return slaRecordMapper.insertSlaRecord(slaRecord);
    }

    @Override
    public int updateSlaRecord(ItSlaRecord slaRecord)
    {
        return slaRecordMapper.updateSlaRecord(slaRecord);
    }
}
