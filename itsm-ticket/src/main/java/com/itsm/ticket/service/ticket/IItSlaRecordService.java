package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItSlaRecord;

public interface IItSlaRecordService
{
    ItSlaRecord selectSlaRecordById(Long recordId);

    ItSlaRecord selectSlaRecordByTicketId(Long ticketId);

    List<ItSlaRecord> selectSlaRecordList(ItSlaRecord slaRecord);

    List<ItSlaRecord> selectActiveSlaRecords();

    int insertSlaRecord(ItSlaRecord slaRecord);

    int updateSlaRecord(ItSlaRecord slaRecord);
}
