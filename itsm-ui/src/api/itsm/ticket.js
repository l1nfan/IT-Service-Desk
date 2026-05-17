import request from '@/utils/request'

export function listTicket(query) {
  return request({
    url: '/itsm/ticket/list',
    method: 'get',
    params: query
  })
}

export function getTicket(ticketId) {
  return request({
    url: '/itsm/ticket/' + ticketId,
    method: 'get'
  })
}

export function addTicket(data) {
  return request({
    url: '/itsm/ticket',
    method: 'post',
    data: data
  })
}

export function updateTicket(data) {
  return request({
    url: '/itsm/ticket',
    method: 'put',
    data: data
  })
}

export function delTicket(ticketId) {
  return request({
    url: '/itsm/ticket/' + ticketId,
    method: 'delete'
  })
}

export function assignTicket(data) {
  return request({
    url: '/itsm/ticket/assign',
    method: 'put',
    data: data
  })
}

export function reassignTicket(data) {
  return request({
    url: '/itsm/ticket/reassign',
    method: 'put',
    data: data
  })
}

export function approveTicket(data) {
  return request({
    url: '/itsm/ticket/approve',
    method: 'put',
    data: data
  })
}

export function transitTicket(data) {
  return request({
    url: '/itsm/ticket/transit',
    method: 'put',
    data: data
  })
}

export function getAvailableTransitions(ticketId) {
  return request({
    url: '/itsm/ticket/transitions/' + ticketId,
    method: 'get'
  })
}

export function listAttachment(ticketId) {
  return request({
    url: '/itsm/ticket/attachment/list/' + ticketId,
    method: 'get'
  })
}

export function delAttachment(attachmentId) {
  return request({
    url: '/itsm/ticket/attachment/' + attachmentId,
    method: 'delete'
  })
}

export function listTicketLog(ticketId) {
  return request({
    url: '/itsm/ticket/log/' + ticketId,
    method: 'get'
  })
}

export function getAvailableWorkflowTransitions(ticketId) {
  return request({
    url: '/itsm/ticket/workflow-transitions/' + ticketId,
    method: 'get'
  })
}

export function listProcessLog(ticketId) {
  return request({
    url: '/itsm/ticket/process-log/' + ticketId,
    method: 'get'
  })
}

export function getSlaRecord(ticketId) {
  return request({
    url: '/itsm/ticket/sla/' + ticketId,
    method: 'get'
  })
}
