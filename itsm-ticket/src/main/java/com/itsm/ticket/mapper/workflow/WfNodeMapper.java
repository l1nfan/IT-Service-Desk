package com.itsm.ticket.mapper.workflow;

import java.util.List;
import com.itsm.ticket.domain.workflow.WfNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfNodeMapper
{
    WfNode selectNodeById(Long nodeId);

    List<WfNode> selectNodesByWorkflowId(Long workflowId);

    WfNode selectNodeByWorkflowIdAndNodeKey(@org.apache.ibatis.annotations.Param("workflowId") Long workflowId, @org.apache.ibatis.annotations.Param("nodeKey") String nodeKey);

    int insertNode(WfNode node);

    int insertNodes(List<WfNode> nodes);

    int updateNode(WfNode node);

    int deleteNodeById(Long nodeId);

    int deleteNodesByWorkflowId(Long workflowId);
}
