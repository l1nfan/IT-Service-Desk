import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { getToken } from '@/utils/auth'

let stompClient = null
let subscriptions = {}
let pendingSubscriptions = []

export function connectWebSocket() {
  const token = getToken()
  if (!token) return
  if (stompClient && stompClient.active) return

  stompClient = new Client({
    webSocketFactory: () => new SockJS(import.meta.env.VITE_APP_BASE_API + '/ws?token=' + token),
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      console.log('WebSocket connected')
      reSubscribeAll()
      processPending()
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame)
    }
  })

  stompClient.activate()
}

function reSubscribeAll() {
  const entries = Object.entries(subscriptions)
  subscriptions = {}
  for (const [key, { destination, callback }] of entries) {
    const sub = stompClient.subscribe(destination, (message) => {
      const data = JSON.parse(message.body)
      callback(data)
    })
    subscriptions[key] = { destination, callback, sub }
  }
}

export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    subscriptions = {}
  }
}

export function subscribeToUserNotifications(userId, callback) {
  const destination = '/user/queue/notifications'
  if (!stompClient || !stompClient.connected) {
    pendingSubscriptions.push({ destination, callback })
    return
  }
  const sub = stompClient.subscribe(destination, (message) => {
    const data = JSON.parse(message.body)
    callback(data)
  })
  subscriptions['user-notifications'] = { destination, callback, sub }
}

export function subscribeToTicket(ticketId, callback) {
  const destination = '/topic/ticket/' + ticketId
  if (!stompClient || !stompClient.connected) {
    pendingSubscriptions.push({ destination, callback })
    return
  }
  const sub = stompClient.subscribe(destination, (message) => {
    const data = JSON.parse(message.body)
    callback(data)
  })
  subscriptions['ticket-' + ticketId] = { destination, callback, sub }
}

function processPending() {
  for (const { destination, callback } of pendingSubscriptions) {
    const sub = stompClient.subscribe(destination, (message) => {
      const data = JSON.parse(message.body)
      callback(data)
    })
    const key = 'pending-' + destination.replace(/[^a-zA-Z0-9]/g, '-')
    subscriptions[key] = { destination, callback, sub }
  }
  pendingSubscriptions = []
}

export function unsubscribe(key) {
  if (subscriptions[key]) {
    subscriptions[key].sub.unsubscribe()
    delete subscriptions[key]
  }
}
