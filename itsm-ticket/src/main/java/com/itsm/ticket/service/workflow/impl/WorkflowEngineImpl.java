package com.itsm.ticket.service.workflow.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itsm.common.exception.ServiceException;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.ItTicketCategory;
import com.itsm.ticket.domain.workflow.WfInstance;
import com.itsm.ticket.domain.workflow.WfNode;
import com.itsm.ticket.domain.workflow.WfTask;
import com.itsm.ticket.domain.workflow.WfTransition;
import com.itsm.ticket.domain.workflow.WfWorkflow;
import com.itsm.ticket.mapper.ticket.ItTicketCategoryMapper;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import com.itsm.ticket.mapper.workflow.WfNodeMapper;
import com.itsm.ticket.mapper.workflow.WfTransitionMapper;
import com.itsm.ticket.service.workflow.IWorkflowEngine;
import com.itsm.ticket.service.workflow.IWfInstanceService;
import com.itsm.ticket.service.workflow.IWfTaskService;
import com.itsm.ticket.service.workflow.IWfWorkflowService;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.enums.TicketTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WorkflowEngineImpl implements IWorkflowEngine
{
    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineImpl.class);
    @Autowired
    private IWfWorkflowService workflowService;

    @Autowired
    private IWfInstanceService instanceService;

    @Autowired
    private IWfTaskService taskService;

    @Autowired
    private WfTransitionMapper transitionMapper;

    @Autowired
    private WfNodeMapper nodeMapper;

    @Autowired
    private ItTicketCategoryMapper categoryMapper;

    @Autowired
    private ItTicketMapper ticketMapper;

    @Override
    @Transactional
    public Long startProcess(Long workflowId, Long businessId, String businessType, Long initiatorId, String initiatorName)
    {
        if (workflowId == null)
        {
            WfWorkflow defaultWorkflow = workflowService.selectDefaultWorkflow();
            if (defaultWorkflow == null)
            {
                throw new ServiceException("未找到默认流程定义");
            }
            workflowId = defaultWorkflow.getWorkflowId();
        }

        WfWorkflow workflow = workflowService.selectWorkflowById(workflowId);
        if (workflow == null)
        {
            throw new ServiceException("流程定义不存在: " + workflowId);
        }
        if (!"0".equals(workflow.getStatus()))
        {
            throw new ServiceException("流程定义已停用: " + workflow.getWorkflowName());
        }

        WfInstance instance = new WfInstance();
        instance.setWorkflowId(workflowId);
        instance.setBusinessId(businessId);
        instance.setBusinessType(businessType);
        instance.setCurrentNodeKey("DRAFT");
        instance.setStatus("RUNNING");
        instance.setStartTime(new Date());
        instance.setCreateBy(initiatorName);
        instanceService.insertInstance(instance);

        WfTask initTask = new WfTask();
        initTask.setInstanceId(instance.getInstanceId());
        initTask.setWorkflowId(workflowId);
        initTask.setBusinessId(businessId);
        initTask.setNodeKey("DRAFT");
        initTask.setNodeName("草稿");
        initTask.setAssigneeId(initiatorId);
        initTask.setAssigneeName(initiatorName);
        initTask.setStatus("PENDING");
        taskService.insertTask(initTask);

        return instance.getInstanceId();
    }

    @Override
    @Transactional
    public void transit(Long instanceId, String action, Long operatorId, String operatorName, String comment)
    {
        WfInstance instance = instanceService.selectInstanceById(instanceId);
        if (instance == null)
        {
            throw new ServiceException("流程实例不存在: " + instanceId);
        }
        if (!"RUNNING".equals(instance.getStatus()))
        {
            throw new ServiceException("流程实例已结束，无法流转");
        }

        List<WfTransition> transitions = transitionMapper.selectTransitionsByFromNode(
            instance.getWorkflowId(), instance.getCurrentNodeKey());

        WfTransition matchedTransition = null;
        for (WfTransition t : transitions)
        {
            if (t.getAction().equals(action))
            {
                matchedTransition = t;
                break;
            }
        }

        if (matchedTransition == null)
        {
            throw new ServiceException("当前节点不允许执行操作: " + action);
        }

        completeCurrentTask(instanceId, operatorId, action, comment);

        String fromNodeKey = instance.getCurrentNodeKey();
        String toNodeKey = matchedTransition.getToNodeKey();

        instance.setCurrentNodeKey(toNodeKey);

        if ("CLOSED".equals(toNodeKey) || "CANCELLED".equals(toNodeKey))
        {
            instance.setStatus("COMPLETED");
            instance.setEndTime(new Date());
        }

        instanceService.updateInstance(instance);

        if (!"CLOSED".equals(toNodeKey) && !"CANCELLED".equals(toNodeKey))
        {
            createPendingTask(instance, toNodeKey);
        }

        log.info("流程流转: instanceId={}, {} -> {}, action={}, operator={}",
            instanceId, fromNodeKey, toNodeKey, action, operatorName);
    }

    @Override
    public List<WfTransition> getAvailableTransitions(Long businessId, String businessType)
    {
        WfInstance instance = instanceService.selectInstanceByBusinessId(businessId, businessType);
        if (instance == null || !"RUNNING".equals(instance.getStatus()))
        {
            return new ArrayList<>();
        }

        return transitionMapper.selectTransitionsByFromNode(
            instance.getWorkflowId(), instance.getCurrentNodeKey());
    }

    @Override
    @Transactional
    public void terminateProcess(Long instanceId)
    {
        WfInstance instance = instanceService.selectInstanceById(instanceId);
        if (instance == null)
        {
            return;
        }

        instance.setStatus("TERMINATED");
        instance.setEndTime(new Date());
        instanceService.updateInstance(instance);

        List<WfTask> pendingTasks = taskService.selectTasksByInstanceId(instanceId);
        for (WfTask task : pendingTasks)
        {
            if ("PENDING".equals(task.getStatus()))
            {
                task.setStatus("CANCELLED");
                taskService.updateTask(task);
            }
        }
    }

    @Override
    public Long getWorkflowIdByCategory(Long categoryId)
    {
        if (categoryId == null)
        {
            return null;
        }
        ItTicketCategory category = categoryMapper.selectCategoryById(categoryId);
        if (category != null && category.getWorkflowId() != null)
        {
            return category.getWorkflowId();
        }
        return null;
    }

    private void completeCurrentTask(Long instanceId, Long operatorId, String action, String comment)
    {
        List<WfTask> tasks = taskService.selectTasksByInstanceId(instanceId);
        for (WfTask task : tasks)
        {
            if ("PENDING".equals(task.getStatus())
                && task.getAssigneeId() != null
                && task.getAssigneeId().equals(operatorId))
            {
                task.setAction(action);
                task.setComment(comment);
                task.setStatus("COMPLETED");
                task.setCompleteTime(new Date());
                taskService.updateTask(task);
                return;
            }
        }
    }

    private void createPendingTask(WfInstance instance, String nodeKey)
    {
        WfNode node = nodeMapper.selectNodeByWorkflowIdAndNodeKey(
            instance.getWorkflowId(), nodeKey);

        Long assigneeId = null;
        String assigneeName = null;

        if (node != null && node.getAssigneeType() != null)
        {
            switch (node.getAssigneeType())
            {
                case "INITIATOR":
                    ItTicket ticket = ticketMapper.selectTicketById(instance.getBusinessId());
                    if (ticket != null)
                    {
                        assigneeId = ticket.getCreatorId();
                        assigneeName = ticket.getCreateBy();
                    }
                    break;
                case "USER":
                    if (node.getAssigneeValue() != null)
                    {
                        try
                        {
                            assigneeId = Long.valueOf(node.getAssigneeValue());
                            assigneeName = "用户(" + node.getAssigneeValue() + ")";
                        }
                        catch (NumberFormatException e)
                        {
                            log.warn("节点配置的assignee_value无法解析为Long: {}", node.getAssigneeValue());
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        WfTask task = new WfTask();
        task.setInstanceId(instance.getInstanceId());
        task.setWorkflowId(instance.getWorkflowId());
        task.setBusinessId(instance.getBusinessId());
        task.setNodeKey(nodeKey);
        task.setNodeName(getNodeName(nodeKey));
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assigneeName);
        task.setStatus("PENDING");
        taskService.insertTask(task);

        log.info("创建待办任务: instanceId={}, nodeKey={}, assigneeId={}",
            instance.getInstanceId(), nodeKey, assigneeId);
    }

    private String getNodeName(String nodeKey)
    {
        try
        {
            TicketStatus status = TicketStatus.fromCode(nodeKey);
            return status.getDesc();
        }
        catch (Exception e)
        {
            return nodeKey;
        }
    }
}
