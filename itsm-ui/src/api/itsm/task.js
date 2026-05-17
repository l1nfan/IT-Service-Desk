import request from '@/utils/request'

export function listPendingTask(query) {
  return request({ url: '/itsm/task/pending', method: 'get', params: query })
}

export function listCompletedTask(query) {
  return request({ url: '/itsm/task/completed', method: 'get', params: query })
}
