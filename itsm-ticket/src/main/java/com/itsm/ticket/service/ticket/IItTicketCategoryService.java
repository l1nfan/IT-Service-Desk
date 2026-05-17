package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketCategory;

/**
 * 工单分类 服务层
 *
 * @author itsm
 */
public interface IItTicketCategoryService
{
    ItTicketCategory selectCategoryById(Long categoryId);

    List<ItTicketCategory> selectCategoryList(ItTicketCategory category);

    List<ItTicketCategory> buildCategoryTree(List<ItTicketCategory> list);

    int insertCategory(ItTicketCategory category);

    int updateCategory(ItTicketCategory category);

    void deleteCategoryByIds(Long[] categoryIds);

    boolean checkCategoryNameUnique(ItTicketCategory category);

    boolean hasChildByCategoryId(Long categoryId);
}
