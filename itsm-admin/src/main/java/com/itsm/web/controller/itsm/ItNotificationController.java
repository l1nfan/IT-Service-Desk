package com.itsm.web.controller.itsm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itsm.common.core.controller.BaseController;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.core.page.TableDataInfo;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.ticket.domain.ticket.ItNotification;
import com.itsm.ticket.service.ticket.IItNotificationService;

@RestController
@RequestMapping("/itsm/notification")
public class ItNotificationController extends BaseController
{
    @Autowired
    private IItNotificationService notificationService;

    @PreAuthorize("@ss.hasPermi('itsm:notification:list')")
    @GetMapping("/list")
    public TableDataInfo list(ItNotification notification)
    {
        notification.setReceiverId(SecurityUtils.getUserId());
        startPage();
        List<ItNotification> list = notificationService.selectNotificationList(notification);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:notification:query')")
    @GetMapping("/unread-count")
    public AjaxResult unreadCount()
    {
        return success(notificationService.selectUnreadCountByReceiverId(SecurityUtils.getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('itsm:notification:query')")
    @PutMapping("/read/{notificationId}")
    public AjaxResult markAsRead(@PathVariable Long notificationId)
    {
        return toAjax(notificationService.markAsRead(notificationId));
    }

    @PreAuthorize("@ss.hasPermi('itsm:notification:query')")
    @PutMapping("/read-all")
    public AjaxResult markAllAsRead()
    {
        return toAjax(notificationService.markAllAsRead(SecurityUtils.getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('itsm:notification:remove')")
    @DeleteMapping("/{notificationIds}")
    public AjaxResult remove(@PathVariable Long[] notificationIds)
    {
        return toAjax(notificationService.deleteNotificationByIds(notificationIds));
    }
}
