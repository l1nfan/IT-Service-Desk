package com.itsm.quartz.task;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.itsm.common.core.domain.entity.SysRole;
import com.itsm.common.core.domain.entity.SysUser;
import com.itsm.ticket.domain.ticket.ItNotification;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import com.itsm.ticket.domain.ticket.ItSlaRecord;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.enums.SlaStatus;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import com.itsm.ticket.service.ticket.IItNotificationService;
import com.itsm.ticket.service.ticket.IItSlaConfigService;
import com.itsm.ticket.service.ticket.IItSlaRecordService;
import com.itsm.ticket.service.workflow.WebSocketPushService;
import com.itsm.system.service.ISysRoleService;
import com.itsm.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("slaMonitorJob")
public class SlaMonitorJob
{
    private static final Logger log = LoggerFactory.getLogger(SlaMonitorJob.class);

    @Autowired
    private IItSlaRecordService slaRecordService;

    @Autowired
    private IItSlaConfigService slaConfigService;

    @Autowired
    private IItNotificationService notificationService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    @Autowired
    private ItTicketMapper ticketMapper;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    public void checkSlaStatus()
    {
        log.info("SLA监控任务开始执行...");
        List<ItSlaRecord> activeRecords = slaRecordService.selectActiveSlaRecords();
        Date now = new Date();

        for (ItSlaRecord record : activeRecords)
        {
            try
            {
                ItSlaConfig config = slaConfigService.selectSlaConfigById(record.getSlaConfigId());
                if (config == null)
                {
                    continue;
                }

                if (SlaStatus.BREACHED.getCode().equals(record.getStatus()))
                {
                    continue;
                }

                boolean breached = false;
                boolean warning = false;

                if (record.getResolutionDeadline() != null && now.after(record.getResolutionDeadline()))
                {
                    breached = true;
                }
                else if (record.getResponseDeadline() != null && now.after(record.getResponseDeadline())
                    && record.getResponseActual() == null)
                {
                    breached = true;
                }
                else
                {
                    int threshold = config.getWarningThreshold() != null ? config.getWarningThreshold() : 80;

                    if (record.getResolutionDeadline() != null)
                    {
                        long totalMillis = record.getResolutionDeadline().getTime() - record.getSlaStartTime().getTime();
                        long elapsedMillis = now.getTime() - record.getSlaStartTime().getTime();
                        if (totalMillis > 0 && (elapsedMillis * 100 / totalMillis) >= threshold)
                        {
                            warning = true;
                        }
                    }

                    if (!warning && record.getResponseDeadline() != null && record.getResponseActual() == null)
                    {
                        long totalMillis = record.getResponseDeadline().getTime() - record.getSlaStartTime().getTime();
                        long elapsedMillis = now.getTime() - record.getSlaStartTime().getTime();
                        if (totalMillis > 0 && (elapsedMillis * 100 / totalMillis) >= threshold)
                        {
                            warning = true;
                        }
                    }
                }

                if (breached)
                {
                    record.setStatus(SlaStatus.BREACHED.getCode());
                    record.setBreachedTime(now);
                    slaRecordService.updateSlaRecord(record);
                    sendSlaNotification(record, config, SlaStatus.BREACHED.getCode());
                    log.warn("SLA超时: recordId={}, ticketId={}", record.getRecordId(), record.getTicketId());
                }
                else if (warning && !SlaStatus.WARNING.getCode().equals(record.getStatus()))
                {
                    record.setStatus(SlaStatus.WARNING.getCode());
                    record.setWarningCount(record.getWarningCount() != null ? record.getWarningCount() + 1 : 1);
                    record.setWarningTime(now);
                    slaRecordService.updateSlaRecord(record);
                    sendSlaNotification(record, config, SlaStatus.WARNING.getCode());
                    log.info("SLA预警: recordId={}, ticketId={}, count={}",
                        record.getRecordId(), record.getTicketId(), record.getWarningCount());
                }
            }
            catch (Exception e)
            {
                log.error("SLA监控处理失败: recordId={}", record.getRecordId(), e);
            }
        }
        log.info("SLA监控任务完成, 扫描记录数: {}", activeRecords.size());
    }

    private void sendSlaNotification(ItSlaRecord record, ItSlaConfig config, String slaStatus)
    {
        Long adminUserId = getAdminUserId();

        ItTicket ticket = ticketMapper.selectTicketById(record.getTicketId());
        Long receiverId = null;
        if (ticket != null && ticket.getAssigneeId() != null)
        {
            receiverId = ticket.getAssigneeId();
        }
        else if (adminUserId != null)
        {
            receiverId = adminUserId;
        }

        if (receiverId == null)
        {
            log.warn("无法确定SLA通知接收人，跳过通知, ticketId={}", record.getTicketId());
            return;
        }

        ItNotification notification = new ItNotification();
        if (SlaStatus.BREACHED.getCode().equals(slaStatus))
        {
            notification.setTitle("SLA超时预警: " + config.getSlaName());
            notification.setContent("工单ID " + record.getTicketId() + " 已超出SLA时限，请及时处理");
        }
        else
        {
            notification.setTitle("SLA预警提醒: " + config.getSlaName());
            notification.setContent("工单ID " + record.getTicketId() + " 即将达到SLA时限，请尽快处理");
        }
        notification.setNotificationType("SYSTEM");
        notification.setBusinessId(record.getTicketId());
        notification.setBusinessType("SLA");
        notification.setReceiverId(receiverId);
        notification.setSenderId(0L);
        notification.setSendStatus("SENT");
        notification.setSendTime(new Date());
        notificationService.insertNotification(notification);

        ItNotification adminNotification = null;
        if (SlaStatus.BREACHED.getCode().equals(slaStatus) && adminUserId != null)
        {
            adminNotification = new ItNotification();
            adminNotification.setTitle("SLA超时通报: " + config.getSlaName());
            adminNotification.setContent("工单ID " + record.getTicketId() + " 已超出SLA时限，处理人未及时处理");
            adminNotification.setNotificationType("SYSTEM");
            adminNotification.setBusinessId(record.getTicketId());
            adminNotification.setBusinessType("SLA");
            adminNotification.setReceiverId(adminUserId);
            adminNotification.setSenderId(0L);
            adminNotification.setSendStatus("SENT");
            adminNotification.setSendTime(new Date());
            notificationService.insertNotification(adminNotification);
        }
        try
        {
            webSocketPushService.pushNotification(notification.getReceiverId(), notification);
            if (adminNotification != null)
            {
                webSocketPushService.pushNotification(adminNotification.getReceiverId(), adminNotification);
            }
        }
        catch (Exception e)
        {
            log.warn("SLA WebSocket推送失败: ticketId={}", record.getTicketId(), e);
        }
    }

    private Long getAdminUserId()
    {
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
            log.error("查询管理员用户失败，无法确定SLA通知接收人", e);
        }
        return null;
    }
}
