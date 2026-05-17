package com.itsm.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itsm.common.core.page.PageDomain;
import com.itsm.common.core.page.TableSupport;
import com.itsm.common.utils.sql.SqlUtil;

public class PageUtils
{
    private static final ThreadLocal<Page<?>> PAGE_HOLDER = new ThreadLocal<>();

    public static <T> Page<T> startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();

        Page<T> page = new Page<>(pageNum, pageSize);
        if (reasonable)
        {
            page.setCurrent(Math.max(1, pageNum));
        }
        if (orderBy != null && !orderBy.isEmpty())
        {
            page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.asc(orderBy));
        }
        PAGE_HOLDER.set(page);
        return page;
    }

    @SuppressWarnings("unchecked")
    public static <T> Page<T> getPage()
    {
        return (Page<T>) PAGE_HOLDER.get();
    }

    public static void clearPage()
    {
        PAGE_HOLDER.remove();
    }
}
