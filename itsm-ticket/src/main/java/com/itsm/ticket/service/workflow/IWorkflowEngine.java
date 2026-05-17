package com.itsm.ticket.service.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfTransition;

public interface IWorkflowEngine
{
    Long startProcess(Long workflowId, Long businessId, String businessType, Long initiatorId, String initiatorName);

    void transit(Long instanceId, String action, Long operatorId, String operatorName, String comment);

    List<WfTransition> getAvailableTransitions(Long businessId, String businessType);

    void terminateProcess(Long instanceId);

    Long getWorkflowIdByCategory(Long categoryId);
}
