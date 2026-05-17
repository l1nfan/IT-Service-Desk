package com.itsm.ticket.mapper.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfWorkflow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfWorkflowMapper
{
    WfWorkflow selectWorkflowById(Long workflowId);

    List<WfWorkflow> selectWorkflowList(WfWorkflow workflow);

    int insertWorkflow(WfWorkflow workflow);

    int updateWorkflow(WfWorkflow workflow);

    int deleteWorkflowById(Long workflowId);

    int deleteWorkflowByIds(Long[] workflowIds);

    WfWorkflow selectWorkflowByKey(String workflowKey);

    WfWorkflow selectDefaultWorkflow();
}
