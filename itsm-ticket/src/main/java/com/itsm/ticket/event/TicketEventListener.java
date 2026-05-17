package com.itsm.ticket.event;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.itsm.common.core.domain.entity.SysRole;
import com.itsm.common.core.domain.entity.SysUser;
import com.itsm.ticket.domain.ticket.ItNotification;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import com.itsm.ticket.domain.ticket.ItSlaRecord;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.workflow.WfTask;
import com.itsm.ticket.enums.SlaStatus;
import com.itsm.ticket.service.ticket.IItNotificationService;
import com.itsm.ticket.service.ticket.IItSlaConfigService;
import com.itsm.ticket.service.ticket.IItSlaRecordService;
import com.itsm.ticket.service.workflow.IWfTaskService;
import com.itsm.ticket.service.workflow.WebSocketPushService;
import com.itsm.system.service.ISysRoleService;
import com.itsm.system.service.ISysUserService;

@Component
public class TicketEventListener
{
    private static final Logger log = LoggerFactory.getLogger(TicketEventListener.class);

    @Autowired
    private IItNotificationService notificationService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    @Autowired
    private IItSlaConfigService slaConfigService;

    @Autowired
    private IItSlaRecordService slaRecordService;

    @Autowired
    private IWfTaskService wfTaskService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTicketEvent(TicketEvent event)
    {
        ItTicket ticket = event.getTicket();
        String action = event.getAction();

        if ("SUBMIT".equals(action))
        {
            ItNotification n = sendNotificationToApprovers(ticket, event);
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(n.getReceiverId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
            createSlaRecord(ticket);
        }
        else if ("APPROVE".equals(action))
        {
            ItNotification n = sendNotificationToAssigners(ticket, event);
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(n.getReceiverId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
        }
        else if ("ASSIGN".equals(action))
        {
            ItNotification n = sendNotificationToAssignee(ticket, event);
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(ticket.getAssigneeId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
            updateSlaResponseTime(ticket);
        }
        else if ("RESOLVE".equals(action))
        {
            ItNotification n = sendNotificationToCreator(ticket, event, "您的工单已解决");
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(ticket.getCreatorId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
            updateSlaResolutionTime(ticket);
        }
        else if ("REJECT".equals(action))
        {
            ItNotification n = sendNotificationToCreator(ticket, event, "您的工单已被驳回");
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(ticket.getCreatorId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
        }
        else if ("CLOSE".equals(action))
        {
            ItNotification n = sendNotificationToCreator(ticket, event, "您的工单已关闭");
            if (n != null)
            {
                try
                {
                    webSocketPushService.pushNotification(ticket.getCreatorId(), n);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "SENT");
                }
                catch (Exception e)
                {
                    log.warn("WebSocket推送通知失败, notificationId={}", n.getNotificationId(), e);
                    notificationService.updateNotificationStatus(n.getNotificationId(), "FAILED");
                }
            }
        }

        webSocketPushService.broadcastTicketUpdate(ticket.getTicketId(), event);
    }

    private ItNotification sendNotificationToApprovers(ItTicket ticket, TicketEvent event)
    {
        ItNotification notification = new ItNotification();
        notification.setTitle("新工单待审批: " + ticket.getTitle());
        notification.setContent("工单编号 " + ticket.getTicketNo() + " 需要您审批");
        notification.setNotificationType("SYSTEM");
        notification.setBusinessId(ticket.getTicketId());
        notification.setBusinessType("TICKET");
        Long receiverId = resolveReceiverId(ticket, event, "SUBMITTED");
        if (receiverId == null)
        {
            log.warn("无法确定审批通知接收人, ticketId={}", ticket.getTicketId());
            return null;
        }
        notification.setReceiverId(receiverId);
        notification.setSenderId(event.getOperatorId());
        notification.setSendStatus("PENDING");
        notification.setSendTime(new Date());
        notificationService.insertNotification(notification);
        return notification;
    }

    private ItNotification sendNotificationToAssigners(ItTicket ticket, TicketEvent event)
    {
        Long receiverId = resolveReceiverId(ticket, event, "APPROVED");
        if (receiverId == null)
        {
            log.warn("无法确定分配通知接收人, ticketId={}", ticket.getTicketId());
            return null;
        }
        ItNotification notification = new ItNotification();
        notification.setTitle("工单待分配: " + ticket.getTitle());
        notification.setContent("工单编号 " + ticket.getTicketNo() + " 已审批通过，请分配处理人");
        notification.setNotificationType("SYSTEM");
        notification.setBusinessId(ticket.getTicketId());
        notification.setBusinessType("TICKET");
        notification.setReceiverId(receiverId);
        notification.setSenderId(event.getOperatorId());
        notification.setSendStatus("PENDING");
        notification.setSendTime(new Date());
        notificationService.insertNotification(notification);
        return notification;
    }

    private Long resolveReceiverId(ItTicket ticket, TicketEvent event, String targetNodeKey)
    {
        if (ticket.getApproverId() != null)
        {
            return ticket.getApproverId();
        }
        if (ticket.getWorkflowInstanceId() != null)
        {
            try
            {
                List<WfTask> tasks = wfTaskService.selectTasksByBusinessId(ticket.getTicketId(), "TICKET");
                for (WfTask task : tasks)
                {
                    if ("PENDING".equals(task.getStatus()) && targetNodeKey.equals(task.getNodeKey()) && task.getAssigneeId() != null)
                    {
                        return task.getAssigneeId();
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("查询工作流待办任务失败, ticketId={}", ticket.getTicketId(), e);
            }
        }
        try
        {
            SysRole roleQuery = new SysRole();
            roleQuery.setRoleKey("admin");
            List<SysRole> adminRoles = roleService.selectRoleList(roleQuery);
            if (!adminRoles.isEmpty())
            {
                SysUser userQuery = new SysUser();
                userQuery.setRoleId(adminRoles.get(0).getRoleId());
                List<SysUser> adminUsers = userService.selectAllocatedList(userQuery);
                if (!adminUsers.isEmpty())
                {
                    return adminUsers.get(0).getUserId();
                }
            }
        }
        catch (Exception e)
        {
            log.error("查询管理员用户失败，无法确定通知接收人, ticketId={}", ticket.getTicketId(), e);
        }
        return null;
    }

    private ItNotification sendNotificationToAssignee(ItTicket ticket, TicketEvent event)
    {
        if (ticket.getAssigneeId() == null)
        {
            return null;
        }
        ItNotification notification = new ItNotification();
        notification.setTitle("新工单待处理: " + ticket.getTitle());
        notification.setContent("工单编号 " + ticket.getTicketNo() + " 已分配给您处理");
        notification.setNotificationType("SYSTEM");
        notification.setBusinessId(ticket.getTicketId());
        notification.setBusinessType("TICKET");
        notification.setReceiverId(ticket.getAssigneeId());
        notification.setSenderId(event.getOperatorId());
        notification.setSendStatus("PENDING");
        notification.setSendTime(new Date());
        notificationService.insertNotification(notification);
        return notification;
    }

    private ItNotification sendNotificationToCreator(ItTicket ticket, TicketEvent event, String message)
    {
        if (ticket.getCreatorId() == null)
        {
            return null;
        }
        ItNotification notification = new ItNotification();
        notification.setTitle(message + ": " + ticket.getTitle());
        notification.setContent("工单编号 " + ticket.getTicketNo() + " - " + message);
        notification.setNotificationType("SYSTEM");
        notification.setBusinessId(ticket.getTicketId());
        notification.setBusinessType("TICKET");
        notification.setReceiverId(ticket.getCreatorId());
        notification.setSenderId(event.getOperatorId());
        notification.setSendStatus("PENDING");
        notification.setSendTime(new Date());
        notificationService.insertNotification(notification);
        return notification;
    }

    private void createSlaRecord(ItTicket ticket)
    {
        try
        {
            ItSlaConfig config = slaConfigService.selectSlaConfigByCategoryAndPriority(
                ticket.getCategoryId(), mapPriority(ticket.getPriority()));
            if (config == null)
            {
                log.debug("未找到匹配的SLA配置: categoryId={}, priority={}",
                    ticket.getCategoryId(), ticket.getPriority());
                return;
            }

            ItSlaRecord record = new ItSlaRecord();
            record.setTicketId(ticket.getTicketId());
            record.setSlaConfigId(config.getSlaId());
            record.setSlaStartTime(new Date());

            if (config.getResponseTime() != null)
            {
                record.setResponseDeadline(new Date(System.currentTimeMillis() + config.getResponseTime() * 60000L));
            }
            if (config.getResolutionTime() != null)
            {
                record.setResolutionDeadline(new Date(System.currentTimeMillis() + config.getResolutionTime() * 60000L));
            }

            slaRecordService.insertSlaRecord(record);
        }
        catch (Exception e)
        {
            log.warn("SLA记录创建失败: ticketId={}", ticket.getTicketId(), e);
        }
    }

    private void updateSlaResponseTime(ItTicket ticket)
    {
        try
        {
            ItSlaRecord record = slaRecordService.selectSlaRecordByTicketId(ticket.getTicketId());
            if (record != null && record.getResponseActual() == null)
            {
                record.setResponseActual(new Date());
                slaRecordService.updateSlaRecord(record);
            }
        }
        catch (Exception e)
        {
            log.warn("SLA响应时间更新失败: ticketId={}", ticket.getTicketId(), e);
        }
    }

    private void updateSlaResolutionTime(ItTicket ticket)
    {
        try
        {
            ItSlaRecord record = slaRecordService.selectSlaRecordByTicketId(ticket.getTicketId());
            if (record != null)
            {
                Date now = new Date();
                record.setResolutionActual(now);
                Date deadline = record.getResolutionDeadline();
                if (!SlaStatus.BREACHED.getCode().equals(record.getStatus()) && !SlaStatus.WARNING.getCode().equals(record.getStatus()))
                {
                    record.setStatus(SlaStatus.RESOLVED.getCode());
                }
                slaRecordService.updateSlaRecord(record);
            }
        }
        catch (Exception e)
        {
            log.warn("SLA解决时间更新失败: ticketId={}", ticket.getTicketId(), e);
        }
    }

    private String mapPriority(Integer priority)
    {
        if (priority == null) return "MEDIUM";
        return switch (priority)
        {
            case 1 -> "URGENT";
            case 2 -> "HIGH";
            case 3 -> "MEDIUM";
            case 4 -> "LOW";
            default -> "MEDIUM";
        };
    }
}
