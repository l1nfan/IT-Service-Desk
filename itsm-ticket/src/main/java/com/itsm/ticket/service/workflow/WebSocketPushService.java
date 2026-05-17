package com.itsm.ticket.service.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.ticket.ItNotification;

@Service
public class WebSocketPushService
{
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushNotification(Long userId, ItNotification notification)
    {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    public void pushTicketUpdate(Long userId, Object ticketUpdate)
    {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/ticket",
            ticketUpdate
        );
    }

    public void broadcastTicketUpdate(Object ticketUpdate)
    {
        messagingTemplate.convertAndSend("/topic/ticket", ticketUpdate);
    }

    public void broadcastTicketUpdate(Long ticketId, Object ticketUpdate)
    {
        messagingTemplate.convertAndSend("/topic/ticket/" + ticketId, ticketUpdate);
    }
}
