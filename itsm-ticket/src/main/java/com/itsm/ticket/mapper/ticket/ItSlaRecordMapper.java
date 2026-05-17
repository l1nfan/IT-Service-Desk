package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItSlaRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItSlaRecordMapper
{
    ItSlaRecord selectSlaRecordById(Long recordId);

    ItSlaRecord selectSlaRecordByTicketId(Long ticketId);

    List<ItSlaRecord> selectSlaRecordList(ItSlaRecord slaRecord);

    List<ItSlaRecord> selectActiveSlaRecords();

    int insertSlaRecord(ItSlaRecord slaRecord);

    int updateSlaRecord(ItSlaRecord slaRecord);
}
