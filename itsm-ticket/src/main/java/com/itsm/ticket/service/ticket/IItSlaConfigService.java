package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItSlaConfig;

public interface IItSlaConfigService
{
    ItSlaConfig selectSlaConfigById(Long slaId);

    List<ItSlaConfig> selectSlaConfigList(ItSlaConfig slaConfig);

    int insertSlaConfig(ItSlaConfig slaConfig);

    int updateSlaConfig(ItSlaConfig slaConfig);

    int deleteSlaConfigByIds(Long[] slaIds);

    ItSlaConfig selectSlaConfigByCategoryId(Long categoryId, String priority);

    ItSlaConfig selectSlaConfigByCategoryAndPriority(Long categoryId, String priority);
}
