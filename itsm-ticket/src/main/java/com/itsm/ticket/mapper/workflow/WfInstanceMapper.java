package com.itsm.ticket.mapper.workflow;

import com.itsm.ticket.domain.workflow.WfInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfInstanceMapper
{
    WfInstance selectInstanceById(Long instanceId);

    WfInstance selectInstanceByBusinessId(@Param("businessId") Long businessId, @Param("businessType") String businessType);

    int insertInstance(WfInstance instance);

    int updateInstance(WfInstance instance);
}
