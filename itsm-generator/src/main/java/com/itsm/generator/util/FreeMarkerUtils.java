package com.itsm.generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.itsm.common.constant.GenConstants;
import com.itsm.common.utils.DateUtils;
import com.itsm.common.utils.StringUtils;
import com.itsm.generator.domain.GenTable;
import com.itsm.generator.domain.GenTableColumn;

/**
 * 模板处理工具类
 *
 * @author itsm
 */
public class FreeMarkerUtils
{
    private static final String PROJECT_PATH = "main/java";

    private static final String MYBATIS_PATH = "main/resources/mapper";

    private static final String DEFAULT_PARENT_MENU_ID = "3";

    private static final String ELEMENT_PLUS = "element-plus";

    private static final String ELEMENT_PLUS_TYPESSRIPT = "element-plus-typescript";

    /**
     * 设置模板变量信息
     *
     * @return 模板上下文
     */
    public static Map<String, Object> prepareContext(GenTable genTable)
    {
        String moduleName = genTable.getModuleName();
        String businessName = genTable.getBusinessName();
        String packageName = genTable.getPackageName();
        String tplCategory = genTable.getTplCategory();
        String functionName = genTable.getFunctionName();

        Map<String, Object> context = new HashMap<>();
        context.put("tplCategory", genTable.getTplCategory());
        context.put("tableName", genTable.getTableName());
        context.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
        context.put("ClassName", genTable.getClassName());
        context.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        context.put("moduleName", genTable.getModuleName());
        context.put("BusinessName", StringUtils.capitalize(genTable.getBusinessName()));
        context.put("businessName", genTable.getBusinessName());
        context.put("basePackage", getPackagePrefix(packageName));
        context.put("packageName", packageName);
        context.put("author", genTable.getFunctionAuthor());
        context.put("colSpan", getColSpan(genTable.getFormColNum()));
        context.put("datetime", DateUtils.getDate());
        context.put("pkColumn", genTable.getPkColumn());
        context.put("importList", getImportList(genTable));
        context.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
        context.put("columns", genTable.getColumns());
        context.put("table", genTable);
        context.put("dicts", getDicts(genTable));
        setExtensionsContext(context, genTable.getOptions());
        setMenuContext(context, genTable);
        if (GenConstants.TPL_TREE.equals(tplCategory))
        {
            setTreeContext(context, genTable);
        }
        if (GenConstants.TPL_SUB.equals(tplCategory))
        {
            setSubContext(context, genTable);
        }
        return context;
    }

    public static void setExtensionsContext(Map<String, Object> context, String options)
    {
        JSONObject paramsObj = JSONObject.parseObject(options);
        boolean genView = genView(paramsObj);
        context.put("genView", genView);
    }

    public static void setMenuContext(Map<String, Object> context, GenTable genTable)
    {
        String options = genTable.getOptions();
        JSONObject paramsObj = JSON.parseObject(options);
        String parentMenuId = getParentMenuId(paramsObj);
        context.put("parentMenuId", parentMenuId);
    }

    public static void setTreeContext(Map<String, Object> context, GenTable genTable)
    {
        String options = genTable.getOptions();
        JSONObject paramsObj = JSON.parseObject(options);
        String treeCode = getTreecode(paramsObj);
        String treeParentCode = getTreeParentCode(paramsObj);
        String treeName = getTreeName(paramsObj);

        context.put("treeCode", treeCode);
        context.put("treeParentCode", treeParentCode);
        context.put("treeName", treeName);
        context.put("expandColumn", getExpandColumn(genTable));
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE))
        {
            context.put("tree_parent_code", paramsObj.getString(GenConstants.TREE_PARENT_CODE));
        }
        if (paramsObj.containsKey(GenConstants.TREE_NAME))
        {
            context.put("tree_name", paramsObj.getString(GenConstants.TREE_NAME));
        }
    }

    public static void setSubContext(Map<String, Object> context, GenTable genTable)
    {
        GenTable subTable = genTable.getSubTable();
        String subTableName = genTable.getSubTableName();
        String subTableFkName = genTable.getSubTableFkName();
        String subClassName = genTable.getSubTable().getClassName();
        String subTableFkClassName = StringUtils.convertToCamelCase(subTableFkName);

        context.put("subTable", subTable);
        context.put("subTableName", subTableName);
        context.put("subTableFkName", subTableFkName);
        context.put("subTableFkClassName", subTableFkClassName);
        context.put("subTableFkclassName", StringUtils.uncapitalize(subTableFkClassName));
        context.put("subClassName", subClassName);
        context.put("subclassName", StringUtils.uncapitalize(subClassName));
        context.put("subImportList", getImportList(genTable.getSubTable()));
    }

    /**
     * 获取模板信息
     * @param table 业务表
     * @return 模板列表
     */
    public static List<String> getTemplateList(GenTable table)
    {
        String tplWebType = table.getTplWebType();
        String tplCategory = table.getTplCategory();
        JSONObject paramsObj = JSONObject.parseObject(table.getOptions());
        boolean isView = genView(paramsObj);
        String useWebType = "ftl/vue";
        String apiTemplate = "ftl/js/api.js.ftl";
        if (StringUtils.equals(ELEMENT_PLUS, tplWebType))
        {
            useWebType = "ftl/vue/v3";
        }
        else if (StringUtils.equals(ELEMENT_PLUS_TYPESSRIPT, tplWebType))
        {
            useWebType = "ftl/vue/v3ts";
            apiTemplate = "ftl/ts/api.ts.ftl";
        }
        List<String> templates = new ArrayList<String>();
        templates.add("ftl/java/domain.java.ftl");
        templates.add("ftl/java/mapper.java.ftl");
        templates.add("ftl/java/service.java.ftl");
        templates.add("ftl/java/serviceImpl.java.ftl");
        templates.add("ftl/java/controller.java.ftl");
        templates.add("ftl/xml/mapper.xml.ftl");
        templates.add("ftl/sql/sql.ftl");
        templates.add(apiTemplate);
        if (StringUtils.equals(ELEMENT_PLUS_TYPESSRIPT, tplWebType))
        {
            templates.add("ftl/ts/type.ts.ftl");
            templates.add("ftl/ts/index.ts.ftl");
        }
        if (GenConstants.TPL_CRUD.equals(tplCategory))
        {
            templates.add(useWebType + "/index.vue.ftl");
        }
        else if (GenConstants.TPL_TREE.equals(tplCategory))
        {
            templates.add(useWebType + "/index-tree.vue.ftl");
        }
        else if (GenConstants.TPL_SUB.equals(tplCategory))
        {
            templates.add(useWebType + "/index.vue.ftl");
            templates.add("ftl/java/sub-domain.java.ftl");
        }
        if (isView)
        {
            templates.add(useWebType + "/view.vue.ftl");
        }
        return templates;
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, GenTable genTable)
    {
        String fileName = "";
        String packageName = genTable.getPackageName();
        String moduleName = genTable.getModuleName();
        String className = genTable.getClassName();
        String businessName = genTable.getBusinessName();

        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;
        String vuePath = "vue";

        if (template.contains("domain.java.ftl"))
        {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        }
        if (template.contains("sub-domain.java.ftl") && StringUtils.equals(GenConstants.TPL_SUB, genTable.getTplCategory()))
        {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, genTable.getSubTable().getClassName());
        }
        else if (template.contains("mapper.java.ftl"))
        {
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        }
        else if (template.contains("service.java.ftl"))
        {
            fileName = StringUtils.format("{}/service/I{}Service.java", javaPath, className);
        }
        else if (template.contains("serviceImpl.java.ftl"))
        {
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        }
        else if (template.contains("controller.java.ftl"))
        {
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        }
        else if (template.contains("mapper.xml.ftl"))
        {
            fileName = StringUtils.format("{}/{}Mapper.xml", mybatisPath, className);
        }
        else if (template.contains("sql.ftl"))
        {
            fileName = businessName + "Menu.sql";
        }
        else if (template.contains("api.js.ftl"))
        {
            fileName = StringUtils.format("{}/api/{}/{}.js", vuePath, moduleName, businessName);
        }
        else if (template.contains("api.ts.ftl"))
        {
            fileName = StringUtils.format("{}/api/{}/{}.ts", vuePath, moduleName, businessName);
        }
        else if (template.contains("type.ts.ftl"))
        {
            fileName = StringUtils.format("{}/types/api/{}/{}.ts", vuePath, moduleName, businessName);
        }
        else if (template.contains("index.ts.ftl"))
        {
            fileName = StringUtils.format("{}/types/api/index-bak.ts", vuePath);
        }
        else if (template.contains("index.vue.ftl"))
        {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        }
        else if (template.contains("index-tree.vue.ftl"))
        {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        }
        else if (template.contains("view.vue.ftl"))
        {
            fileName = StringUtils.format("{}/views/{}/{}/view.vue", vuePath, moduleName, businessName);
        }
        return fileName;
    }

    /**
     * 获取包前缀
     *
     * @param packageName 包名称
     * @return 包前缀名称
     */
    public static String getPackagePrefix(String packageName)
    {
        int lastIndex = packageName.lastIndexOf(".");
        return StringUtils.substring(packageName, 0, lastIndex);
    }

    /**
     * 根据列类型获取导入包
     *
     * @param genTable 业务表对象
     * @return 返回需要导入的包列表
     */
    public static HashSet<String> getImportList(GenTable genTable)
    {
        List<GenTableColumn> columns = genTable.getColumns();
        GenTable subGenTable = genTable.getSubTable();
        HashSet<String> importList = new HashSet<String>();
        if (StringUtils.isNotNull(subGenTable))
        {
            importList.add("java.util.List");
        }
        for (GenTableColumn column : columns)
        {
            if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType()))
            {
                importList.add("java.util.Date");
                importList.add("com.fasterxml.jackson.annotation.JsonFormat");
            }
            else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType()))
            {
                importList.add("java.math.BigDecimal");
            }
        }
        return importList;
    }

    /**
     * 根据列类型获取字典组
     *
     * @param genTable 业务表对象
     * @return 返回字典组
     */
    public static String getDicts(GenTable genTable)
    {
        List<GenTableColumn> columns = genTable.getColumns();
        Set<String> dicts = new HashSet<String>();
        addDicts(dicts, columns);
        if (StringUtils.isNotNull(genTable.getSubTable()))
        {
            List<GenTableColumn> subColumns = genTable.getSubTable().getColumns();
            addDicts(dicts, subColumns);
        }
        return StringUtils.join(dicts, ", ");
    }

    /**
     * 添加字典列表
     *
     * @param dicts 字典列表
     * @param columns 列集合
     */
    public static void addDicts(Set<String> dicts, List<GenTableColumn> columns)
    {
        for (GenTableColumn column : columns)
        {
            if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
                    column.getHtmlType(),
                    new String[] { GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX }))
            {
                dicts.add("'" + column.getDictType() + "'");
            }
        }
    }

    /**
     * 获取权限前缀
     *
     * @param moduleName 模块名称
     * @param businessName 业务名称
     * @return 返回权限前缀
     */
    public static String getPermissionPrefix(String moduleName, String businessName)
    {
        return StringUtils.format("{}:{}", moduleName, businessName);
    }

    /**
     * 获取上级菜单ID字段
     *
     * @param paramsObj 生成其他选项
     * @return 上级菜单ID字段
     */
    public static String getParentMenuId(JSONObject paramsObj)
    {
        if (StringUtils.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.PARENT_MENU_ID)
                && StringUtils.isNotEmpty(paramsObj.getString(GenConstants.PARENT_MENU_ID)))
        {
            return paramsObj.getString(GenConstants.PARENT_MENU_ID);
        }
        return DEFAULT_PARENT_MENU_ID;
    }

    /**
     * 获取树编码
     *
     * @param paramsObj 生成其他选项
     * @return 树编码
     */
    public static String getTreecode(JSONObject paramsObj)
    {
        if (paramsObj.containsKey(GenConstants.TREE_CODE))
        {
            return StringUtils.toCamelCase(paramsObj.getString(GenConstants.TREE_CODE));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树父编码
     *
     * @param paramsObj 生成其他选项
     * @return 树父编码
     */
    public static String getTreeParentCode(JSONObject paramsObj)
    {
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE))
        {
            return StringUtils.toCamelCase(paramsObj.getString(GenConstants.TREE_PARENT_CODE));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 扩展功能/生成详情页
     *
     * @param paramsObj 生成其他选项
     * @return 是否生成详细页
     */
    public static boolean genView(JSONObject paramsObj)
    {
        if (StringUtils.isNotNull(paramsObj) && paramsObj.containsKey(GenConstants.GEN_VIEW))
        {
            return paramsObj.getBoolean(GenConstants.GEN_VIEW);
        }
        return false;
    }

    /**
     * 获取树名称
     *
     * @param paramsObj 生成其他选项
     * @return 树名称
     */
    public static String getTreeName(JSONObject paramsObj)
    {
        if (paramsObj.containsKey(GenConstants.TREE_NAME))
        {
            return StringUtils.toCamelCase(paramsObj.getString(GenConstants.TREE_NAME));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取需要在哪一列上面显示展开按钮
     *
     * @param genTable 业务表对象
     * @return 展开按钮列序号
     */
    public static int getExpandColumn(GenTable genTable)
    {
        String options = genTable.getOptions();
        JSONObject paramsObj = JSON.parseObject(options);
        String treeName = paramsObj.getString(GenConstants.TREE_NAME);
        int num = 0;
        for (GenTableColumn column : genTable.getColumns())
        {
            if (column.isList())
            {
                num++;
                String columnName = column.getColumnName();
                if (columnName.equals(treeName))
                {
                    break;
                }
            }
        }
        return num;
    }

    /**
     * 获取表单 el-col span
     *
     * @param formColNum 表单布局方式（1单列 2双列 3三列）
     * @return span 数值字符串
     */
    public static String getColSpan(int formColNum)
    {
        if (formColNum == 2)
        {
            return "12";
        }
        else if (formColNum == 3)
        {
            return "8";
        }
        return "24";
    }
}
