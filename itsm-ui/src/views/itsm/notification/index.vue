<template>
   <div class="app-container">
      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Check"
               @click="handleMarkAllRead"
               :disabled="unreadCount === 0"
            >全部已读</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-tag type="danger" v-if="unreadCount > 0" size="large">{{ unreadCount }} 条未读</el-tag>
            <el-tag type="success" v-else size="large">全部已读</el-tag>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="notificationList">
         <el-table-column label="通知标题" align="center" prop="title" min-width="200" :show-overflow-tooltip="true" />
         <el-table-column label="通知内容" align="center" prop="content" min-width="300" :show-overflow-tooltip="true" />
         <el-table-column label="通知类型" align="center" prop="notificationType" width="120">
            <template #default="scope">
               <el-tag v-if="scope.row.notificationType === 'SYSTEM'" type="info">系统通知</el-tag>
               <el-tag v-else-if="scope.row.notificationType === 'TICKET'" type="primary">工单通知</el-tag>
               <el-tag v-else-if="scope.row.notificationType === 'SLA'" type="warning">SLA预警</el-tag>
               <el-tag v-else-if="scope.row.notificationType === 'APPROVAL'" type="success">审批通知</el-tag>
               <el-tag v-else>{{ scope.row.notificationType }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="isRead" width="100">
            <template #default="scope">
               <el-tag v-if="scope.row.isRead === '0'" type="danger">未读</el-tag>
               <el-tag v-else type="success">已读</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="发送时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
            <template #default="scope">
               <el-button v-if="scope.row.isRead === '0'" link type="primary" icon="Check" @click="handleMarkRead(scope.row)">已读</el-button>
               <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
   </div>
</template>

<script setup name="ItsmNotification">
import { listNotification, getUnreadCount, markAsRead, markAllAsRead, delNotification } from "@/api/itsm/notification"

const { proxy } = getCurrentInstance()

const notificationList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const unreadCount = ref(0)

const data = reactive({
   queryParams: {
      pageNum: 1,
      pageSize: 10
   }
})

const { queryParams } = toRefs(data)

function getList() {
   loading.value = true
   listNotification(queryParams.value).then(response => {
      notificationList.value = response.rows
      total.value = response.total
      loading.value = false
   })
}

function loadUnreadCount() {
   getUnreadCount().then(response => {
      unreadCount.value = response.data || 0
   })
}

function handleMarkRead(row) {
   markAsRead(row.notificationId).then(() => {
      proxy.$modal.msgSuccess("标记已读成功")
      getList()
      loadUnreadCount()
   })
}

function handleMarkAllRead() {
   proxy.$modal.confirm("是否确认将所有通知标记为已读？").then(function() {
      return markAllAsRead()
   }).then(() => {
      proxy.$modal.msgSuccess("全部标记已读成功")
      getList()
      loadUnreadCount()
   }).catch(() => {})
}

function handleDelete(row) {
   proxy.$modal.confirm('是否确认删除该通知？').then(function() {
      return delNotification(row.notificationId)
   }).then(() => {
      getList()
      loadUnreadCount()
      proxy.$modal.msgSuccess("删除成功")
   }).catch(() => {})
}

getList()
loadUnreadCount()
</script>
