import request from '@/utils/request'

export function listSla(query) {
  return request({ url: '/itsm/sla/list', method: 'get', params: query })
}

export function getSla(slaId) {
  return request({ url: '/itsm/sla/' + slaId, method: 'get' })
}

export function addSla(data) {
  return request({ url: '/itsm/sla', method: 'post', data: data })
}

export function updateSla(data) {
  return request({ url: '/itsm/sla', method: 'put', data: data })
}

export function delSla(slaIds) {
  return request({ url: '/itsm/sla/' + slaIds, method: 'delete' })
}
