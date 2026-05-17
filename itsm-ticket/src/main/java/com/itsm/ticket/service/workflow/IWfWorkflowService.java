package com.itsm.ticket.service.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfWorkflow;

public interface IWfWorkflowService
{
    WfWorkflow selectWorkflowById(Long workflowId);

    List<WfWorkflow> selectWorkflowList(WfWorkflow workflow);

    int insertWorkflow(WfWorkflow workflow);

    int updateWorkflow(WfWorkflow workflow);

    int deleteWorkflowByIds(Long[] workflowIds);

    WfWorkflow selectWorkflowByKey(String workflowKey);

    WfWorkflow selectDefaultWorkflow();
}
