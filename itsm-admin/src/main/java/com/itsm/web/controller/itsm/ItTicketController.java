package com.itsm.web.controller.itsm;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.itsm.common.utils.poi.ExcelUtil;
import com.itsm.ticket.domain.ticket.ItSlaConfig;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.ItTicketLog;
import com.itsm.ticket.domain.ticket.ItTicketProcessLog;
import com.itsm.ticket.domain.ticket.ItSlaRecord;
import com.itsm.ticket.domain.ticket.TicketTransitDTO;
import com.itsm.ticket.domain.workflow.WfTransition;
import com.itsm.ticket.service.ticket.IItTicketService;
import com.itsm.ticket.service.ticket.IItTicketLogService;
import com.itsm.ticket.service.ticket.IItTicketProcessLogService;
import com.itsm.ticket.service.ticket.IItSlaConfigService;
import com.itsm.ticket.service.ticket.IItSlaRecordService;

@RestController
@RequestMapping("/itsm/ticket")
public class ItTicketController extends BaseController
{
    @Autowired
    private IItTicketService ticketService;

    @Autowired
    private IItTicketLogService ticketLogService;

    @Autowired
    private IItTicketProcessLogService processLogService;

    @Autowired
    private IItSlaConfigService slaConfigService;

    @Autowired
    private IItSlaRecordService slaRecordService;

    @PreAuthorize("@ss.hasPermi('itsm:ticket:list')")
    @GetMapping("/list")
    public TableDataInfo list(ItTicket ticket)
    {
        startPage();
        List<ItTicket> list = ticketService.selectTicketList(ticket);
        return getDataTable(list);
    }

    @Log(title = "工单管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ItTicket ticket)
    {
        List<ItTicket> list = ticketService.selectTicketList(ticket);
        ExcelUtil<ItTicket> util = new ExcelUtil<>(ItTicket.class);
        util.exportExcel(response, list, "工单数据");
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/{ticketId}")
    public AjaxResult getInfo(@PathVariable Long ticketId)
    {
        return success(ticketService.selectTicketById(ticketId));
    }

    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ItTicket ticket)
    {
        int rows = ticketService.insertTicket(ticket);
        AjaxResult ajax = toAjax(rows);
        if (rows > 0)
        {
            ItSlaConfig slaConfig = slaConfigService.selectSlaConfigByCategoryAndPriority(
                ticket.getCategoryId(), mapPriority(ticket.getPriority()));
            if (slaConfig == null)
            {
                ajax.put("warning", "该工单分类+优先级未配置SLA，无法进行SLA监控");
            }
        }
        return ajax;
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ItTicket ticket)
    {
        return toAjax(ticketService.updateTicket(ticket));
    }

    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:remove')")
    @DeleteMapping("/{ticketIds}")
    public AjaxResult remove(@PathVariable Long[] ticketIds)
    {
        ticketService.deleteTicketByIds(ticketIds);
        return toAjax(1);
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:assign')")
    @PutMapping("/assign")
    public AjaxResult assign(@Validated @RequestBody ItTicket ticket)
    {
        return toAjax(ticketService.assignTicket(ticket));
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:assign')")
    @PutMapping("/reassign")
    public AjaxResult reassign(@Validated @RequestBody ItTicket ticket)
    {
        return toAjax(ticketService.reassignTicket(ticket));
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:approve')")
    @PutMapping("/approve")
    public AjaxResult approve(@Validated @RequestBody ItTicket ticket)
    {
        return toAjax(ticketService.approveTicket(ticket));
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:edit')")
    @PutMapping("/transit")
    public AjaxResult transit(@Validated @RequestBody TicketTransitDTO dto)
    {
        return toAjax(ticketService.transitTicket(dto));
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/transitions/{ticketId}")
    public AjaxResult getAvailableTransitions(@PathVariable Long ticketId)
    {
        return success(ticketService.getAvailableTransitions(ticketId));
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/workflow-transitions/{ticketId}")
    public AjaxResult getAvailableWorkflowTransitions(@PathVariable Long ticketId)
    {
        return success(ticketService.getAvailableWorkflowTransitions(ticketId));
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/process-log/{ticketId}")
    public AjaxResult getProcessLogs(@PathVariable Long ticketId)
    {
        List<ItTicketProcessLog> list = processLogService.selectProcessLogsByTicketId(ticketId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/log/{ticketId}")
    public AjaxResult getTicketLogs(@PathVariable Long ticketId)
    {
        List<ItTicketLog> list = ticketLogService.selectLogsByTicketId(ticketId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping(value = "/sla/{ticketId}")
    public AjaxResult getSlaRecord(@PathVariable Long ticketId)
    {
        ItSlaRecord record = slaRecordService.selectSlaRecordByTicketId(ticketId);
        return success(record);
    }

    private String mapPriority(Integer priority)
    {
        if (priority == null) return "MEDIUM";
        return switch (priority)
        {
            case 1 -> "URGENT";
            case 2 -> "HIGH";
            case 3 -> "MEDIUM";
            case 4 -> "LOW";
            default -> "MEDIUM";
        };
    }
}
