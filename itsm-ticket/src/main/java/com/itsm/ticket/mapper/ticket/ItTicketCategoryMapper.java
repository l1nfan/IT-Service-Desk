package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketCategory;

/**
 * 工单分类 数据层
 *
 * @author itsm
 */
public interface ItTicketCategoryMapper
{
    ItTicketCategory selectCategoryById(Long categoryId);

    List<ItTicketCategory> selectCategoryList(ItTicketCategory category);

    int insertCategory(ItTicketCategory category);

    int updateCategory(ItTicketCategory category);

    int deleteCategoryById(Long categoryId);

    int deleteCategoryByIds(Long[] categoryIds);

    ItTicketCategory checkCategoryNameUnique(String categoryName);
}
