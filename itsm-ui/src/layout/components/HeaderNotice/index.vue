<template>
  <div>
    <el-popover ref="noticePopover" placement="bottom-end" :width="360" trigger="manual" v-model:visible="noticeVisible" popper-class="notice-popover">
      <div class="notice-header">
        <span class="notice-title">消息通知</span>
        <span class="notice-mark-all" @click="markAllRead">全部已读</span>
      </div>
      <el-tabs v-model="activeTab" class="notice-tabs">
        <el-tab-pane label="系统公告" name="system">
          <div v-if="noticeLoading" class="notice-loading">
            <el-icon class="is-loading"><Loading /></el-icon> 加载中...
          </div>
          <div v-else-if="noticeList.length === 0" class="notice-empty">
            <el-icon style="font-size:24px;display:block;margin-bottom:6px;"><Postcard /></el-icon>
            暂无公告
          </div>
          <div v-else>
            <div v-for="item in noticeList" :key="item.noticeId" class="notice-item" :class="{ 'is-read': item.isRead }" @click="previewNotice(item)">
              <el-tag size="small" :type="item.noticeType === '1' ? 'warning' : 'success'" class="notice-tag">
                {{ item.noticeType === '1' ? '通知' : '公告' }}
              </el-tag>
              <span class="notice-item-title">{{ item.noticeTitle }}</span>
              <span class="notice-item-date">{{ item.createTime }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane name="itsm">
          <template #label>
            工单通知<el-badge v-if="itsmUnreadCount > 0" :value="itsmUnreadCount" :max="99" class="itsm-tab-badge" />
          </template>
          <div v-if="itsmLoading" class="notice-loading">
            <el-icon class="is-loading"><Loading /></el-icon> 加载中...
          </div>
          <div v-else-if="itsmList.length === 0" class="notice-empty">
            <el-icon style="font-size:24px;display:block;margin-bottom:6px;"><Bell /></el-icon>
            暂无工单通知
          </div>
          <div v-else>
            <div v-for="item in itsmList" :key="item.notificationId" class="notice-item" :class="{ 'is-read': item.isRead === '1' }" @click="handleItsmNotification(item)">
              <el-tag size="small" :type="getItsmTagType(item.notificationType)" class="notice-tag">
                {{ getItsmTagLabel(item.notificationType) }}
              </el-tag>
              <span class="notice-item-title">{{ item.title }}</span>
              <span class="notice-item-date">{{ parseTime(item.createTime, '{m}-{d} {h}:{i}') }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #reference>
        <div class="right-menu-item hover-effect notice-trigger" @mouseenter="onNoticeEnter" @mouseleave="onNoticeLeave">
          <svg-icon icon-class="bell" />
          <span v-if="totalUnread > 0" class="notice-badge">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
        </div>
      </template>
    </el-popover>

    <notice-detail-view ref="noticeViewRef" />
  </div>
</template>

<script setup>
import NoticeDetailView from './DetailView'
import { listNoticeTop, markNoticeRead, markNoticeReadAll } from '@/api/system/notice'
import { listNotification, getUnreadCount, markAsRead, markAllAsRead } from '@/api/itsm/notification'
import { connectWebSocket, disconnectWebSocket, subscribeToUserNotifications } from '@/utils/websocket'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

const noticePopover = ref(null)
const noticeList = ref([])
const unreadCount = ref(0)
const noticeLoading = ref(false)
const noticeVisible = ref(false)
const noticeLeaveTimer = ref(null)

const activeTab = ref('system')
const itsmList = ref([])
const itsmUnreadCount = ref(0)
const itsmLoading = ref(false)

const totalUnread = computed(() => unreadCount.value + itsmUnreadCount.value)

function loadNoticeTop() {
  noticeLoading.value = true
  listNoticeTop().then(res => {
    noticeList.value = res.data || []
    unreadCount.value = res.unreadCount !== undefined ? res.unreadCount : noticeList.value.filter(n => !n.isRead).length
  }).finally(() => {
    noticeLoading.value = false
  })
}

function loadItsmNotifications() {
  itsmLoading.value = true
  listNotification({ pageNum: 1, pageSize: 20 }).then(res => {
    itsmList.value = res.rows || []
  }).finally(() => {
    itsmLoading.value = false
  })
  getUnreadCount().then(res => {
    itsmUnreadCount.value = res.data || 0
  }).catch(() => {})
}

function initWebSocket() {
  const token = getToken()
  if (!token) return
  connectWebSocket()
  subscribeToUserNotifications(null, (data) => {
    itsmList.value.unshift(data)
    itsmUnreadCount.value++
    proxy.$modal.msgSuccess(data.title || '收到新的工单通知')
  })
}

onMounted(() => {
  loadNoticeTop()
  loadItsmNotifications()
  initWebSocket()
})

onBeforeUnmount(() => {
  disconnectWebSocket()
})

function onNoticeEnter() {
  clearTimeout(noticeLeaveTimer.value)
  noticeVisible.value = true
  nextTick(() => {
    const popper = noticePopover.value?.popperRef?.contentRef
    if (popper && !popper._noticeBound) {
      popper._noticeBound = true
      popper.addEventListener('mouseenter', () => clearTimeout(noticeLeaveTimer.value))
      popper.addEventListener('mouseleave', () => {
        noticeLeaveTimer.value = setTimeout(() => { noticeVisible.value = false }, 100)
      })
    }
  })
}

function onNoticeLeave() {
  noticeLeaveTimer.value = setTimeout(() => { noticeVisible.value = false }, 150)
}

function previewNotice(item) {
  if (!item.isRead) {
    markNoticeRead(item.noticeId).catch(() => {})
    const idx = noticeList.value.indexOf(item)
    if (idx !== -1) noticeList.value[idx] = { ...item, isRead: true }
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  proxy.$refs["noticeViewRef"].open(item.noticeId)
}

function handleItsmNotification(item) {
  if (item.isRead === '0') {
    markAsRead(item.notificationId).then(() => {
      item.isRead = '1'
      itsmUnreadCount.value = Math.max(0, itsmUnreadCount.value - 1)
    }).catch(() => {})
  }
  if (item.businessType === 'TICKET' && item.businessId) {
    proxy.$router.push({ path: '/itsm/ticket/detail', query: { ticketId: item.businessId } })
    noticeVisible.value = false
  }
}

function markAllRead() {
  if (activeTab.value === 'system') {
    const ids = noticeList.value.map(n => n.noticeId).join(',')
    if (!ids) return
    markNoticeReadAll(ids).catch(() => {})
    noticeList.value = noticeList.value.map(n => ({ ...n, isRead: true }))
    unreadCount.value = 0
  } else {
    markAllAsRead().then(() => {
      itsmList.value.forEach(n => { n.isRead = '1' })
      itsmUnreadCount.value = 0
    }).catch(() => {})
  }
}

function getItsmTagType(type) {
  const map = { SYSTEM: '', TICKET: 'warning', SLA: 'danger', APPROVAL: 'success' }
  return map[type] || 'info'
}

function getItsmTagLabel(type) {
  const map = { SYSTEM: '系统', TICKET: '工单', SLA: 'SLA', APPROVAL: '审批' }
  return map[type] || '通知'
}
</script>

<style lang="scss" scoped>
.notice-trigger {
  position: relative;
  transform: translateX(-6px);
  .svg-icon { width: 1.2em; height: 1.2em; vertical-align: -0.2em; }
  .notice-badge {
    position: absolute;
    top: 7px;
    right: -3px;
    background: #f56c6c;
    color: #fff;
    border-radius: 10px;
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    min-width: 16px;
    text-align: center;
    white-space: nowrap;
    pointer-events: none;
  }
}
.notice-popover { padding: 0 !important; }
.notice-popover .notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #f7f9fb;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.notice-popover .notice-mark-all {
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: normal;
  cursor: pointer;
}
.notice-popover .notice-mark-all:hover { color: #2b7cc1; }
.notice-popover .notice-tabs {
  :deep(.el-tabs__header) { margin: 0; padding: 0 14px; }
  :deep(.el-tabs__nav-wrap::after) { height: 1px; }
  :deep(.el-tabs__item) { font-size: 12px; height: 36px; line-height: 36px; }
}
.itsm-tab-badge {
  margin-left: 4px;
  :deep(.el-badge__content) { top: -4px; }
}
.notice-popover .notice-loading,
.notice-popover .notice-empty {
  padding: 24px;
  text-align: center;
  color: #bbb;
  font-size: 12px;
  line-height: 1.8;
}
.notice-popover .notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.notice-popover .notice-item:last-child { border-bottom: none; }
.notice-popover .notice-item:hover { background: #f7f9fb; }
.notice-popover .notice-item.is-read .notice-tag,
.notice-popover .notice-item.is-read .notice-item-title,
.notice-popover .notice-item.is-read .notice-item-date { opacity: 0.45; filter: grayscale(1); color: #999; }
.notice-popover .notice-tag { flex-shrink: 0; }
.notice-popover .notice-item-title {
  flex: 1;
  font-size: 12px;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.notice-popover .notice-item-date {
  flex-shrink: 0;
  font-size: 11px;
  color: #bbb;
}
</style>
