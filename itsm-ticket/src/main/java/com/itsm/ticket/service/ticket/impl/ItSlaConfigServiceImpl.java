package com.itsm.ticket.service.ticket.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.common.exception.ServiceException;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import com.itsm.ticket.mapper.ticket.ItSlaConfigMapper;
import com.itsm.ticket.service.ticket.IItSlaConfigService;

@Service
public class ItSlaConfigServiceImpl implements IItSlaConfigService
{
    @Autowired
    private ItSlaConfigMapper slaConfigMapper;

    @Override
    public ItSlaConfig selectSlaConfigById(Long slaId)
    {
        return slaConfigMapper.selectSlaConfigById(slaId);
    }

    @Override
    public List<ItSlaConfig> selectSlaConfigList(ItSlaConfig slaConfig)
    {
        return slaConfigMapper.selectSlaConfigList(slaConfig);
    }

    @Override
    public int insertSlaConfig(ItSlaConfig slaConfig)
    {
        if (slaConfig.getCategoryId() != null && slaConfig.getPriority() != null)
        {
            ItSlaConfig existing = slaConfigMapper.selectSlaConfigByCategoryAndPriority(
                slaConfig.getCategoryId(), slaConfig.getPriority());
            if (existing != null)
            {
                throw new ServiceException("该分类和优先级已存在SLA配置");
            }
        }
        slaConfig.setCreateBy(SecurityUtils.getUsername());
        return slaConfigMapper.insertSlaConfig(slaConfig);
    }

    @Override
    public int updateSlaConfig(ItSlaConfig slaConfig)
    {
        if (slaConfig.getCategoryId() != null && slaConfig.getPriority() != null)
        {
            ItSlaConfig existing = slaConfigMapper.selectSlaConfigByCategoryAndPriority(
                slaConfig.getCategoryId(), slaConfig.getPriority());
            if (existing != null && !existing.getSlaId().equals(slaConfig.getSlaId()))
            {
                throw new ServiceException("该分类和优先级已存在其他SLA配置");
            }
        }
        slaConfig.setUpdateBy(SecurityUtils.getUsername());
        return slaConfigMapper.updateSlaConfig(slaConfig);
    }

    @Override
    public int deleteSlaConfigByIds(Long[] slaIds)
    {
        return slaConfigMapper.deleteSlaConfigByIds(slaIds);
    }

    @Override
    public ItSlaConfig selectSlaConfigByCategoryId(Long categoryId, String priority)
    {
        return slaConfigMapper.selectSlaConfigByCategoryId(categoryId, priority);
    }

    @Override
    public ItSlaConfig selectSlaConfigByCategoryAndPriority(Long categoryId, String priority)
    {
        return slaConfigMapper.selectSlaConfigByCategoryAndPriority(categoryId, priority);
    }
}
