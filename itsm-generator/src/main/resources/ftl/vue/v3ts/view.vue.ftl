<template>
  <el-drawer title="${functionName}详情" v-model="visible" direction="rtl" size="60%" append-to-body :before-close="handleClose" class="detail-drawer">
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
              <dict-tag :options="${dictType}" :value="info.${javaField} ? info.${javaField}.split(',') : []" />
<#else>
              <dict-tag :options="${dictType}" :value="info.${javaField}" />
</#if>
<#elseif column.htmlType == "datetime">
              {{ parseTime(info.${javaField}, '{y}-{m}-{d}') }}
<#elseif column.htmlType == "imageUpload">
              <image-preview :src="info.${javaField}" :width="60" :height="60" />
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

<script setup lang="ts" name="${BusinessName}ViewDrawer">
import type { ${ClassName} } from "@/types/api/${moduleName}/${businessName}"
import { get${BusinessName} } from '@/api/${moduleName}/${businessName}'
<#if dicts != ''>
<#assign dictsNoSymbol=dicts?replace("'", "")>

const { ${dictsNoSymbol} } = useDict(${dicts})
</#if>

const visible = ref<boolean>(false)
const loading = ref<boolean>(false)
const info = reactive<${ClassName}>({} as ${ClassName})

const open = async (${pkColumn.javaField}: number) => {
  visible.value = true
  loading.value = true
  try {
    const res = await get${BusinessName}(${pkColumn.javaField})
    Object.assign(info, res.data || {})
  } catch (error) {
    console.error('获取${functionName}信息失败:', error)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  visible.value = false
  Object.keys(info).forEach(key => delete (info as any)[key])
}

defineExpose({ open })
</script>
