package com.itsm.web.controller.itsm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itsm.common.core.controller.BaseController;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.ticket.domain.workflow.WfTask;
import com.itsm.ticket.service.workflow.IWfTaskService;

@RestController
@RequestMapping("/itsm/task")
public class WfTaskController extends BaseController
{
    @Autowired
    private IWfTaskService taskService;

    @PreAuthorize("@ss.hasPermi('itsm:ticket:list')")
    @GetMapping("/pending")
    public AjaxResult pending()
    {
        List<WfTask> list = taskService.selectPendingTasksByAssignee(SecurityUtils.getUserId());
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:list')")
    @GetMapping("/completed")
    public AjaxResult completed()
    {
        List<WfTask> list = taskService.selectCompletedTasksByAssignee(SecurityUtils.getUserId());
        return success(list);
    }
}
