import request from '@/utils/request'

export function listWorkflow(query) {
  return request({ url: '/itsm/workflow/list', method: 'get', params: query })
}

export function getWorkflow(workflowId) {
  return request({ url: '/itsm/workflow/' + workflowId, method: 'get' })
}

export function addWorkflow(data) {
  return request({ url: '/itsm/workflow', method: 'post', data: data })
}

export function updateWorkflow(data) {
  return request({ url: '/itsm/workflow', method: 'put', data: data })
}

export function delWorkflow(workflowId) {
  return request({ url: '/itsm/workflow/' + workflowId, method: 'delete' })
}
