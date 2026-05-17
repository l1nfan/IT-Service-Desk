package com.itsm.ticket.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.itsm.common.constant.UserConstants;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.ticket.domain.ticket.ItTicketCategory;
import com.itsm.ticket.service.ticket.IItTicketCategoryService;
import com.itsm.web.controller.itsm.ItTicketCategoryController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketCategoryControllerTest
{
    @Mock
    private IItTicketCategoryService categoryService;

    @InjectMocks
    private ItTicketCategoryController controller;

    private ItTicketCategory testCategory;

    @BeforeEach
    void setUp()
    {
        testCategory = new ItTicketCategory();
        testCategory.setCategoryId(1L);
        testCategory.setCategoryName("网络故障");
        testCategory.setParentId(0L);
        testCategory.setAncestors("0");
        testCategory.setOrderNum(1);
        testCategory.setStatus("0");
    }

    @Test
    @DisplayName("list返回树形结构")
    void list_ReturnsTreeStructure()
    {
        List<ItTicketCategory> categoryList = new ArrayList<>();
        categoryList.add(testCategory);

        ItTicketCategory child = new ItTicketCategory();
        child.setCategoryId(2L);
        child.setCategoryName("网络中断");
        child.setParentId(1L);
        categoryList.add(child);

        List<ItTicketCategory> tree = new ArrayList<>();
        tree.add(testCategory);

        when(categoryService.selectCategoryList(any(ItTicketCategory.class))).thenReturn(categoryList);
        when(categoryService.buildCategoryTree(categoryList)).thenReturn(tree);

        AjaxResult result = controller.list(testCategory);

        assertTrue(result.isSuccess());
        assertEquals(tree, result.get(AjaxResult.DATA_TAG));
        verify(categoryService).selectCategoryList(any(ItTicketCategory.class));
        verify(categoryService).buildCategoryTree(categoryList);
    }

    @Test
    @DisplayName("getInfo返回分类数据")
    void getInfo_ReturnsCategory()
    {
        when(categoryService.selectCategoryById(1L)).thenReturn(testCategory);

        AjaxResult result = controller.getInfo(1L);

        assertTrue(result.isSuccess());
        assertEquals(testCategory, result.get(AjaxResult.DATA_TAG));
        verify(categoryService).selectCategoryById(1L);
    }

    @Test
    @DisplayName("add创建分类-名称唯一时成功")
    void add_CreatesCategory_WhenNameUnique()
    {
        when(categoryService.checkCategoryNameUnique(any(ItTicketCategory.class))).thenReturn(UserConstants.UNIQUE);
        when(categoryService.insertCategory(any(ItTicketCategory.class))).thenReturn(1);

        AjaxResult result = controller.add(testCategory);

        assertTrue(result.isSuccess());
        verify(categoryService).checkCategoryNameUnique(any(ItTicketCategory.class));
        verify(categoryService).insertCategory(any(ItTicketCategory.class));
    }

    @Test
    @DisplayName("add创建分类-名称重复时失败")
    void add_Fails_WhenNameNotUnique()
    {
        when(categoryService.checkCategoryNameUnique(any(ItTicketCategory.class))).thenReturn(UserConstants.NOT_UNIQUE);

        AjaxResult result = controller.add(testCategory);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("分类名称已存在"));
        verify(categoryService).checkCategoryNameUnique(any(ItTicketCategory.class));
        verify(categoryService, never()).insertCategory(any(ItTicketCategory.class));
    }

    @Test
    @DisplayName("edit更新分类-名称唯一且非自引用时成功")
    void edit_UpdatesCategory_WhenNameUniqueAndNotSelfParent()
    {
        ItTicketCategory category = new ItTicketCategory();
        category.setCategoryId(1L);
        category.setCategoryName("网络故障-修改");
        category.setParentId(0L);

        when(categoryService.checkCategoryNameUnique(any(ItTicketCategory.class))).thenReturn(UserConstants.UNIQUE);
        when(categoryService.updateCategory(any(ItTicketCategory.class))).thenReturn(1);

        AjaxResult result = controller.edit(category);

        assertTrue(result.isSuccess());
        verify(categoryService).checkCategoryNameUnique(any(ItTicketCategory.class));
        verify(categoryService).updateCategory(any(ItTicketCategory.class));
    }

    @Test
    @DisplayName("edit更新分类-名称重复时失败")
    void edit_Fails_WhenNameNotUnique()
    {
        ItTicketCategory category = new ItTicketCategory();
        category.setCategoryId(1L);
        category.setCategoryName("已存在名称");
        category.setParentId(0L);

        when(categoryService.checkCategoryNameUnique(any(ItTicketCategory.class))).thenReturn(UserConstants.NOT_UNIQUE);

        AjaxResult result = controller.edit(category);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("分类名称已存在"));
        verify(categoryService).checkCategoryNameUnique(any(ItTicketCategory.class));
        verify(categoryService, never()).updateCategory(any(ItTicketCategory.class));
    }

    @Test
    @DisplayName("edit更新分类-上级分类为自己时失败")
    void edit_Fails_WhenSelfParent()
    {
        ItTicketCategory category = new ItTicketCategory();
        category.setCategoryId(1L);
        category.setCategoryName("网络故障");
        category.setParentId(1L);

        when(categoryService.checkCategoryNameUnique(any(ItTicketCategory.class))).thenReturn(UserConstants.UNIQUE);

        AjaxResult result = controller.edit(category);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("上级分类不能是自己"));
        verify(categoryService).checkCategoryNameUnique(any(ItTicketCategory.class));
        verify(categoryService, never()).updateCategory(any(ItTicketCategory.class));
    }

    @Test
    @DisplayName("remove删除分类并调用deleteCategoryByIds返回toAjax1")
    void remove_DeletesCategories()
    {
        Long[] categoryIds = new Long[]{1L, 2L};

        AjaxResult result = controller.remove(categoryIds);

        verify(categoryService).deleteCategoryByIds(categoryIds);
        assertTrue(result.isSuccess());
    }
}
