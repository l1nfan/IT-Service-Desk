package com.itsm.ticket.service.workflow;

import com.itsm.ticket.domain.workflow.WfInstance;

public interface IWfInstanceService
{
    WfInstance selectInstanceById(Long instanceId);

    WfInstance selectInstanceByBusinessId(Long businessId, String businessType);

    int insertInstance(WfInstance instance);

    int updateInstance(WfInstance instance);
}
