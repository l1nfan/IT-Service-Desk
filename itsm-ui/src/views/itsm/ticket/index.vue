<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="工单编号" prop="ticketNo">
            <el-input
               v-model="queryParams.ticketNo"
               placeholder="请输入工单编号"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="工单标题" prop="title">
            <el-input
               v-model="queryParams.title"
               placeholder="请输入工单标题"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="工单分类" prop="categoryId">
            <el-tree-select
               v-model="queryParams.categoryId"
               :data="categoryOptions"
               :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }"
               value-key="categoryId"
               placeholder="请选择分类"
               check-strictly
               clearable
               style="width: 200px"
            />
         </el-form-item>
         <el-form-item label="优先级" prop="priority">
            <el-select v-model="queryParams.priority" placeholder="请选择优先级" clearable style="width: 200px">
               <el-option
                  v-for="dict in itsm_ticket_priority"
                  :key="dict.value"
                  :label="dict.label"
                  :value="parseInt(dict.value)"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
               <el-option
                  v-for="dict in itsm_ticket_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="创建时间" style="width: 308px">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
            />
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
               v-hasPermi="['itsm:ticket:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['itsm:ticket:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['itsm:ticket:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['itsm:ticket:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="ticketList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="工单编号" align="center" prop="ticketNo" width="160" />
         <el-table-column label="工单标题" align="center" :show-overflow-tooltip="true" min-width="200">
            <template #default="scope">
               <el-link type="primary" @click="handleViewDetail(scope.row)">{{ scope.row.title }}</el-link>
            </template>
         </el-table-column>
         <el-table-column label="分类" align="center" prop="categoryName" width="120" />
         <el-table-column label="优先级" align="center" prop="priority" width="90">
            <template #default="scope">
               <dict-tag :options="itsm_ticket_priority" :value="scope.row.priority" />
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <dict-tag :options="itsm_ticket_status" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="SLA" align="center" prop="slaStatus" width="80">
            <template #default="scope">
               <el-tag v-if="slaMap[scope.row.ticketId]" :type="getSlaStatusType(slaMap[scope.row.ticketId])" size="small">{{ getSlaStatusLabel(slaMap[scope.row.ticketId]) }}</el-tag>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="创建人" align="center" prop="creatorName" width="100" />
         <el-table-column label="处理人" align="center" prop="assigneeName" width="100" />
         <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
            <template #default="scope">
               <el-button link type="primary" icon="View" @click="handleViewDetail(scope.row)" v-hasPermi="['itsm:ticket:query']">详情</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['itsm:ticket:edit']">修改</el-button>
               <el-button v-if="scope.row.status === 'SUBMITTED'" link type="primary" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['itsm:ticket:approve']">审批</el-button>
               <el-button v-if="scope.row.status === 'APPROVED'" link type="primary" icon="User" @click="handleAssign(scope.row)" v-hasPermi="['itsm:ticket:assign']">分配</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['itsm:ticket:remove']">删除</el-button>
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

      <el-dialog :title="title" v-model="open" width="780px" append-to-body>
         <el-form ref="ticketRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="24">
                  <el-form-item label="工单标题" prop="title">
                     <el-input v-model="form.title" placeholder="请输入工单标题" maxlength="200" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="工单分类" prop="categoryId">
                     <el-tree-select
                        v-model="form.categoryId"
                        :data="categoryOptions"
                        :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }"
                        value-key="categoryId"
                        placeholder="请选择分类"
                        check-strictly
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                     <el-select v-model="form.priority" placeholder="请选择优先级">
                        <el-option
                           v-for="dict in itsm_ticket_priority"
                           :key="dict.value"
                           :label="dict.label"
                           :value="parseInt(dict.value)"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="影响范围" prop="impactScope">
                     <el-input v-model="form.impactScope" placeholder="请输入影响范围" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="期望解决时间" prop="expectedResolveTime">
                     <el-date-picker
                        v-model="form.expectedResolveTime"
                        type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="请选择期望解决时间"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="工单描述" prop="description">
                     <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入工单描述" />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                     <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="附件" prop="attachment">
                     <el-upload
                        :action="uploadUrl"
                        :headers="uploadHeaders"
                        :before-upload="handleBeforeUpload"
                        :on-success="handleUploadSuccess"
                        :on-error="handleUploadError"
                        :on-remove="handleUploadRemove"
                        :file-list="formFileList"
                        :data="{ ticketId: form.ticketId }"
                        :disabled="!form.ticketId"
                        multiple
                        :limit="5"
                        :on-exceed="handleExceed"
                     >
                        <el-button type="primary" :disabled="!form.ticketId">选取文件</el-button>
                        <template #tip>
                           <div class="el-upload__tip">上传文件大小不超过50MB，最多5个文件</div>
                        </template>
                     </el-upload>
                     <div v-if="!form.ticketId" style="color: #909399; font-size: 12px">请先保存工单后再上传附件</div>
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

      <el-dialog title="工单详情" v-model="detailOpen" width="780px" append-to-body>
         <el-descriptions :column="2" border>
            <el-descriptions-item label="工单编号">{{ detail.ticketNo }}</el-descriptions-item>
            <el-descriptions-item label="工单标题">{{ detail.title }}</el-descriptions-item>
            <el-descriptions-item label="工单分类">{{ detail.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="优先级">
               <dict-tag :options="itsm_ticket_priority" :value="detail.priority" />
            </el-descriptions-item>
            <el-descriptions-item label="状态">
               <dict-tag :options="itsm_ticket_status" :value="detail.status" />
            </el-descriptions-item>
            <el-descriptions-item label="来源">
               <dict-tag :options="itsm_ticket_source" :value="detail.source" />
            </el-descriptions-item>
            <el-descriptions-item label="创建人">{{ detail.creatorName }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ detail.assigneeName }}</el-descriptions-item>
            <el-descriptions-item label="影响范围">{{ detail.impactScope }}</el-descriptions-item>
            <el-descriptions-item label="期望解决时间">{{ parseTime(detail.expectedResolveTime) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ parseTime(detail.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="工单描述" :span="2">{{ detail.description }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
         </el-descriptions>
         <template #footer>
            <div class="dialog-footer">
               <el-button @click="detailOpen = false">关 闭</el-button>
            </div>
         </template>
      </el-dialog>

      <el-dialog title="工单分配" v-model="assignOpen" width="500px" append-to-body>
         <el-form ref="assignRef" :model="assignForm" :rules="assignRules" label-width="100px">
            <el-form-item label="处理人" prop="assigneeId">
               <el-select v-model="assignForm.assigneeId" placeholder="请选择处理人" filterable>
                  <el-option
                     v-for="user in userList"
                     :key="user.userId"
                     :label="user.nickName"
                     :value="user.userId"
                  />
               </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="assignForm.remark" type="textarea" placeholder="请输入分配备注" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitAssign">确 定</el-button>
               <el-button @click="assignOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <el-dialog title="工单审批" v-model="approveOpen" width="500px" append-to-body>
         <el-form ref="approveRef" :model="approveForm" :rules="approveRules" label-width="100px">
            <el-form-item label="审批结果" prop="status">
               <el-radio-group v-model="approveForm.status">
                  <el-radio value="APPROVED">通过</el-radio>
                  <el-radio value="REJECTED">驳回</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见" prop="remark">
               <el-input v-model="approveForm.remark" type="textarea" placeholder="请输入审批意见" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitApprove">确 定</el-button>
               <el-button @click="approveOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="ItsmTicket">
import { listTicket, getTicket, delTicket, addTicket, updateTicket, assignTicket, approveTicket, listAttachment, delAttachment, getSlaRecord } from "@/api/itsm/ticket"
import { listCategory } from "@/api/itsm/category"
import { listUser } from "@/api/system/user"
import { getToken } from "@/utils/auth"

const router = useRouter()
const { proxy } = getCurrentInstance()
const { itsm_ticket_priority, itsm_ticket_status, itsm_ticket_source } = useDict("itsm_ticket_priority", "itsm_ticket_status", "itsm_ticket_source")

const ticketList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const assignOpen = ref(false)
const approveOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const categoryOptions = ref([])
const userList = ref([])
const detail = ref({})
const dateRange = ref([])
const assignForm = ref({})
const approveForm = ref({})
const formFileList = ref([])
const slaMap = ref({})
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/itsm/ticket/attachment/upload")
const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    ticketNo: undefined,
    title: undefined,
    categoryId: undefined,
    priority: undefined,
    status: undefined
  },
  rules: {
    title: [{ required: true, message: "工单标题不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "工单分类不能为空", trigger: "change" }],
    priority: [{ required: true, message: "优先级不能为空", trigger: "change" }]
  },
  assignRules: {
    assigneeId: [{ required: true, message: "处理人不能为空", trigger: "change" }]
  },
  approveRules: {
    status: [{ required: true, message: "审批结果不能为空", trigger: "change" }]
  },
})

const { queryParams, form, rules, assignRules, approveRules } = toRefs(data)

function getList() {
  loading.value = true
  listTicket(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    ticketList.value = response.rows
    total.value = response.total
    loading.value = false
    loadSlaForTickets(response.rows)
  })
}

function loadSlaForTickets(tickets) {
  slaMap.value = {}
  for (const t of tickets) {
    if (t.ticketId && t.status !== 'DRAFT' && t.status !== 'CANCELLED') {
      getSlaRecord(t.ticketId).then(res => {
        if (res.data) {
          slaMap.value[t.ticketId] = res.data.status
        }
      }).catch(() => {})
    }
  }
}

function getSlaStatusType(status) {
  switch (status) {
    case 'NORMAL': return 'success'
    case 'WARNING': return 'warning'
    case 'BREACHED': return 'danger'
    case 'RESOLVED': return 'info'
    default: return 'info'
  }
}

function getSlaStatusLabel(status) {
  switch (status) {
    case 'NORMAL': return '正常'
    case 'WARNING': return '预警'
    case 'BREACHED': return '超时'
    case 'RESOLVED': return '已解决'
    default: return status || '-'
  }
}

function getCategoryTree() {
  listCategory().then(response => {
    categoryOptions.value = response.data
  })
}

function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(response => {
    userList.value = response.rows
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    ticketId: undefined,
    title: undefined,
    categoryId: undefined,
    subCategoryId: undefined,
    priority: undefined,
    impactScope: undefined,
    expectedResolveTime: undefined,
    description: undefined,
    remark: undefined
  }
  proxy.resetForm("ticketRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ticketId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  getCategoryTree()
  open.value = true
  title.value = "新增工单"
}

function handleUpdate(row) {
  reset()
  getCategoryTree()
  const ticketId = row.ticketId || ids.value
  getTicket(ticketId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改工单"
    loadFormAttachments()
  })
}

function handleView(row) {
   getTicket(row.ticketId).then(response => {
      detail.value = response.data
      detailOpen.value = true
   })
}

function handleViewDetail(row) {
   router.push({ path: "/itsm/ticket/detail", query: { ticketId: row.ticketId } })
}

function handleAssign(row) {
  assignForm.value = {
    ticketId: row.ticketId,
    assigneeId: undefined,
    remark: undefined
  }
  getUserList()
  assignOpen.value = true
}

function handleApprove(row) {
  approveForm.value = {
    ticketId: row.ticketId,
    status: "APPROVED",
    remark: undefined
  }
  approveOpen.value = true
}

function submitForm() {
  proxy.$refs["ticketRef"].validate(valid => {
    if (valid) {
      if (form.value.ticketId != undefined) {
        updateTicket(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addTicket(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function submitAssign() {
  proxy.$refs["assignRef"].validate(valid => {
    if (valid) {
      assignTicket(assignForm.value).then(response => {
        proxy.$modal.msgSuccess("分配成功")
        assignOpen.value = false
        getList()
      })
    }
  })
}

function submitApprove() {
  proxy.$refs["approveRef"].validate(valid => {
    if (valid) {
      approveTicket(approveForm.value).then(response => {
        proxy.$modal.msgSuccess("审批成功")
        approveOpen.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const ticketIds = row.ticketId || ids.value
  proxy.$modal.confirm('是否确认删除工单编号为"' + ticketIds + '"的数据项？').then(function() {
    return delTicket(ticketIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('itsm/ticket/export', {
    ...queryParams.value
  }, `工单数据_${new Date().getTime()}.xlsx`)
}

function handleBeforeUpload(file) {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    proxy.$modal.msgError("上传文件大小不能超过 50MB!")
    return false
  }
  return true
}

function handleUploadSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess("上传成功")
    loadFormAttachments()
  } else {
    proxy.$modal.msgError(res.msg || "上传失败")
  }
}

function handleUploadError() {
  proxy.$modal.msgError("上传文件失败")
}

function handleUploadRemove(file) {
  if (file.response && file.response.data && file.response.data.attachmentId) {
    delAttachment(file.response.data.attachmentId).then(() => {
      proxy.$modal.msgSuccess("删除成功")
    })
  }
}

function handleExceed() {
  proxy.$modal.msgError("上传文件数量不能超过5个!")
}

function loadFormAttachments() {
  if (form.value.ticketId) {
    listAttachment(form.value.ticketId).then(response => {
      formFileList.value = (response.data || []).map(item => ({
         name: item.fileName,
         url: import.meta.env.VITE_APP_BASE_API + "/itsm/ticket/attachment/download/" + item.attachmentId,
         attachmentId: item.attachmentId
      }))
    })
  } else {
    formFileList.value = []
  }
}

getCategoryTree()
getList()
</script>
