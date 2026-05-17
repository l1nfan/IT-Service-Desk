package com.itsm.ticket.mapper.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfTaskMapper
{
    WfTask selectTaskById(Long taskId);

    List<WfTask> selectTasksByInstanceId(Long instanceId);

    List<WfTask> selectPendingTasksByAssignee(Long assigneeId);

    List<WfTask> selectCompletedTasksByAssignee(Long assigneeId);

    int insertTask(WfTask task);

    int updateTask(WfTask task);

    List<WfTask> selectTasksByBusinessId(@Param("businessId") Long businessId, @Param("businessType") String businessType);
}
