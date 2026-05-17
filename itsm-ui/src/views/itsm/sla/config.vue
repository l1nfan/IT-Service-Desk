<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="SLA名称" prop="slaName">
            <el-input
               v-model="queryParams.slaName"
               placeholder="请输入SLA名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="SLA状态" clearable style="width: 200px">
               <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['itsm:sla:add']"
            >新增</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="slaList">
         <el-table-column prop="slaId" label="SLA ID" width="80"></el-table-column>
         <el-table-column prop="slaName" label="SLA名称" min-width="160" show-overflow-tooltip></el-table-column>
         <el-table-column prop="priority" label="优先级" width="100">
            <template #default="scope">
               <el-tag v-if="scope.row.priority === 'URGENT'" type="danger">紧急</el-tag>
               <el-tag v-else-if="scope.row.priority === 'HIGH'" type="warning">高</el-tag>
               <el-tag v-else-if="scope.row.priority === 'MEDIUM'" type="success">中</el-tag>
               <el-tag v-else-if="scope.row.priority === 'LOW'" type="info">低</el-tag>
               <span v-else>{{ scope.row.priority }}</span>
            </template>
         </el-table-column>
         <el-table-column prop="responseTime" label="响应时限(分钟)" width="130"></el-table-column>
         <el-table-column prop="resolutionTime" label="解决时限(分钟)" width="130"></el-table-column>
         <el-table-column prop="warningThreshold" label="预警阈值(%)" width="120"></el-table-column>
         <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['itsm:sla:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['itsm:sla:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="slaRef" :model="form" :rules="rules" label-width="120px">
            <el-row>
               <el-col :span="24">
                  <el-form-item label="SLA名称" prop="slaName">
                     <el-input v-model="form.slaName" placeholder="请输入SLA名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                     <el-select v-model="form.priority" placeholder="请选择优先级">
                        <el-option label="低" value="LOW" />
                        <el-option label="中" value="MEDIUM" />
                        <el-option label="高" value="HIGH" />
                        <el-option label="紧急" value="URGENT" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-radio-group v-model="form.status">
                        <el-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="响应时限(分钟)" prop="responseTime">
                     <el-input-number v-model="form.responseTime" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="解决时限(分钟)" prop="resolutionTime">
                     <el-input-number v-model="form.resolutionTime" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="预警阈值(%)" prop="warningThreshold">
                     <el-input-number v-model="form.warningThreshold" controls-position="right" :min="1" :max="100" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="关联分类" prop="categoryId">
                     <el-input-number v-model="form.categoryId" controls-position="right" :min="0" placeholder="可选" />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                     <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
                  </el-form-item>
               </el-col>
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

<script setup name="ItsmSlaConfig">
import { listSla, getSla, delSla, addSla, updateSla } from "@/api/itsm/sla"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const slaList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    slaName: undefined,
    status: undefined
  },
  rules: {
    slaName: [{ required: true, message: "SLA名称不能为空", trigger: "blur" }],
    priority: [{ required: true, message: "优先级不能为空", trigger: "change" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listSla(queryParams.value).then(response => {
    slaList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    slaId: undefined,
    slaName: undefined,
    categoryId: undefined,
    priority: undefined,
    responseTime: undefined,
    resolutionTime: undefined,
    warningThreshold: 80,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("slaRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加SLA配置"
}

function handleUpdate(row) {
  reset()
  getSla(row.slaId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改SLA配置"
  })
}

function submitForm() {
  proxy.$refs["slaRef"].validate(valid => {
    if (valid) {
      if (form.value.slaId != undefined) {
        updateSla(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSla(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除SLA配置"' + row.slaName + '"的数据项？').then(function() {
    return delSla(row.slaId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
