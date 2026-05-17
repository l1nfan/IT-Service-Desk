<template>
   <div class="app-container">
      <el-page-header @back="goBack" content="工单详情" />

      <div v-loading="loading" style="margin-top: 20px">
         <el-card shadow="never" class="ticket-header-card">
            <el-descriptions :column="3" border>
               <el-descriptions-item label="工单编号">{{ ticket.ticketNo }}</el-descriptions-item>
               <el-descriptions-item label="工单标题" :span="2">{{ ticket.title }}</el-descriptions-item>
               <el-descriptions-item label="工单分类">{{ ticket.categoryName }}</el-descriptions-item>
               <el-descriptions-item label="优先级">
                  <dict-tag :options="itsm_ticket_priority" :value="ticket.priority" />
               </el-descriptions-item>
               <el-descriptions-item label="状态">
                  <TicketStatusTag :status="ticket.status" />
               </el-descriptions-item>
               <el-descriptions-item label="当前流程节点" v-if="ticket.currentNode">
                  <el-tag type="info" size="small">{{ getNodeLabel(ticket.currentNode) }}</el-tag>
               </el-descriptions-item>
               <el-descriptions-item label="来源">
                  <dict-tag :options="itsm_ticket_source" :value="ticket.source" />
               </el-descriptions-item>
               <el-descriptions-item label="创建人">{{ ticket.creatorName }}</el-descriptions-item>
               <el-descriptions-item label="处理人">{{ ticket.assigneeName }}</el-descriptions-item>
               <el-descriptions-item label="影响范围">{{ ticket.impactScope }}</el-descriptions-item>
               <el-descriptions-item label="期望解决时间">{{ parseTime(ticket.expectedResolveTime) }}</el-descriptions-item>
               <el-descriptions-item label="创建时间">{{ parseTime(ticket.createTime) }}</el-descriptions-item>
               <el-descriptions-item label="更新时间">{{ parseTime(ticket.updateTime) }}</el-descriptions-item>
               <el-descriptions-item label="工单描述" :span="3">{{ ticket.description }}</el-descriptions-item>
               <el-descriptions-item label="备注" :span="3">{{ ticket.remark }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions v-if="slaRecord" :column="3" border style="margin-top: 15px" title="SLA信息">
               <el-descriptions-item label="SLA状态">
                  <el-tag :type="getSlaStatusType(slaRecord.status)">{{ getSlaStatusLabel(slaRecord.status) }}</el-tag>
               </el-descriptions-item>
               <el-descriptions-item label="响应截止时间">{{ parseTime(slaRecord.responseDeadline) }}</el-descriptions-item>
               <el-descriptions-item label="解决截止时间">{{ parseTime(slaRecord.resolutionDeadline) }}</el-descriptions-item>
               <el-descriptions-item label="实际响应时间">{{ parseTime(slaRecord.responseActual) || '-' }}</el-descriptions-item>
               <el-descriptions-item label="实际解决时间">{{ parseTime(slaRecord.resolutionActual) || '-' }}</el-descriptions-item>
               <el-descriptions-item label="预警次数">{{ slaRecord.warningCount || 0 }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 15px; text-align: right">
               <el-button v-if="ticket.status === 'ASSIGNED' || ticket.status === 'PROCESSING'" type="warning" @click="handleReassign" v-hasPermi="['itsm:ticket:assign']">
                  转派
               </el-button>
               <el-button v-for="t in availableTransitions" :key="t.code" type="primary" @click="handleTransit(t)" v-hasPermi="t.permission ? [t.permission] : []">
                  {{ t.label }}
               </el-button>
            </div>
         </el-card>

         <el-tabs v-model="activeTab" style="margin-top: 15px">
            <el-tab-pane label="附件管理" name="attachment">
               <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                     <el-upload
                        :action="uploadUrl"
                        :headers="uploadHeaders"
                        :before-upload="handleBeforeUpload"
                        :on-success="handleUploadSuccess"
                        :on-error="handleUploadError"
                        :show-file-list="false"
                        :data="{ ticketId: ticketId }"
                        v-hasPermi="['itsm:ticket:edit']"
                     >
                        <el-button type="primary" icon="Upload">上传附件</el-button>
                     </el-upload>
                  </el-col>
               </el-row>
               <el-table :data="attachmentList" border>
                  <el-table-column label="文件名" prop="fileName" min-width="250">
                     <template #default="scope">
                        <el-link type="primary" @click="handleDownload(scope.row)">{{ scope.row.fileName }}</el-link>
                     </template>
                  </el-table-column>
                  <el-table-column label="文件大小" prop="fileSize" width="120" align="center">
                     <template #default="scope">
                        {{ formatFileSize(scope.row.fileSize) }}
                     </template>
                  </el-table-column>
                  <el-table-column label="文件类型" prop="fileType" width="100" align="center" />
                  <el-table-column label="上传人" prop="uploadBy" width="120" align="center" />
                  <el-table-column label="上传时间" prop="uploadTime" width="160" align="center">
                     <template #default="scope">
                        <span>{{ parseTime(scope.row.uploadTime) }}</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" align="center">
                     <template #default="scope">
                        <el-button link type="danger" icon="Delete" @click="handleDeleteAttachment(scope.row)" v-hasPermi="['itsm:ticket:edit']">删除</el-button>
                     </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="操作日志" name="log">
               <TicketTimeline :logs="logList" />
            </el-tab-pane>

            <el-tab-pane label="流转记录" name="transition">
               <el-table :data="logList" border>
                  <el-table-column label="操作类型" prop="actionName" width="120" align="center" />
                  <el-table-column label="原状态" prop="fromStatus" width="120" align="center">
                     <template #default="scope">
                        <dict-tag v-if="scope.row.fromStatus" :options="itsm_ticket_status" :value="scope.row.fromStatus" />
                        <span v-else>-</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="目标状态" prop="toStatus" width="120" align="center">
                     <template #default="scope">
                        <dict-tag v-if="scope.row.toStatus" :options="itsm_ticket_status" :value="scope.row.toStatus" />
                        <span v-else>-</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="操作人" prop="operatorName" width="120" align="center" />
                  <el-table-column label="备注" prop="comment" min-width="200" :show-overflow-tooltip="true" />
                  <el-table-column label="操作时间" prop="operateTime" width="160" align="center">
                     <template #default="scope">
                        <span>{{ parseTime(scope.row.operateTime) }}</span>
                     </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="处理记录" name="process-log">
               <el-table :data="processLogList" border>
                  <el-table-column label="操作类型" prop="actionName" width="120" align="center" />
                  <el-table-column label="原状态" prop="fromStatus" width="120" align="center">
                     <template #default="scope">
                        <dict-tag v-if="scope.row.fromStatus" :options="itsm_ticket_status" :value="scope.row.fromStatus" />
                        <span v-else>-</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="新状态" prop="toStatus" width="120" align="center">
                     <template #default="scope">
                        <dict-tag v-if="scope.row.toStatus" :options="itsm_ticket_status" :value="scope.row.toStatus" />
                        <span v-else>-</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="操作人" prop="operatorName" width="120" align="center" />
                  <el-table-column label="处理内容" prop="content" min-width="200" :show-overflow-tooltip="true" />
                  <el-table-column label="工时(h)" prop="workHours" width="80" align="center" />
                  <el-table-column label="操作时间" prop="operateTime" width="160" align="center">
                     <template #default="scope">
                        <span>{{ parseTime(scope.row.operateTime) }}</span>
                     </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>
         </el-tabs>
      </div>

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

      <el-dialog title="工单转派" v-model="reassignOpen" width="500px" append-to-body>
         <el-form ref="reassignRef" :model="reassignForm" :rules="assignRules" label-width="100px">
            <el-form-item label="处理人" prop="assigneeId">
               <el-select v-model="reassignForm.assigneeId" placeholder="请选择处理人" filterable>
                  <el-option
                     v-for="user in userList"
                     :key="user.userId"
                     :label="user.nickName"
                     :value="user.userId"
                  />
               </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="reassignForm.remark" type="textarea" placeholder="请输入转派备注" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitReassign">确 定</el-button>
               <el-button @click="reassignOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <TicketProcessForm
         v-model:visible="transitOpen"
         :transitions="availableTransitions"
         :model-value="transitForm"
         @submit="submitTransit"
      />
   </div>
</template>

<script setup name="TicketDetail">
import { getTicket, approveTicket, assignTicket, reassignTicket, transitTicket, getAvailableTransitions, getAvailableWorkflowTransitions, listProcessLog, listAttachment, delAttachment, listTicketLog, getSlaRecord } from "@/api/itsm/ticket"
import { listUser } from "@/api/system/user"
import { getToken } from "@/utils/auth"
import request from "@/utils/request"
import TicketStatusTag from "./components/TicketStatusTag.vue"
import TicketTimeline from "./components/TicketTimeline.vue"
import TicketProcessForm from "./components/TicketProcessForm.vue"

const { proxy } = getCurrentInstance()
const { itsm_ticket_priority, itsm_ticket_status, itsm_ticket_source } = useDict("itsm_ticket_priority", "itsm_ticket_status", "itsm_ticket_source")

const route = useRoute()
const router = useRouter()

const ticketId = ref(null)
const ticket = ref({})
const loading = ref(true)
const activeTab = ref("attachment")
const attachmentList = ref([])
const logList = ref([])
const availableTransitions = ref([])
const processLogList = ref([])
const userList = ref([])

const approveOpen = ref(false)
const assignOpen = ref(false)
const reassignOpen = ref(false)
const transitOpen = ref(false)
const approveForm = ref({})
const assignForm = ref({})
const reassignForm = ref({})
const transitForm = ref({})
const slaRecord = ref(null)

const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/itsm/ticket/attachment/upload")
const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

const approveRules = {
   status: [{ required: true, message: "审批结果不能为空", trigger: "change" }]
}
const assignRules = {
   assigneeId: [{ required: true, message: "处理人不能为空", trigger: "change" }]
}

function goBack() {
   router.push("/itsm/ticket")
}

function loadTicket() {
   loading.value = true
   getTicket(ticketId.value).then(response => {
      ticket.value = response.data
      loading.value = false
   })
}

function loadAttachments() {
   listAttachment(ticketId.value).then(response => {
      attachmentList.value = response.data || []
   })
}

function loadLogs() {
   listTicketLog(ticketId.value).then(response => {
      logList.value = response.data || []
   })
}

function loadAvailableTransitions() {
   getAvailableWorkflowTransitions(ticketId.value).then(response => {
      availableTransitions.value = (response.data || []).map(t => ({
         code: t.toNodeKey,
         label: t.transitionName,
         action: t.action,
         permission: t.permission
      }))
   }).catch(() => {
      getAvailableTransitions(ticketId.value).then(response => {
         availableTransitions.value = (response.data || []).map(s => ({
            code: s.code,
            label: s.desc,
            permission: 'itsm:ticket:edit'
         }))
      })
   })
}

function loadProcessLogs() {
   listProcessLog(ticketId.value).then(response => {
      processLogList.value = response.data || []
   })
}

function loadSlaRecord() {
   getSlaRecord(ticketId.value).then(response => {
      slaRecord.value = response.data || null
   }).catch(() => {
      slaRecord.value = null
   })
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

const nodeLabelMap = {
   DRAFT: '草稿', SUBMITTED: '待审批', APPROVED: '已审批', ASSIGNED: '已分配',
   PROCESSING: '处理中', RESOLVED: '已解决', VERIFIED: '已验证', CLOSED: '已关闭',
   REJECTED: '已驳回', CANCELLED: '已取消'
}
function getNodeLabel(nodeKey) {
   return nodeLabelMap[nodeKey] || nodeKey
}

function handleBeforeUpload(file) {
   const maxSize = 50 * 1024 * 1024
   if (file.size > maxSize) {
      proxy.$modal.msgError("上传文件大小不能超过 50MB!")
      return false
   }
   proxy.$modal.loading("正在上传文件，请稍候...")
   return true
}

function handleUploadSuccess(res) {
   proxy.$modal.closeLoading()
   if (res.code === 200) {
      proxy.$modal.msgSuccess("上传成功")
      loadAttachments()
   } else {
      proxy.$modal.msgError(res.msg || "上传失败")
   }
}

function handleUploadError() {
   proxy.$modal.closeLoading()
   proxy.$modal.msgError("上传文件失败")
}

function handleDownload(row) {
   const url = "/itsm/ticket/attachment/download/" + row.attachmentId
   request({
      url: url,
      method: "get",
      responseType: "blob"
   }).then(data => {
      const blob = new Blob([data])
      const link = document.createElement("a")
      link.href = URL.createObjectURL(blob)
      link.download = row.fileName
      link.click()
      URL.revokeObjectURL(link.href)
   }).catch(() => {
      proxy.$modal.msgError("下载附件失败")
   })
}

function handleDeleteAttachment(row) {
   proxy.$modal.confirm('是否确认删除附件"' + row.fileName + '"？').then(function() {
      return delAttachment(row.attachmentId)
   }).then(() => {
      loadAttachments()
      proxy.$modal.msgSuccess("删除成功")
   }).catch(() => {})
}

function formatFileSize(bytes) {
   if (!bytes) return "0 B"
   const units = ["B", "KB", "MB", "GB"]
   let i = 0
   let size = bytes
   while (size >= 1024 && i < units.length - 1) {
      size /= 1024
      i++
   }
   return size.toFixed(i === 0 ? 0 : 2) + " " + units[i]
}

function handleApprove() {
   approveForm.value = {
      ticketId: ticketId.value,
      status: "APPROVED",
      remark: undefined
   }
   approveOpen.value = true
}

function handleAssign() {
   assignForm.value = {
      ticketId: ticketId.value,
      assigneeId: undefined,
      remark: undefined
   }
   listUser({ pageNum: 1, pageSize: 9999 }).then(response => {
      userList.value = response.rows
   })
   assignOpen.value = true
}

function handleTransit(transition) {
   transitForm.value = {
      ticketId: ticketId.value,
      targetStatus: transition.code,
      remark: undefined
   }
   transitOpen.value = true
}

function submitApprove() {
   proxy.$refs["approveRef"].validate(valid => {
      if (valid) {
         approveTicket(approveForm.value).then(() => {
            proxy.$modal.msgSuccess("审批成功")
            approveOpen.value = false
            loadTicket()
            loadAvailableTransitions()
         })
      }
   })
}

function submitAssign() {
   proxy.$refs["assignRef"].validate(valid => {
      if (valid) {
         assignTicket(assignForm.value).then(() => {
            proxy.$modal.msgSuccess("分配成功")
            assignOpen.value = false
            loadTicket()
            loadAvailableTransitions()
         })
      }
   })
}

function handleReassign() {
   reassignForm.value = {
      ticketId: ticketId.value,
      assigneeId: undefined,
      remark: undefined
   }
   listUser({ pageNum: 1, pageSize: 9999 }).then(response => {
      userList.value = response.rows
   })
   reassignOpen.value = true
}

function submitReassign() {
   proxy.$refs["reassignRef"].validate(valid => {
      if (valid) {
         reassignTicket(reassignForm.value).then(() => {
            proxy.$modal.msgSuccess("转派成功")
            reassignOpen.value = false
            loadTicket()
            loadAvailableTransitions()
         })
      }
   })
}

function submitTransit(formData) {
   transitTicket(formData).then(() => {
      proxy.$modal.msgSuccess("流转成功")
      transitOpen.value = false
      loadTicket()
      loadAvailableTransitions()
   })
}

onMounted(() => {
   ticketId.value = route.params.ticketId || route.query.ticketId
   if (ticketId.value) {
      loadTicket()
      loadAttachments()
      loadLogs()
      loadAvailableTransitions()
      loadProcessLogs()
      loadSlaRecord()
   }
})
</script>

<style scoped>
.ticket-header-card {
   margin-bottom: 15px;
}
</style>
