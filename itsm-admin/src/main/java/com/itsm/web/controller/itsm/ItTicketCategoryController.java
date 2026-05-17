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
import com.itsm.common.enums.BusinessType;
import com.itsm.common.utils.StringUtils;
import com.itsm.ticket.domain.ticket.ItTicketCategory;
import com.itsm.ticket.service.ticket.IItTicketCategoryService;

/**
 * 工单分类 信息操作处理
 *
 * @author itsm
 */
@RestController
@RequestMapping("/itsm/category")
public class ItTicketCategoryController extends BaseController
{
    @Autowired
    private IItTicketCategoryService categoryService;

    @PreAuthorize("@ss.hasPermi('itsm:category:list')")
    @GetMapping("/list")
    public AjaxResult list(ItTicketCategory category)
    {
        List<ItTicketCategory> list = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTree(list));
    }

    @PreAuthorize("@ss.hasPermi('itsm:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    @Log(title = "工单分类", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('itsm:category:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ItTicketCategory category)
    {
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("新增分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(categoryService.insertCategory(category));
    }

    @Log(title = "工单分类", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('itsm:category:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ItTicketCategory category)
    {
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        if (category.getParentId().equals(category.getCategoryId()))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，上级分类不能是自己");
        }
        return toAjax(categoryService.updateCategory(category));
    }

    @Log(title = "工单分类", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('itsm:category:remove')")
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        categoryService.deleteCategoryByIds(categoryIds);
        return toAjax(1);
    }
}
