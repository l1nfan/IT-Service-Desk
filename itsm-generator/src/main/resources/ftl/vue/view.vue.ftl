<template>
  <el-drawer title="${functionName}详情" :visible.sync="visible" direction="rtl" size="60%" append-to-body :before-close="handleClose" custom-class="detail-drawer">
    <div v-loading="loading" class="drawer-content">
      <h4 class="section-header">基本信息</h4>
<#assign i = 0>
<#list columns as column>
<#if !column.pk && column.list>
<#assign dictType=column.dictType>
<#assign javaField=column.javaField>
<#assign parentheseIndex=column.columnComment.indexOf("（")>
<#if parentheseIndex != -1>
<#assign comment=column.columnComment.substring(0, parentheseIndex)>
<#else>
<#assign comment=column.columnComment>
</#if>
<#if i % 2 == 0>
      <el-row :gutter="20" class="mb8">
</#if>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">${comment}：</label>
            <span class="info-value plaintext">
<#if "" != dictType>
<#if column.htmlType == "checkbox">
              <dict-tag :options="dict.type.${dictType}" :value="info.${javaField} ? info.${javaField}.split(',') : []" />
<#else>
              <dict-tag :options="dict.type.${dictType}" :value="info.${javaField}" />
</#if>
<#elseif column.htmlType == "datetime">
              {{ parseTime(info.${javaField}, '{y}-{m}-{d}') }}
<#else>
              {{ info.${javaField} }}
</#if>
            </span>
          </div>
        </el-col>
<#assign i = i + 1>
<#if i % 2 == 0>
      </el-row>
</#if>
</#if>
</#list>
<#if i % 2 != 0>
      </el-row>
</#if>
    </div>
  </el-drawer>
</template>

<script>
import { get${BusinessName} } from '@/api/${moduleName}/${businessName}'

export default {
  name: '${BusinessName}ViewDrawer',
<#list columns as column>
<#if "" != column.dictType>
<#assign hasDicts = true>
<#break>
</#if>
</#list>
<#if hasDicts??>
  dicts: [<#list columns as column><#if "" != column.dictType>'${column.dictType}'<#if column_has_next>, </#if></#if></#list>],
</#if>
  data() {
    return {
      visible: false,
      loading: false,
      info: {}
    }
  },
  methods: {
    open(${pkColumn.javaField}) {
      this.visible = true
      this.loading = true
      get${BusinessName}(${pkColumn.javaField}).then(res => {
        this.info = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    handleClose() {
      this.visible = false
    }
  }
}
</script>
