package com.itsm.ticket.service.workflow.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.ticket.domain.workflow.WfTask;
import com.itsm.ticket.mapper.workflow.WfTaskMapper;
import com.itsm.ticket.service.workflow.IWfTaskService;

@Service
public class WfTaskServiceImpl implements IWfTaskService
{
    @Autowired
    private WfTaskMapper taskMapper;

    @Override
    public WfTask selectTaskById(Long taskId)
    {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    public List<WfTask> selectTasksByInstanceId(Long instanceId)
    {
        return taskMapper.selectTasksByInstanceId(instanceId);
    }

    @Override
    public List<WfTask> selectPendingTasksByAssignee(Long assigneeId)
    {
        return taskMapper.selectPendingTasksByAssignee(assigneeId);
    }

    @Override
    public List<WfTask> selectCompletedTasksByAssignee(Long assigneeId)
    {
        return taskMapper.selectCompletedTasksByAssignee(assigneeId);
    }

    @Override
    public int insertTask(WfTask task)
    {
        return taskMapper.insertTask(task);
    }

    @Override
    public int updateTask(WfTask task)
    {
        return taskMapper.updateTask(task);
    }

    @Override
    public List<WfTask> selectTasksByBusinessId(Long businessId, String businessType)
    {
        return taskMapper.selectTasksByBusinessId(businessId, businessType);
    }
}
