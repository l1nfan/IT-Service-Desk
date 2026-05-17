package com.itsm.generator.util;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import com.itsm.common.constant.Constants;

/**
 * FreeMarker配置工厂
 *
 * @author itsm
 */
public class FreeMarkerInitializer
{
    private static Configuration cfg;

    /**
     * 获取FreeMarker配置
     */
    public static Configuration getConfiguration()
    {
        if (cfg == null)
        {
            try
            {
                cfg = new Configuration(Configuration.VERSION_2_3_31);
                cfg.setClassForTemplateLoading(FreeMarkerInitializer.class, "/");
                cfg.setDefaultEncoding(Constants.UTF8);
                cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                cfg.setLogTemplateExceptions(false);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return cfg;
    }
}
