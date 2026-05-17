package com.itsm.ticket.service.ticket.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itsm.common.core.redis.RedisCache;
import com.itsm.common.exception.ServiceException;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.common.utils.StringUtils;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.ItTicketLog;
import com.itsm.ticket.domain.ticket.ItTicketProcessLog;
import com.itsm.ticket.domain.ticket.TicketTransitDTO;
import com.itsm.ticket.domain.workflow.WfTransition;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.enums.TicketTransition;
import com.itsm.ticket.event.TicketEvent;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import com.itsm.ticket.service.ticket.IItTicketLogService;
import com.itsm.ticket.service.ticket.IItTicketProcessLogService;
import com.itsm.ticket.service.ticket.IItTicketService;
import com.itsm.ticket.service.workflow.IWorkflowEngine;
import com.itsm.ticket.statemachine.TicketStateMachine;
import com.itsm.common.annotation.DataScope;

@Service
public class ItTicketServiceImpl implements IItTicketService
{
    private static final String TICKET_NO_PREFIX = "TK";

    @Autowired
    private ItTicketMapper ticketMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TicketStateMachine stateMachine;

    @Autowired
    private IItTicketLogService logService;

    @Autowired
    private IWorkflowEngine workflowEngine;

    @Autowired
    private IItTicketProcessLogService processLogService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public ItTicket selectTicketById(Long ticketId)
    {
        return ticketMapper.selectTicketById(ticketId);
    }

    @DataScope(deptAlias = "t", userAlias = "t", deptField = "creator_dept_id", userField = "creator_id")
    @Override
    public List<ItTicket> selectTicketList(ItTicket ticket)
    {
        return ticketMapper.selectTicketList(ticket);
    }

    @Transactional
    @Override
    public int insertTicket(ItTicket ticket)
    {
        ticket.setTicketNo(generateTicketNo());
        ticket.setStatus(TicketStatus.DRAFT.getCode());
        ticket.setSource(StringUtils.defaultIfEmpty(ticket.getSource(), "WEB"));
        ticket.setCreatorId(SecurityUtils.getUserId());
        ticket.setCreatorDeptId(SecurityUtils.getDeptId());
        ticket.setCreateBy(SecurityUtils.getUsername());

        Long workflowId = workflowEngine.getWorkflowIdByCategory(ticket.getCategoryId());
        if (workflowId != null)
        {
            ticket.setWorkflowId(workflowId);
        }
        ticket.setCurrentNode("DRAFT");

        int rows = ticketMapper.insertTicket(ticket);
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), ticket.getTicketNo(), "CREATE", "创建工单",
                    null, TicketStatus.DRAFT.getCode(), null);

            if (workflowId != null)
            {
                try
                {
                    Long instanceId = workflowEngine.startProcess(
                        workflowId, ticket.getTicketId(), "TICKET",
                        SecurityUtils.getUserId(), SecurityUtils.getUsername());
                    ticket.setWorkflowInstanceId(instanceId);
                    ItTicket updateTicket = new ItTicket();
                    updateTicket.setTicketId(ticket.getTicketId());
                    updateTicket.setWorkflowInstanceId(instanceId);
                    updateTicket.setUpdateBy(SecurityUtils.getUsername());
                    ticketMapper.updateTicket(updateTicket);
                }
                catch (Exception e)
                {
                    throw new ServiceException("启动流程失败: " + e.getMessage());
                }
            }

            recordProcessLog(ticket.getTicketId(), "CREATE", "创建工单",
                    null, TicketStatus.DRAFT.getCode(), null, null);

            eventPublisher.publishEvent(new TicketEvent(this, ticket, "CREATE",
                    null, TicketStatus.DRAFT.getCode(),
                    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        }
        return rows;
    }

    @Override
    public int updateTicket(ItTicket ticket)
    {
        ItTicket existing = ticketMapper.selectTicketById(ticket.getTicketId());
        if (existing == null)
        {
            throw new ServiceException("工单不存在");
        }
        TicketStatus currentStatus = TicketStatus.fromCode(existing.getStatus());
        if (currentStatus != TicketStatus.DRAFT && currentStatus != TicketStatus.REJECTED)
        {
            throw new ServiceException("只有草稿或已驳回的工单才能编辑");
        }
        ticket.setUpdateBy(SecurityUtils.getUsername());
        int rows = ticketMapper.updateTicket(ticket);
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), existing.getTicketNo(), "UPDATE", "编辑工单",
                    existing.getStatus(), existing.getStatus(), null);
            recordProcessLog(ticket.getTicketId(), "UPDATE", "编辑工单",
                    existing.getStatus(), existing.getStatus(), null, null);
        }
        return rows;
    }

    @Transactional
    @Override
    public void deleteTicketByIds(Long[] ticketIds)
    {
        for (Long ticketId : ticketIds)
        {
            ItTicket existing = ticketMapper.selectTicketById(ticketId);
            if (existing == null)
            {
                continue;
            }
            TicketStatus currentStatus = TicketStatus.fromCode(existing.getStatus());
            if (currentStatus != TicketStatus.DRAFT
                && currentStatus != TicketStatus.REJECTED
                && currentStatus != TicketStatus.CANCELLED)
            {
                throw new ServiceException("只有草稿、已驳回或已取消的工单才能删除");
            }
            if (existing.getWorkflowInstanceId() != null)
            {
                try
                {
                    workflowEngine.terminateProcess(existing.getWorkflowInstanceId());
                }
                catch (Exception ignored)
                {
                }
            }
            ticketMapper.deleteTicketById(ticketId);
            recordLog(ticketId, existing.getTicketNo(), "DELETE", "删除工单",
                    existing.getStatus(), null, null);
        }
    }

    @Override
    public String generateTicketNo()
    {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String cacheKey = "itsm:ticket:seq:" + dateStr;
        Long seq = redisCache.increment(cacheKey, 1, 25, TimeUnit.HOURS);
        if (seq > 9999)
        {
            throw new ServiceException("当日工单编号已用尽，请联系管理员");
        }
        return TICKET_NO_PREFIX + "-" + dateStr + "-" + String.format("%04d", seq);
    }

    @Transactional
    @Override
    public int assignTicket(ItTicket ticket)
    {
        ItTicket existing = getAndValidate(ticket.getTicketId());
        TicketTransition transition = stateMachine.validateTransition(
            TicketStatus.fromCode(existing.getStatus()), TicketStatus.ASSIGNED);
        ticket.setStatus(transition.getTo().getCode());
        ticket.setOldStatus(existing.getStatus());
        ticket.setUpdateBy(SecurityUtils.getUsername());
        int rows = ticketMapper.assignTicket(ticket);
        if (rows == 0)
        {
            throw new ServiceException("工单状态已变更，请刷新后重试");
        }
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), existing.getTicketNo(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(), ticket.getRemark());

            updateWorkflowNode(existing, transition);

            recordProcessLog(ticket.getTicketId(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(),
                    existing.getAssigneeId(), ticket.getAssigneeId());

            eventPublisher.publishEvent(new TicketEvent(this, ticket, transition.name(),
                    existing.getStatus(), transition.getTo().getCode(),
                    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        }
        return rows;
    }

    @Transactional
    @Override
    public int reassignTicket(ItTicket ticket)
    {
        ItTicket existing = getAndValidate(ticket.getTicketId());
        TicketStatus currentStatus = TicketStatus.fromCode(existing.getStatus());
        if (currentStatus != TicketStatus.ASSIGNED && currentStatus != TicketStatus.PROCESSING)
        {
            throw new ServiceException("只有已分配或处理中的工单才能转派");
        }
        TicketTransition transition = stateMachine.validateTransition(currentStatus, TicketStatus.ASSIGNED);
        ticket.setStatus(transition.getTo().getCode());
        ticket.setOldStatus(existing.getStatus());
        ticket.setUpdateBy(SecurityUtils.getUsername());
        int rows;
        if (currentStatus == TicketStatus.ASSIGNED)
        {
            rows = ticketMapper.assignTicket(ticket);
        }
        else
        {
            rows = ticketMapper.updateTicketStatus(ticket);
        }
        if (rows == 0)
        {
            throw new ServiceException("工单状态已变更，请刷新后重试");
        }
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), existing.getTicketNo(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(), ticket.getRemark());

            updateWorkflowNode(existing, transition);

            recordProcessLog(ticket.getTicketId(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(),
                    existing.getAssigneeId(), ticket.getAssigneeId());

            eventPublisher.publishEvent(new TicketEvent(this, ticket, transition.name(),
                    existing.getStatus(), transition.getTo().getCode(),
                    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        }
        return rows;
    }

    @Transactional
    @Override
    public int approveTicket(ItTicket ticket)
    {
        ItTicket existing = getAndValidate(ticket.getTicketId());
        String targetCode = StringUtils.defaultIfEmpty(ticket.getStatus(), TicketStatus.APPROVED.getCode());
        TicketStatus targetStatus = TicketStatus.fromCode(targetCode);
        if (targetStatus == null)
        {
            throw new ServiceException("无效的目标状态");
        }
        TicketTransition transition = stateMachine.validateTransition(
            TicketStatus.fromCode(existing.getStatus()), targetStatus);
        ticket.setStatus(transition.getTo().getCode());
        ticket.setOldStatus(existing.getStatus());
        ticket.setApproverId(SecurityUtils.getUserId());
        ticket.setUpdateBy(SecurityUtils.getUsername());
        int rows = ticketMapper.approveTicket(ticket);
        if (rows == 0)
        {
            throw new ServiceException("工单状态已变更，请刷新后重试");
        }
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), existing.getTicketNo(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(), ticket.getRemark());

            updateWorkflowNode(existing, transition);

            recordProcessLog(ticket.getTicketId(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(), null, null);

            eventPublisher.publishEvent(new TicketEvent(this, ticket, transition.name(),
                    existing.getStatus(), transition.getTo().getCode(),
                    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        }
        return rows;
    }

    private ItTicket getAndValidate(Long ticketId)
    {
        ItTicket existing = ticketMapper.selectTicketById(ticketId);
        if (existing == null)
        {
            throw new ServiceException("工单不存在");
        }
        return existing;
    }

    @Transactional
    @Override
    public int transitTicket(ItTicket ticket, TicketStatus targetStatus)
    {
        ItTicket existing = getAndValidate(ticket.getTicketId());
        TicketTransition transition = stateMachine.validateTransition(
            TicketStatus.fromCode(existing.getStatus()), targetStatus);
        ticket.setStatus(transition.getTo().getCode());
        ticket.setOldStatus(existing.getStatus());
        ticket.setUpdateBy(SecurityUtils.getUsername());

        handleSpecialStatusFields(ticket, existing, transition);

        int rows = ticketMapper.updateTicketStatus(ticket);
        if (rows == 0)
        {
            throw new ServiceException("工单状态已变更，请刷新后重试");
        }
        if (rows > 0)
        {
            recordLog(ticket.getTicketId(), existing.getTicketNo(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(), ticket.getRemark());

            updateWorkflowNode(existing, transition);

            recordProcessLog(ticket.getTicketId(), transition.name(), transition.getAction(),
                    existing.getStatus(), transition.getTo().getCode(),
                    existing.getAssigneeId(), ticket.getAssigneeId());

            eventPublisher.publishEvent(new TicketEvent(this, ticket, transition.name(),
                    existing.getStatus(), transition.getTo().getCode(),
                    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        }
        return rows;
    }

    @Transactional
    @Override
    public int transitTicket(TicketTransitDTO dto)
    {
        TicketStatus targetStatus = TicketStatus.fromCode(dto.getTargetStatus());
        if (targetStatus == null)
        {
            throw new ServiceException("无效的目标状态: " + dto.getTargetStatus());
        }
        ItTicket existing = getAndValidate(dto.getTicketId());
        TicketTransition transition = TicketTransition.findTransition(
            TicketStatus.fromCode(existing.getStatus()), targetStatus);
        if (transition == null)
        {
            throw new ServiceException("不允许的状态转换");
        }
        String permission = transition.getPermission();
        if (!SecurityUtils.hasPermi(permission))
        {
            throw new ServiceException("没有执行此操作的权限");
        }
        ItTicket ticket = new ItTicket();
        ticket.setTicketId(dto.getTicketId());
        ticket.setRemark(dto.getRemark());
        if (dto.getAssigneeId() != null)
        {
            ticket.setAssigneeId(dto.getAssigneeId());
        }
        return transitTicket(ticket, targetStatus);
    }

    @Override
    public List<TicketStatus> getAvailableTransitions(Long ticketId)
    {
        ItTicket existing = getAndValidate(ticketId);
        TicketStatus currentStatus = TicketStatus.fromCode(existing.getStatus());
        return stateMachine.getAvailableTargetStatuses(currentStatus);
    }

    @Override
    public List<WfTransition> getAvailableWorkflowTransitions(Long ticketId)
    {
        ItTicket existing = getAndValidate(ticketId);
        if (existing.getWorkflowInstanceId() != null)
        {
            try
            {
                return workflowEngine.getAvailableTransitions(existing.getTicketId(), "TICKET");
            }
            catch (Exception e)
            {
                return stateMachine.getAvailableTransitions(
                    TicketStatus.fromCode(existing.getStatus()))
                    .stream()
                    .map(t -> {
                        WfTransition wt = new WfTransition();
                        wt.setAction(t.name());
                        wt.setTransitionName(t.getAction());
                        wt.setFromNodeKey(t.getFrom().getCode());
                        wt.setToNodeKey(t.getTo().getCode());
                        wt.setPermission(t.getPermission());
                        return wt;
                    })
                    .toList();
            }
        }
        return stateMachine.getAvailableTransitions(
            TicketStatus.fromCode(existing.getStatus()))
            .stream()
            .map(t -> {
                WfTransition wt = new WfTransition();
                wt.setAction(t.name());
                wt.setTransitionName(t.getAction());
                wt.setFromNodeKey(t.getFrom().getCode());
                wt.setToNodeKey(t.getTo().getCode());
                wt.setPermission(t.getPermission());
                return wt;
            })
            .toList();
    }

    private void updateWorkflowNode(ItTicket existing, TicketTransition transition)
    {
        if (existing.getWorkflowInstanceId() != null)
        {
            try
            {
                workflowEngine.transit(
                    existing.getWorkflowInstanceId(),
                    transition.name(),
                    SecurityUtils.getUserId(),
                    SecurityUtils.getUsername(),
                    null);

                ItTicket updateTicket = new ItTicket();
                updateTicket.setTicketId(existing.getTicketId());
                updateTicket.setCurrentNode(transition.getTo().getCode());
                updateTicket.setUpdateBy(SecurityUtils.getUsername());
                ticketMapper.updateTicket(updateTicket);
            }
            catch (Exception e)
            {
                throw new ServiceException("流程流转失败: " + e.getMessage());
            }
        }
    }

    private void handleSpecialStatusFields(ItTicket ticket, ItTicket existing, TicketTransition transition)
    {
        switch (transition)
        {
            case ASSIGN ->
            {
                if (ticket.getAssigneeId() != null)
                {
                    ticket.setAssigneeDeptId(existing.getAssigneeDeptId());
                }
            }
            case RESOLVE ->
            {
                ticket.setResolvedBy(SecurityUtils.getUserId());
                ticket.setActualResolveTime(new Date());
            }
            case CLOSE ->
            {
                ticket.setClosedBy(SecurityUtils.getUserId());
                ticket.setCloseTime(new Date());
            }
            default ->
            {
            }
        }
    }

    private void recordLog(Long ticketId, String ticketNo, String action, String actionName,
                           String fromStatus, String toStatus, String comment)
    {
        ItTicketLog log = new ItTicketLog();
        log.setTicketId(ticketId);
        log.setTicketNo(ticketNo);
        log.setAction(action);
        log.setActionName(actionName);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(SecurityUtils.getUserId());
        log.setOperatorName(SecurityUtils.getUsername());
        log.setComment(comment);
        logService.insertLog(log);
    }

    private void recordProcessLog(Long ticketId, String action, String actionName,
                                  String fromStatus, String toStatus,
                                  Long fromAssignee, Long toAssignee)
    {
        ItTicketProcessLog processLog = new ItTicketProcessLog();
        processLog.setTicketId(ticketId);
        processLog.setAction(action);
        processLog.setActionName(actionName);
        processLog.setFromStatus(fromStatus);
        processLog.setToStatus(toStatus);
        processLog.setFromAssignee(fromAssignee);
        processLog.setToAssignee(toAssignee);
        processLog.setOperatorId(SecurityUtils.getUserId());
        processLog.setOperatorName(SecurityUtils.getUsername());
        processLog.setOperateTime(new Date());
        processLogService.insertProcessLog(processLog);
    }
}
