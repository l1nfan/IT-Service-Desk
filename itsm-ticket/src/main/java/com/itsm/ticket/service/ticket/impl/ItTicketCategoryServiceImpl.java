package com.itsm.ticket.service.ticket.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.common.constant.UserConstants;
import com.itsm.common.exception.ServiceException;
import com.itsm.common.utils.StringUtils;
import com.itsm.ticket.domain.ticket.ItTicketCategory;
import com.itsm.ticket.mapper.ticket.ItTicketCategoryMapper;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import com.itsm.ticket.service.ticket.IItTicketCategoryService;

/**
 * 工单分类 服务层实现
 *
 * @author itsm
 */
@Service
public class ItTicketCategoryServiceImpl implements IItTicketCategoryService
{
    @Autowired
    private ItTicketCategoryMapper categoryMapper;

    @Autowired
    private ItTicketMapper ticketMapper;

    @Override
    public ItTicketCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public List<ItTicketCategory> selectCategoryList(ItTicketCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public List<ItTicketCategory> buildCategoryTree(List<ItTicketCategory> list)
    {
        List<ItTicketCategory> returnList = new ArrayList<>();
        List<Long> tempList = list.stream().map(ItTicketCategory::getCategoryId).collect(Collectors.toList());
        for (ItTicketCategory category : list)
        {
            if (!tempList.contains(category.getParentId()))
            {
                recursionFn(list, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = list;
        }
        return returnList;
    }

    @Override
    public int insertCategory(ItTicketCategory category)
    {
        ItTicketCategory info = categoryMapper.selectCategoryById(category.getParentId());
        if (!"0".equals(category.getParentId().toString()) && StringUtils.isNull(info))
        {
            throw new ServiceException("父分类不存在");
        }
        if (StringUtils.isNotNull(category.getCategoryId()) && "0".equals(category.getCategoryId().toString()))
        {
            throw new ServiceException("不能修改根分类");
        }
        if (StringUtils.isNotNull(info))
        {
            category.setAncestors(info.getAncestors() + "," + category.getParentId());
        }
        else
        {
            category.setAncestors("0");
        }
        return categoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(ItTicketCategory category)
    {
        ItTicketCategory newParent = categoryMapper.selectCategoryById(category.getParentId());
        ItTicketCategory oldCategory = categoryMapper.selectCategoryById(category.getCategoryId());
        if (StringUtils.isNotNull(newParent) && StringUtils.isNotNull(oldCategory))
        {
            String newAncestors = newParent.getAncestors() + "," + newParent.getCategoryId();
            String oldAncestors = oldCategory.getAncestors();
            category.setAncestors(newAncestors);
            updateCategoryChildren(category.getCategoryId(), newAncestors, oldAncestors);
        }
        return categoryMapper.updateCategory(category);
    }

    private void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors)
    {
        List<ItTicketCategory> children = categoryMapper.selectCategoryList(new ItTicketCategory());
        for (ItTicketCategory child : children)
        {
            if (child.getAncestors() != null && child.getAncestors().startsWith(oldAncestors))
            {
                child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
                categoryMapper.updateCategory(child);
            }
        }
    }

    @Override
    public void deleteCategoryByIds(Long[] categoryIds)
    {
        for (Long categoryId : categoryIds)
        {
            if (hasChildByCategoryId(categoryId))
            {
                throw new ServiceException("存在子分类，不允许删除");
            }
            if (ticketMapper.countByCategoryId(categoryId) > 0)
            {
                throw new ServiceException("该分类下存在工单，不允许删除");
            }
            categoryMapper.deleteCategoryById(categoryId);
        }
    }

    @Override
    public boolean checkCategoryNameUnique(ItTicketCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        ItTicketCategory info = categoryMapper.checkCategoryNameUnique(category.getCategoryName());
        if (StringUtils.isNotNull(info) && info.getCategoryId().longValue() != categoryId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean hasChildByCategoryId(Long categoryId)
    {
        int result = categoryMapper.selectCategoryList(new ItTicketCategory() {{
            setParentId(categoryId);
        }}).size();
        return result > 0;
    }

    private void recursionFn(List<ItTicketCategory> list, ItTicketCategory t)
    {
        List<ItTicketCategory> childList = getChildList(list, t);
        t.getParams().put("children", childList);
        for (ItTicketCategory tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    private List<ItTicketCategory> getChildList(List<ItTicketCategory> list, ItTicketCategory t)
    {
        List<ItTicketCategory> tlist = new ArrayList<>();
        for (ItTicketCategory n : list)
        {
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getCategoryId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<ItTicketCategory> list, ItTicketCategory t)
    {
        return getChildList(list, t).size() > 0;
    }
}
