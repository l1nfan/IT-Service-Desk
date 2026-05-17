package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicket;

/**
 * 工单 数据层
 *
 * @author itsm
 */
public interface ItTicketMapper
{
    ItTicket selectTicketById(Long ticketId);

    List<ItTicket> selectTicketList(ItTicket ticket);

    int insertTicket(ItTicket ticket);

    int updateTicket(ItTicket ticket);

    int deleteTicketById(Long ticketId);

    int deleteTicketByIds(Long[] ticketIds);

    ItTicket checkTicketNoUnique(String ticketNo);

    int assignTicket(ItTicket ticket);

    int approveTicket(ItTicket ticket);

    int updateTicketStatus(ItTicket ticket);

    int countByCategoryId(Long categoryId);
}
