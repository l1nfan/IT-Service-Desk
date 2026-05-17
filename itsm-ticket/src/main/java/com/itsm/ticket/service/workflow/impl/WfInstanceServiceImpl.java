package com.itsm.ticket.service.workflow.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.workflow.WfInstance;
import com.itsm.ticket.mapper.workflow.WfInstanceMapper;
import com.itsm.ticket.service.workflow.IWfInstanceService;

@Service
public class WfInstanceServiceImpl implements IWfInstanceService
{
    @Autowired
    private WfInstanceMapper instanceMapper;

    @Override
    public WfInstance selectInstanceById(Long instanceId)
    {
        return instanceMapper.selectInstanceById(instanceId);
    }

    @Override
    public WfInstance selectInstanceByBusinessId(Long businessId, String businessType)
    {
        return instanceMapper.selectInstanceByBusinessId(businessId, businessType);
    }

    @Override
    public int insertInstance(WfInstance instance)
    {
        return instanceMapper.insertInstance(instance);
    }

    @Override
    public int updateInstance(WfInstance instance)
    {
        return instanceMapper.updateInstance(instance);
    }
}
