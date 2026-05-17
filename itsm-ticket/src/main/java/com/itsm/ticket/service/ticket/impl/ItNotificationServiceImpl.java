package com.itsm.ticket.service.ticket.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.common.exception.ServiceException;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.ticket.domain.ticket.ItNotification;
import com.itsm.ticket.mapper.ticket.ItNotificationMapper;
import com.itsm.ticket.service.ticket.IItNotificationService;

@Service
public class ItNotificationServiceImpl implements IItNotificationService
{
    @Autowired
    private ItNotificationMapper notificationMapper;

    @Override
    public ItNotification selectNotificationById(Long notificationId)
    {
        return notificationMapper.selectNotificationById(notificationId);
    }

    @Override
    public List<ItNotification> selectNotificationsByReceiverId(Long receiverId)
    {
        return notificationMapper.selectNotificationsByReceiverId(receiverId);
    }

    @Override
    public int selectUnreadCountByReceiverId(Long receiverId)
    {
        return notificationMapper.selectUnreadCountByReceiverId(receiverId);
    }

    @Override
    public int insertNotification(ItNotification notification)
    {
        return notificationMapper.insertNotification(notification);
    }

    @Override
    public int markAsRead(Long notificationId)
    {
        ItNotification notification = notificationMapper.selectNotificationById(notificationId);
        if (notification == null)
        {
            throw new ServiceException("通知不存在");
        }
        if (!notification.getReceiverId().equals(SecurityUtils.getUserId()))
        {
            throw new ServiceException("无权操作他人通知");
        }
        return notificationMapper.markAsRead(notificationId);
    }

    @Override
    public int markAllAsRead(Long receiverId)
    {
        return notificationMapper.markAllAsRead(receiverId);
    }

    @Override
    public List<ItNotification> selectNotificationList(ItNotification notification)
    {
        return notificationMapper.selectNotificationList(notification);
    }

    @Override
    public void updateNotificationStatus(Long notificationId, String sendStatus)
    {
        ItNotification notification = new ItNotification();
        notification.setNotificationId(notificationId);
        notification.setSendStatus(sendStatus);
        if ("SENT".equals(sendStatus))
        {
            notification.setSendTime(new Date());
        }
        notificationMapper.updateNotificationStatus(notification);
    }

    @Override
    public int deleteNotificationByIds(Long[] notificationIds)
    {
        for (Long notificationId : notificationIds)
        {
            ItNotification notification = notificationMapper.selectNotificationById(notificationId);
            if (notification != null && !notification.getReceiverId().equals(SecurityUtils.getUserId()))
            {
                throw new ServiceException("无权删除他人通知");
            }
        }
        return notificationMapper.deleteNotificationByIds(notificationIds);
    }
}
