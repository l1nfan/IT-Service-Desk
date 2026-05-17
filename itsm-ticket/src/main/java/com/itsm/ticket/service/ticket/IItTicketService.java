package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.TicketTransitDTO;
import com.itsm.ticket.domain.workflow.WfTransition;
import com.itsm.ticket.enums.TicketStatus;

public interface IItTicketService
{
    ItTicket selectTicketById(Long ticketId);

    List<ItTicket> selectTicketList(ItTicket ticket);

    int insertTicket(ItTicket ticket);

    int updateTicket(ItTicket ticket);

    void deleteTicketByIds(Long[] ticketIds);

    String generateTicketNo();

    int assignTicket(ItTicket ticket);

    int reassignTicket(ItTicket ticket);

    int approveTicket(ItTicket ticket);

    int transitTicket(ItTicket ticket, TicketStatus targetStatus);

    int transitTicket(TicketTransitDTO dto);

    List<TicketStatus> getAvailableTransitions(Long ticketId);

    List<WfTransition> getAvailableWorkflowTransitions(Long ticketId);
}
