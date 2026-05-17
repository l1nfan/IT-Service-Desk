package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItSlaConfigMapper
{
    ItSlaConfig selectSlaConfigById(Long slaId);

    List<ItSlaConfig> selectSlaConfigList(ItSlaConfig slaConfig);

    int insertSlaConfig(ItSlaConfig slaConfig);

    int updateSlaConfig(ItSlaConfig slaConfig);

    int deleteSlaConfigById(Long slaId);

    int deleteSlaConfigByIds(Long[] slaIds);

    ItSlaConfig selectSlaConfigByCategoryId(@org.apache.ibatis.annotations.Param("categoryId") Long categoryId, @org.apache.ibatis.annotations.Param("priority") String priority);

    ItSlaConfig selectSlaConfigByCategoryAndPriority(@org.apache.ibatis.annotations.Param("categoryId") Long categoryId, @org.apache.ibatis.annotations.Param("priority") String priority);
}
