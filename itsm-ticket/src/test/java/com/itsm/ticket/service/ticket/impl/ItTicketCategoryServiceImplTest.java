package com.itsm.ticket.service.ticket.impl;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.itsm.common.exception.ServiceException;
import com.itsm.ticket.domain.ticket.ItTicketCategory;
import com.itsm.ticket.mapper.ticket.ItTicketCategoryMapper;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketCategoryServiceImplTest
{
    @Mock
    private ItTicketCategoryMapper categoryMapper;

    @Mock
    private ItTicketMapper ticketMapper;

    @InjectMocks
    private ItTicketCategoryServiceImpl categoryService;

    private ItTicketCategory testCategory;

    @BeforeEach
    void setUp()
    {
        testCategory = new ItTicketCategory();
        testCategory.setCategoryId(1L);
        testCategory.setCategoryName("网络故障");
        testCategory.setParentId(0L);
        testCategory.setAncestors("0");
    }

    @Test
    @DisplayName("BUG-010: 删除分类-无子分类且无工单引用时允许删除")
    void deleteCategoryByIds_NoChildrenNoTickets_Allowed()
    {
        when(categoryMapper.selectCategoryList(any())).thenReturn(Collections.emptyList());
        when(ticketMapper.countByCategoryId(1L)).thenReturn(0);
        when(categoryMapper.deleteCategoryById(1L)).thenReturn(1);
        categoryService.deleteCategoryByIds(new Long[]{1L});
        verify(categoryMapper).deleteCategoryById(1L);
    }

    @Test
    @DisplayName("BUG-010: 删除分类-存在子分类时抛出异常")
    void deleteCategoryByIds_HasChildren_ThrowsException()
    {
        ItTicketCategory child = new ItTicketCategory();
        child.setCategoryId(2L);
        child.setParentId(1L);
        when(categoryMapper.selectCategoryList(any())).thenReturn(Collections.singletonList(child));
        ServiceException exception = assertThrows(ServiceException.class, () ->
            categoryService.deleteCategoryByIds(new Long[]{1L}));
        assertTrue(exception.getMessage().contains("存在子分类"));
        verify(categoryMapper, never()).deleteCategoryById(anyLong());
    }

    @Test
    @DisplayName("BUG-010: 删除分类-存在工单引用时抛出异常")
    void deleteCategoryByIds_HasTickets_ThrowsException()
    {
        when(categoryMapper.selectCategoryList(any())).thenReturn(Collections.emptyList());
        when(ticketMapper.countByCategoryId(1L)).thenReturn(5);
        ServiceException exception = assertThrows(ServiceException.class, () ->
            categoryService.deleteCategoryByIds(new Long[]{1L}));
        assertTrue(exception.getMessage().contains("该分类下存在工单"));
        verify(categoryMapper, never()).deleteCategoryById(anyLong());
    }

    @Test
    @DisplayName("根据ID查询分类")
    void selectCategoryById_ReturnsCategory()
    {
        when(categoryMapper.selectCategoryById(1L)).thenReturn(testCategory);
        ItTicketCategory result = categoryService.selectCategoryById(1L);
        assertNotNull(result);
        assertEquals("网络故障", result.getCategoryName());
    }
}
