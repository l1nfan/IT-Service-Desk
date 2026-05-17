package com.itsm.ticket.service.workflow.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itsm.ticket.domain.workflow.WfNode;
import com.itsm.ticket.domain.workflow.WfTransition;
import com.itsm.ticket.domain.workflow.WfWorkflow;
import com.itsm.ticket.mapper.workflow.WfNodeMapper;
import com.itsm.ticket.mapper.workflow.WfTransitionMapper;
import com.itsm.ticket.mapper.workflow.WfWorkflowMapper;
import com.itsm.ticket.service.workflow.IWfWorkflowService;

@Service
public class WfWorkflowServiceImpl implements IWfWorkflowService
{
    @Autowired
    private WfWorkflowMapper workflowMapper;

    @Autowired
    private WfNodeMapper nodeMapper;

    @Autowired
    private WfTransitionMapper transitionMapper;

    @Override
    public WfWorkflow selectWorkflowById(Long workflowId)
    {
        return workflowMapper.selectWorkflowById(workflowId);
    }

    @Override
    public List<WfWorkflow> selectWorkflowList(WfWorkflow workflow)
    {
        return workflowMapper.selectWorkflowList(workflow);
    }

    @Transactional
    @Override
    public int insertWorkflow(WfWorkflow workflow)
    {
        int rows = workflowMapper.insertWorkflow(workflow);
        if (rows > 0)
        {
            insertNodesAndTransitions(workflow);
        }
        return rows;
    }

    @Transactional
    @Override
    public int updateWorkflow(WfWorkflow workflow)
    {
        nodeMapper.deleteNodesByWorkflowId(workflow.getWorkflowId());
        transitionMapper.deleteTransitionsByWorkflowId(workflow.getWorkflowId());
        int rows = workflowMapper.updateWorkflow(workflow);
        if (rows > 0)
        {
            insertNodesAndTransitions(workflow);
        }
        return rows;
    }

    @Transactional
    @Override
    public int deleteWorkflowByIds(Long[] workflowIds)
    {
        int rows = 0;
        for (Long workflowId : workflowIds)
        {
            nodeMapper.deleteNodesByWorkflowId(workflowId);
            transitionMapper.deleteTransitionsByWorkflowId(workflowId);
            rows += workflowMapper.deleteWorkflowById(workflowId);
        }
        return rows;
    }

    @Override
    public WfWorkflow selectWorkflowByKey(String workflowKey)
    {
        return workflowMapper.selectWorkflowByKey(workflowKey);
    }

    @Override
    public WfWorkflow selectDefaultWorkflow()
    {
        return workflowMapper.selectDefaultWorkflow();
    }

    private void insertNodesAndTransitions(WfWorkflow workflow)
    {
        List<WfNode> nodes = workflow.getNodes();
        if (nodes != null && !nodes.isEmpty())
        {
            for (WfNode node : nodes)
            {
                node.setWorkflowId(workflow.getWorkflowId());
            }
            nodeMapper.insertNodes(nodes);
        }
        List<WfTransition> transitions = workflow.getTransitions();
        if (transitions != null && !transitions.isEmpty())
        {
            for (WfTransition transition : transitions)
            {
                transition.setWorkflowId(workflow.getWorkflowId());
            }
            transitionMapper.insertTransitions(transitions);
        }
    }
}
