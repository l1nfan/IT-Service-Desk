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
import com.itsm.ticket.domain.workflow.WfWorkflow;
import com.itsm.ticket.service.workflow.IWfWorkflowService;

@RestController
@RequestMapping("/itsm/workflow")
public class WfWorkflowController extends BaseController
{
    @Autowired
    private IWfWorkflowService workflowService;

    @PreAuthorize("@ss.hasPermi('itsm:workflow:list')")
    @GetMapping("/list")
    public TableDataInfo list(WfWorkflow workflow)
    {
        startPage();
        List<WfWorkflow> list = workflowService.selectWorkflowList(workflow);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:workflow:query')")
    @GetMapping(value = "/{workflowId}")
    public AjaxResult getInfo(@PathVariable Long workflowId)
    {
        return success(workflowService.selectWorkflowById(workflowId));
    }

    @Log(title = "流程管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('itsm:workflow:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WfWorkflow workflow)
    {
        return toAjax(workflowService.insertWorkflow(workflow));
    }

    @Log(title = "流程管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:workflow:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WfWorkflow workflow)
    {
        return toAjax(workflowService.updateWorkflow(workflow));
    }

    @Log(title = "流程管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('itsm:workflow:remove')")
    @DeleteMapping("/{workflowIds}")
    public AjaxResult remove(@PathVariable Long[] workflowIds)
    {
        return toAjax(workflowService.deleteWorkflowByIds(workflowIds));
    }
}
