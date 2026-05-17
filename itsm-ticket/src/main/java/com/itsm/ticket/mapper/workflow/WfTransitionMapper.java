package com.itsm.ticket.mapper.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfTransition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfTransitionMapper
{
    WfTransition selectTransitionById(Long transitionId);

    List<WfTransition> selectTransitionsByWorkflowId(Long workflowId);

    List<WfTransition> selectTransitionsByFromNode(Long workflowId, String fromNodeKey);

    int insertTransition(WfTransition transition);

    int insertTransitions(List<WfTransition> transitions);

    int updateTransition(WfTransition transition);

    int deleteTransitionById(Long transitionId);

    int deleteTransitionsByWorkflowId(Long workflowId);
}
