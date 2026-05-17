<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
<#list columns as column>
<#if column.query>
<#assign dictType=column.dictType>
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if column.htmlType == "input">
      <el-form-item label="${comment}" prop="${column.javaField}">
        <el-input v-model="queryParams.${column.javaField}" placeholder="请输入${comment}" clearable @keyup.enter="handleQuery" />
      </el-form-item>
<#elseif (column.htmlType == "select" || column.htmlType == "radio") && "" != dictType>
      <el-form-item label="${comment}" prop="${column.javaField}">
        <el-select v-model="queryParams.${column.javaField}" placeholder="请选择${comment}" clearable>
          <el-option v-for="dict in ${dictType}" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
<#elseif (column.htmlType == "select" || column.htmlType == "radio") && dictType??>
      <el-form-item label="${comment}" prop="${column.javaField}">
        <el-select v-model="queryParams.${column.javaField}" placeholder="请选择${comment}" clearable>
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
<#elseif column.htmlType == "datetime" && column.queryType != "BETWEEN">
      <el-form-item label="${comment}" prop="${column.javaField}">
        <el-date-picker clearable v-model="queryParams.${column.javaField}" type="date" value-format="YYYY-MM-DD" placeholder="选择${comment}" />
      </el-form-item>
<#elseif column.htmlType == "datetime" && column.queryType == "BETWEEN">
      <el-form-item label="${comment}" style="width: 308px">
        <el-date-picker v-model="daterange${AttrName}" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
</#if>
</#if>
</#list>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['${permissionPrefix}:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-if="refreshTable" v-loading="loading" :data="${businessName}List" row-key="${treeCode}" :default-expand-all="isExpandAll" :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
<#list columns as column>
<#assign javaField=column.javaField>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if column.pk>
<#elseif column.list && column.htmlType == "datetime">
      <el-table-column label="${comment}" align="center" prop="${javaField}" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.${javaField}, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
<#elseif column.list && column.htmlType == "imageUpload">
      <el-table-column label="${comment}" align="center" prop="${javaField}" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.${javaField}" :width="50" :height="50"/>
        </template>
      </el-table-column>
<#elseif column.list && "" != column.dictType>
      <el-table-column label="${comment}" align="center" prop="${javaField}">
        <template #default="scope">
<#if column.htmlType == "checkbox">
          <dict-tag :options="${column.dictType}" :value="scope.row.${javaField} ? scope.row.${javaField}.split(',') : []"/>
<#else>
          <dict-tag :options="${column.dictType}" :value="scope.row.${javaField}"/>
</#if>
        </template>
      </el-table-column>
<#elseif column.list && "" != javaField>
<#if column?index == 1>
      <el-table-column label="${comment}" prop="${javaField}" />
<#else>
      <el-table-column label="${comment}" align="center" prop="${javaField}" />
</#if>
</#if>
</#list>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
<#if genView>
          <el-button link type="primary" icon="View" @click="handleViewData(scope.row)" v-hasPermi="['${permissionPrefix}:query']">详情</el-button>
</#if>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['${permissionPrefix}:edit']">修改</el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['${permissionPrefix}:add']">新增</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['${permissionPrefix}:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

<#if genView>
    <${businessName}-view-drawer ref="${businessName}ViewRef" />
</#if>
<#if table.formColNum == 2>
<#assign dialogWidth = "800px">
<#elseif table.formColNum == 3>
<#assign dialogWidth = "1100px">
<#else>
<#assign dialogWidth = "500px">
</#if>
    <el-dialog :title="title" v-model="open" width="${dialogWidth}" append-to-body>
      <el-form ref="${businessName}Ref" :model="form" :rules="rules" label-width="100px">
        <el-row>
<#list columns as column>
<#assign field=column.javaField>
<#if column.insert && !column.pk>
<#if column.usableColumn || !column.superColumn>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#assign dictType=column.dictType>
<#if "" != treeParentCode && column.javaField == treeParentCode>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${treeParentCode}">
              <el-tree-select v-model="form.${treeParentCode}" :data="${businessName}Options" :props="{ value: '${treeCode}', label: '${treeName}', children: 'children' }" value-key="${treeCode}" placeholder="请选择${comment}" check-strictly />
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "input">
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-input v-model="form.${field}" placeholder="请输入${comment}" />
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "imageUpload">
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <image-upload v-model="form.${field}"/>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "fileUpload">
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <file-upload v-model="form.${field}"/>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "editor">
          <el-col :span="24">
            <el-form-item label="${comment}">
              <editor v-model="form.${field}" :min-height="192"/>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "select" && "" != dictType>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-select v-model="form.${field}" placeholder="请选择${comment}">
                <el-option v-for="dict in ${dictType}" :key="dict.value" :label="dict.label"
<#if column.javaType == "Integer" || column.javaType == "Long">
                  :value="parseInt(dict.value)"
<#else>
                  :value="dict.value"
</#if>
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "select" && dictType??>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-select v-model="form.${field}" placeholder="请选择${comment}">
                <el-option label="请选择字典生成" value="" />
              </el-select>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "checkbox" && "" != dictType>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-checkbox-group v-model="form.${field}">
                <el-checkbox v-for="dict in ${dictType}" :key="dict.value" :label="dict.value">{{dict.label}}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "checkbox" && dictType??>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-checkbox-group v-model="form.${field}">
                <el-checkbox>请选择字典生成</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "radio" && "" != dictType>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-radio-group v-model="form.${field}">
                <el-radio v-for="dict in ${dictType}" :key="dict.value"
<#if column.javaType == "Integer" || column.javaType == "Long">
                  :label="parseInt(dict.value)"
<#else>
                  :label="dict.value"
</#if>
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "radio" && dictType??>
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-radio-group v-model="form.${field}">
                <el-radio label="1">请选择字典生成</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "datetime">
          <el-col :span="${colSpan}">
            <el-form-item label="${comment}" prop="${field}">
              <el-date-picker clearable v-model="form.${field}" type="date" value-format="YYYY-MM-DD" placeholder="选择${comment}" />
            </el-form-item>
          </el-col>
<#elseif column.htmlType == "textarea">
          <el-col :span="24">
            <el-form-item label="${comment}" prop="${field}">
              <el-input v-model="form.${field}" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
</#if>
</#if>
</#if>
</#list>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="${BusinessName}">
import type { ${ClassName}, ${BusinessName}QueryParams } from "@/types/api/${moduleName}/${businessName}"
import { list${BusinessName}, get${BusinessName}, del${BusinessName}, add${BusinessName}, update${BusinessName} } from "@/api/${moduleName}/${businessName}"
<#if genView>
import ${BusinessName}ViewDrawer from "./view"
</#if>

const { proxy } = getCurrentInstance()
<#if dicts != ''>
<#assign dictsNoSymbol=dicts?replace("'", "")>
const { ${dictsNoSymbol} } = useDict(${dicts})
</#if>

const ${businessName}List = ref<${ClassName}[]>([])
const ${businessName}Options = ref<any[]>([])
const open = ref<boolean>(false)
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const title = ref<string>("")
const isExpandAll = ref<boolean>(true)
const refreshTable = ref<boolean>(true)
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
const daterange${AttrName} = ref<string[]>([])
</#if>
</#list>

const data = reactive({
  form: {} as ${ClassName},
  queryParams: {
    <#list columns as column>
<#if column.query>
    ${column.javaField}: undefined<#if column_has_next>,</#if>
</#if>
</#list>
  } as ${BusinessName}QueryParams,
  rules: {
    <#list columns as column>
<#if column.required>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
    ${column.javaField}: [
      { required: true, message: "${comment}不能为空", trigger: <#if column.htmlType == "select" || column.htmlType == "radio">"change"<#else>"blur"</#if> }
    ]<#if column_has_next>,</#if>
</#if>
</#list>
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
  queryParams.value.params = {}
<#break>
</#if>
</#list>
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
  if (null != daterange${AttrName}.value && '' != daterange${AttrName}.value) {
    queryParams.value.params["begin${AttrName}"] = daterange${AttrName}.value[0]
    queryParams.value.params["end${AttrName}"] = daterange${AttrName}.value[1]
  }
</#if>
</#list>
  list${BusinessName}(queryParams.value).then(response => {
    ${businessName}List.value = proxy.handleTree(response.data, "${treeCode}", "${treeParentCode}")
    loading.value = false
  })
}

function getTreeselect() {
  list${BusinessName}().then(response => {
    ${businessName}Options.value = []
    const data = { ${treeCode}: 0, ${treeName}: '顶级节点', children: [] }
    data.children = proxy.handleTree(response.data, "${treeCode}", "${treeParentCode}")
    ${businessName}Options.value.push(data)
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
<#list columns as column>
<#if column.htmlType == "checkbox">
    ${column.javaField}: []<#if column_has_next>,</#if>
<#else>
    ${column.javaField}: null<#if column_has_next>,</#if>
</#if>
</#list>
  }
  proxy.resetForm("${businessName}Ref")
}

function handleQuery() {
  getList()
}

function resetQuery() {
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
  daterange${AttrName}.value = []
</#if>
</#list>
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleAdd(row?: ${ClassName}) {
  reset()
  getTreeselect()
  if (row != null && row.${treeCode}) {
    form.value.${treeParentCode} = row.${treeCode}
  } else {
    form.value.${treeParentCode} = 0
  }
  open.value = true
  title.value = "添加${functionName}"
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}
<#if genView>

function handleViewData(row: ${ClassName}) {
  proxy.$refs["${businessName}ViewRef"].open(row.${pkColumn.javaField})
}
</#if>

async function handleUpdate(row: ${ClassName}) {
  reset()
  await getTreeselect()
  if (row != null) {
    form.value.${treeParentCode} = row.${treeParentCode}
  }
  get${BusinessName}(row.${pkColumn.javaField}).then(response => {
    form.value = response.data
<#list columns as column>
<#if column.htmlType == "checkbox">
    form.value.${column.javaField} = form.value.${column.javaField}.split(",")
</#if>
</#list>
    open.value = true
    title.value = "修改${functionName}"
  })
}

function submitForm() {
  proxy.$refs["${businessName}Ref"].validate((valid: boolean) => {
    if (valid) {
<#list columns as column>
<#if column.htmlType == "checkbox">
      form.value.${column.javaField} = form.value.${column.javaField}.join(",")
</#if>
</#list>
      if (form.value.${pkColumn.javaField} != null) {
        update${BusinessName}(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        add${BusinessName}(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row: ${ClassName}) {
  proxy.$modal.confirm('是否确认删除${functionName}编号为"' + row.${pkColumn.javaField} + '"的数据项？').then(function() {
    return del${BusinessName}(row.${pkColumn.javaField})
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
