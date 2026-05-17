import request from '@/utils/request'

export function listNotification(query) {
  return request({ url: '/itsm/notification/list', method: 'get', params: query })
}

export function getUnreadCount() {
  return request({ url: '/itsm/notification/unread-count', method: 'get' })
}

export function markAsRead(notificationId) {
  return request({ url: '/itsm/notification/read/' + notificationId, method: 'put' })
}

export function markAllAsRead() {
  return request({ url: '/itsm/notification/read-all', method: 'put' })
}

export function delNotification(notificationId) {
  return request({ url: '/itsm/notification/' + notificationId, method: 'delete' })
}
