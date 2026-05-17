package com.itsm.ticket.service.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfTask;

public interface IWfTaskService
{
    WfTask selectTaskById(Long taskId);

    List<WfTask> selectTasksByInstanceId(Long instanceId);

    List<WfTask> selectPendingTasksByAssignee(Long assigneeId);

    List<WfTask> selectCompletedTasksByAssignee(Long assigneeId);

    int insertTask(WfTask task);

    int updateTask(WfTask task);

    List<WfTask> selectTasksByBusinessId(Long businessId, String businessType);
}
