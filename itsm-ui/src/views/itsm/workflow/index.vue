<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="流程名称" prop="workflowName">
            <el-input
               v-model="queryParams.workflowName"
               placeholder="请输入流程名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="流程标识" prop="workflowKey">
            <el-input
               v-model="queryParams.workflowKey"
               placeholder="请输入流程标识"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="流程状态" clearable style="width: 200px">
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
               v-hasPermi="['itsm:workflow:add']"
            >新增</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="workflowList">
         <el-table-column prop="workflowId" label="流程ID" width="80"></el-table-column>
         <el-table-column prop="workflowName" label="流程名称" min-width="180" show-overflow-tooltip></el-table-column>
         <el-table-column prop="workflowKey" label="流程标识" width="160"></el-table-column>
         <el-table-column prop="version" label="版本" width="60"></el-table-column>
         <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column prop="isDefault" label="默认流程" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.isDefault === '1'" type="success">是</el-tag>
               <el-tag v-else type="info">否</el-tag>
            </template>
         </el-table-column>
         <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['itsm:workflow:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['itsm:workflow:remove']">删除</el-button>
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
         <el-form ref="workflowRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="24">
                  <el-form-item label="流程名称" prop="workflowName">
                     <el-input v-model="form.workflowName" placeholder="请输入流程名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="流程标识" prop="workflowKey">
                     <el-input v-model="form.workflowKey" placeholder="请输入流程标识" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="版本" prop="version">
                     <el-input-number v-model="form.version" controls-position="right" :min="1" />
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
                  <el-form-item label="默认流程" prop="isDefault">
                     <el-radio-group v-model="form.isDefault">
                        <el-radio value="1">是</el-radio>
                        <el-radio value="0">否</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="描述" prop="description">
                     <el-input v-model="form.description" type="textarea" placeholder="请输入流程描述" />
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

<script setup name="ItsmWorkflow">
import { listWorkflow, getWorkflow, delWorkflow, addWorkflow, updateWorkflow } from "@/api/itsm/workflow"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const workflowList = ref([])
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
    workflowName: undefined,
    workflowKey: undefined,
    status: undefined
  },
  rules: {
    workflowName: [{ required: true, message: "流程名称不能为空", trigger: "blur" }],
    workflowKey: [{ required: true, message: "流程标识不能为空", trigger: "blur" }],
    version: [{ required: true, message: "版本号不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listWorkflow(queryParams.value).then(response => {
    workflowList.value = response.rows
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
    workflowId: undefined,
    workflowName: undefined,
    workflowKey: undefined,
    description: undefined,
    version: 1,
    status: "0",
    isDefault: "0",
    remark: undefined
  }
  proxy.resetForm("workflowRef")
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
  title.value = "添加流程"
}

function handleUpdate(row) {
  reset()
  getWorkflow(row.workflowId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改流程"
  })
}

function submitForm() {
  proxy.$refs["workflowRef"].validate(valid => {
    if (valid) {
      if (form.value.workflowId != undefined) {
        updateWorkflow(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addWorkflow(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除流程名称为"' + row.workflowName + '"的数据项？').then(function() {
    return delWorkflow(row.workflowId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
