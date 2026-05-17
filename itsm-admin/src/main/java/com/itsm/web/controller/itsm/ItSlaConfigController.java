package com.itsm.web.controller.itsm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itsm.common.annotation.Log;
import com.itsm.common.core.controller.BaseController;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.core.page.TableDataInfo;
import com.itsm.common.enums.BusinessType;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import com.itsm.ticket.service.ticket.IItSlaConfigService;

@RestController
@RequestMapping("/itsm/sla")
public class ItSlaConfigController extends BaseController
{
    @Autowired
    private IItSlaConfigService slaConfigService;

    @PreAuthorize("@ss.hasPermi('itsm:sla:list')")
    @GetMapping("/list")
    public TableDataInfo list(ItSlaConfig slaConfig)
    {
        startPage();
        List<ItSlaConfig> list = slaConfigService.selectSlaConfigList(slaConfig);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:sla:query')")
    @GetMapping(value = "/{slaId}")
    public AjaxResult getInfo(@PathVariable Long slaId)
    {
        return success(slaConfigService.selectSlaConfigById(slaId));
    }

    @Log(title = "SLA管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('itsm:sla:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ItSlaConfig slaConfig)
    {
        return toAjax(slaConfigService.insertSlaConfig(slaConfig));
    }

    @Log(title = "SLA管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:sla:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ItSlaConfig slaConfig)
    {
        return toAjax(slaConfigService.updateSlaConfig(slaConfig));
    }

    @Log(title = "SLA管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('itsm:sla:remove')")
    @DeleteMapping("/{slaIds}")
    public AjaxResult remove(@PathVariable Long[] slaIds)
    {
        return toAjax(slaConfigService.deleteSlaConfigByIds(slaIds));
    }
}
