package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItNotification;

public interface IItNotificationService
{
    ItNotification selectNotificationById(Long notificationId);

    List<ItNotification> selectNotificationsByReceiverId(Long receiverId);

    List<ItNotification> selectNotificationList(ItNotification notification);

    int selectUnreadCountByReceiverId(Long receiverId);

    int insertNotification(ItNotification notification);

    int markAsRead(Long notificationId);

    int markAllAsRead(Long receiverId);

    void updateNotificationStatus(Long notificationId, String sendStatus);

    int deleteNotificationByIds(Long[] notificationIds);
}
