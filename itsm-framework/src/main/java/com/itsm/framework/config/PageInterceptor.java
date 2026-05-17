package com.itsm.framework.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itsm.common.utils.PageUtils;

@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PageInterceptor implements Interceptor
{
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        Page<?> page = PageUtils.getPage();
        if (page == null)
        {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        if (ms.getSqlCommandType() != SqlCommandType.SELECT)
        {
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        Connection connection = null;
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        try
        {
            connection = ms.getConfiguration().getEnvironment().getDataSource().getConnection();
            String countSql = "SELECT COUNT(0) FROM (" + originalSql + ") _page_count";
            countStmt = connection.prepareStatement(countSql);
            setParameters(countStmt, ms, boundSql, parameter);
            rs = countStmt.executeQuery();
            long total = 0;
            if (rs.next())
            {
                total = rs.getLong(1);
            }
            page.setTotal(total);

            long offset = (page.getCurrent() - 1) * page.getSize();
            String pageSql = originalSql + " LIMIT " + offset + "," + page.getSize();
            BoundSql pageBoundSql = copyBoundSql(ms, boundSql, pageSql);

            org.apache.ibatis.cache.CacheKey cacheKey = executor.createCacheKey(ms, parameter, RowBounds.DEFAULT, pageBoundSql);
            List<?> result = executor.query(ms, parameter, RowBounds.DEFAULT, (ResultHandler<?>) args[3], cacheKey, pageBoundSql);
            page.setRecords((List) result);
            return result;
        }
        finally
        {
            if (rs != null) { try { rs.close(); } catch (Exception ignored) {} }
            if (countStmt != null) { try { countStmt.close(); } catch (Exception ignored) {} }
            if (connection != null) { try { connection.close(); } catch (Exception ignored) {} }
        }
    }

    private void setParameters(PreparedStatement ps, MappedStatement ms, BoundSql boundSql, Object parameterObject)
            throws java.sql.SQLException
    {
        org.apache.ibatis.mapping.ParameterMapping[] mappings = boundSql.getParameterMappings().toArray(new org.apache.ibatis.mapping.ParameterMapping[0]);
        if (mappings.length == 0)
        {
            return;
        }
        org.apache.ibatis.scripting.defaults.DefaultParameterHandler handler = new org.apache.ibatis.scripting.defaults.DefaultParameterHandler(ms, parameterObject, boundSql);
        handler.setParameters(ps);
    }

    private BoundSql copyBoundSql(MappedStatement ms, BoundSql boundSql, String newSql)
    {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (java.util.Map.Entry<String, Object> entry : boundSql.getAdditionalParameters().entrySet())
        {
            newBoundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return newBoundSql;
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
    }
}
