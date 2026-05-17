<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
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
        <el-input
          v-model="queryParams.${column.javaField}"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<#elseif (column.htmlType == "select" || column.htmlType == "radio") && "" != dictType>
      <el-form-item label="${comment}" prop="${column.javaField}">
        <el-select v-model="queryParams.${column.javaField}" placeholder="请选择${comment}" clearable>
          <el-option
            v-for="dict in dict.type.${dictType}"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
        <el-date-picker clearable
          v-model="queryParams.${column.javaField}"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择${comment}">
        </el-date-picker>
      </el-form-item>
<#elseif column.htmlType == "datetime" && column.queryType == "BETWEEN">
      <el-form-item label="${comment}">
        <el-date-picker
          v-model="daterange${AttrName}"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
</#if>
</#if>
</#list>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['${permissionPrefix}:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['${permissionPrefix}:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['${permissionPrefix}:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['${permissionPrefix}:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="${businessName}List" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
<#list columns as column>
<#assign javaField=column.javaField>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if column.pk>
      <el-table-column label="${comment}" align="center" prop="${javaField}" />
<#elseif column.list && column.htmlType == "datetime">
      <el-table-column label="${comment}" align="center" prop="${javaField}" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.${javaField}, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
<#elseif column.list && column.htmlType == "imageUpload">
      <el-table-column label="${comment}" align="center" prop="${javaField}" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.${javaField}" :width="50" :height="50"/>
        </template>
      </el-table-column>
<#elseif column.list && "" != column.dictType>
      <el-table-column label="${comment}" align="center" prop="${javaField}">
        <template slot-scope="scope">
<#if column.htmlType == "checkbox">
          <dict-tag :options="dict.type.${column.dictType}" :value="scope.row.${javaField} ? scope.row.${javaField}.split(',') : []"/>
<#else>
          <dict-tag :options="dict.type.${column.dictType}" :value="scope.row.${javaField}"/>
</#if>
        </template>
      </el-table-column>
<#elseif column.list && "" != javaField>
      <el-table-column label="${comment}" align="center" prop="${javaField}" />
</#if>
</#list>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
<#if genView>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewData(scope.row)"
            v-hasPermi="['${permissionPrefix}:query']"
          >详情</el-button>
</#if>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['${permissionPrefix}:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['${permissionPrefix}:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

<#if genView>
    <!-- ${functionName}详情抽屉 -->
    <${businessName}-view-drawer ref="${businessName}ViewRef" />
</#if>
    <!-- 添加或修改${functionName}对话框 -->
<#if table.formColNum == 2>
<#assign dialogWidth = "800px">
<#elseif table.formColNum == 3>
<#assign dialogWidth = "1100px">
<#else>
<#assign dialogWidth = "500px">
</#if>
    <el-dialog :title="title" :visible.sync="open" width="${dialogWidth}" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
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
<#if column.htmlType == "input">
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
                <el-option
                  v-for="dict in dict.type.${dictType}"
                  :key="dict.value"
                  :label="dict.label"
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
                <el-checkbox
                  v-for="dict in dict.type.${dictType}"
                  :key="dict.value"
                  :label="dict.value">
                  {{dict.label}}
                </el-checkbox>
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
                <el-radio
                  v-for="dict in dict.type.${dictType}"
                  :key="dict.value"
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
              <el-date-picker clearable
                v-model="form.${field}"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择${comment}">
              </el-date-picker>
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
<#if table.sub>
        <el-divider content-position="center">${subTable.functionName}信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd${subClassName}">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleDelete${subClassName}">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="${subclassName}List" :row-class-name="row${subClassName}Index" @selection-change="handle${subClassName}SelectionChange" ref="${subclassName}">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
<#list subTable.columns as column>
<#assign javaField=column.javaField>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if column.pk || javaField == subTableFkclassName>
<#elseif column.list && column.htmlType == "input">
          <el-table-column label="${comment}" prop="${javaField}" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.${javaField}" placeholder="请输入${comment}" />
            </template>
          </el-table-column>
<#elseif column.list && column.htmlType == "datetime">
          <el-table-column label="${comment}" prop="${javaField}" width="240">
            <template slot-scope="scope">
              <el-date-picker clearable v-model="scope.row.${javaField}" type="date" value-format="yyyy-MM-dd" placeholder="请选择${comment}" />
            </template>
          </el-table-column>
<#elseif column.list && (column.htmlType == "select" || column.htmlType == "radio") && "" != column.dictType>
          <el-table-column label="${comment}" prop="${javaField}" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.${javaField}" placeholder="请选择${comment}">
                <el-option
                  v-for="dict in dict.type.${column.dictType}"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </template>
          </el-table-column>
<#elseif column.list && (column.htmlType == "select" || column.htmlType == "radio") && "" == column.dictType>
          <el-table-column label="${comment}" prop="${javaField}" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.${javaField}" placeholder="请选择${comment}">
                <el-option label="请选择字典生成" value="" />
              </el-select>
            </template>
          </el-table-column>
</#if>
</#list>
        </el-table>
</#if>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { list${BusinessName}, get${BusinessName}, del${BusinessName}, add${BusinessName}, update${BusinessName} } from "@/api/${moduleName}/${businessName}"
<#if genView>
import ${BusinessName}ViewDrawer from "./view"
</#if>

export default {
  name: "${BusinessName}",
<#if genView>
  components: { ${BusinessName}ViewDrawer },
</#if>
<#if dicts != ''>
  dicts: [${dicts}],
</#if>
  data() {
    return {
      loading: true,
      ids: [],
<#if table.sub>
      checked${subClassName}: [],
</#if>
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      ${businessName}List: [],
<#if table.sub>
      ${subclassName}List: [],
</#if>
      title: "",
      open: false,
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
      daterange${AttrName}: [],
</#if>
</#list>
      queryParams: {
        pageNum: 1,
        pageSize: 10,
<#list columns as column>
<#if column.query>
        ${column.javaField}: null<#if column_has_next>,</#if>
</#if>
</#list>
      },
      form: {},
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
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
      this.queryParams.params = {}
<#break>
</#if>
</#list>
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
      if (null != this.daterange${AttrName} && '' != this.daterange${AttrName}) {
        this.queryParams.params["begin${AttrName}"] = this.daterange${AttrName}[0]
        this.queryParams.params["end${AttrName}"] = this.daterange${AttrName}[1]
      }
</#if>
</#list>
      list${BusinessName}(this.queryParams).then(response => {
        this.${businessName}List = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
<#list columns as column>
<#if column.htmlType == "checkbox">
        ${column.javaField}: []<#if column_has_next>,</#if>
<#else>
        ${column.javaField}: null<#if column_has_next>,</#if>
</#if>
</#list>
      }
<#if table.sub>
      this.${subclassName}List = []
</#if>
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
<#list columns as column>
<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
<#assign AttrName=column.javaField.substring(0,1)?upper_case + column.javaField.substring(1)>
      this.daterange${AttrName} = []
</#if>
</#list>
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.${pkColumn.javaField})
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加${functionName}"
    },
    handleUpdate(row) {
      this.reset()
      const ${pkColumn.javaField} = row.${pkColumn.javaField} || this.ids
      get${BusinessName}(${pkColumn.javaField}).then(response => {
        this.form = response.data
<#list columns as column>
<#if column.htmlType == "checkbox">
        this.form.${column.javaField} = this.form.${column.javaField}.split(",")
</#if>
</#list>
<#if table.sub>
        this.${subclassName}List = response.data.${subclassName}List
</#if>
        this.open = true
        this.title = "修改${functionName}"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
<#list columns as column>
<#if column.htmlType == "checkbox">
          this.form.${column.javaField} = this.form.${column.javaField}.join(",")
</#if>
</#list>
<#if table.sub>
          this.form.${subclassName}List = this.${subclassName}List
</#if>
          if (this.form.${pkColumn.javaField} != null) {
            update${BusinessName}(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            add${BusinessName}(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ${pkColumn.javaField}s = row.${pkColumn.javaField} || this.ids
      this.$modal.confirm('是否确认删除${functionName}编号为"' + ${pkColumn.javaField}s + '"的数据项？').then(function() {
        return del${BusinessName}(${pkColumn.javaField}s)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
<#if table.sub>
    row${subClassName}Index({ row, rowIndex }) {
      row.index = rowIndex + 1
    },
    handleAdd${subClassName}() {
      let obj = {}
<#list subTable.columns as column>
<#if column.pk || column.javaField == subTableFkclassName>
<#elseif column.list && "" != javaField>
      obj.${column.javaField} = ""
</#if>
</#list>
      this.${subclassName}List.push(obj)
    },
    handleDelete${subClassName}() {
      if (this.checked${subClassName}.length == 0) {
        this.$modal.msgError("请先选择要删除的${subTable.functionName}数据")
      } else {
        const ${subclassName}List = this.${subclassName}List
        const checked${subClassName} = this.checked${subClassName}
        this.${subclassName}List = ${subclassName}List.filter(function(item) {
          return checked${subClassName}.indexOf(item.index) == -1
        })
      }
    },
    handle${subClassName}SelectionChange(selection) {
      this.checked${subClassName} = selection.map(item => item.index)
    },
</#if>
<#if genView>
    handleViewData(row) {
      this.$refs["${businessName}ViewRef"].open(row.${pkColumn.javaField})
    },
</#if>
    handleExport() {
      this.download('${moduleName}/${businessName}/export', {
        ...this.queryParams
      }, `${businessName}_${r"${new Date().getTime()}"}.xlsx`)
    }
  }
}
</script>
