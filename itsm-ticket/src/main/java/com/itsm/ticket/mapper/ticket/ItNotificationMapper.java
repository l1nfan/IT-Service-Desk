package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItNotificationMapper
{
    ItNotification selectNotificationById(Long notificationId);

    List<ItNotification> selectNotificationsByReceiverId(Long receiverId);

    int selectUnreadCountByReceiverId(Long receiverId);

    int insertNotification(ItNotification notification);

    int updateNotification(ItNotification notification);

    int updateNotificationStatus(ItNotification notification);

    int markAsRead(Long notificationId);

    int markAllAsRead(Long receiverId);

    int deleteNotificationById(Long notificationId);

    int deleteNotificationByIds(Long[] notificationIds);

    List<ItNotification> selectNotificationList(ItNotification notification);
}
