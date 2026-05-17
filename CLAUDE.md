# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**IT运维工单管理系统 (IT Service Desk Ticketing System)** — graduation project built on the ITSM-Desk framework (originally RuoYi-Vue 3.9.2, fully renamed).

- **Backend**: Spring Boot 4.0.6, Java 25, MyBatis Plus 3.5.15, JWT + Redis auth
- **Frontend**: Vue 3.5 + Element Plus 2.13 + Pinia 3.0 + ECharts 5.6 (in `itsm-ui/`)
- **Database**: MySQL 8.x (database name: `itsm`), Redis
- **Docs**: `docs/` — project plan, tech assessment, work status, defect audit

## Build & Run

```bash
mvn clean compile -DskipTests              # compile all modules
mvn test -pl itsm-ticket,itsm-admin -am   # run 100 unit tests (71 ticket + 29 admin)
cd itsm-admin && mvn spring-boot:run      # dev server (hot-reload)
cd itsm-ui && npm run dev                 # frontend dev server
npm run build:prod                        # frontend production build
```

- **Entry**: `com.itsm.ItsmApplication`
- **Port**: `8080`, context `/`
- **Profile**: `druid` (see `application-druid.yml`)
- **Swagger**: `http://localhost:8080/swagger-ui.html`
- **Druid monitor**: `http://localhost:8080/druid/`
- **Login**: `admin` / `admin123`
- **JWT secret**: 64-byte key in `application.yml` (required for HS512, `< 64 bytes will fail`)

## Module Architecture

| Module | Status | Purpose |
|---|---|---|
| `itsm-admin` | ✅ Running | Application entry, web controllers |
| `itsm-common` | ✅ | Shared utils, annotations, enums, BaseEntity, BaseController |
| `itsm-framework` | ✅ | Security, Druid, Redis, MyBatis Plus, aspects, token service, WebSocket config |
| `itsm-system` | ✅ | Users, roles, menus, depts, dicts, notices |
| `itsm-quartz` | ✅ | Scheduled task management (depends on itsm-ticket for SLA jobs) |
| `itsm-generator` | ✅ | Code generator (Freemarker templates) |
| `itsm-ticket` | ✅ **Built** | IT service desk core: tickets, categories, workflow, SLA, notifications, events |
| `itsm-ui` | ✅ | Vue 3 frontend (in `itsm-ui/`) |

**Dependency chain**: `itsm-admin` → `itsm-framework` + `itsm-quartz` + `itsm-generator` + `itsm-ticket`; `itsm-framework` → `itsm-system` → `itsm-common`; `itsm-quartz` → `itsm-ticket`

## Current Progress

| Iteration | Status | Deliverables |
|---|---|---|
| Iteration 0 | ✅ 100% | Environment, modules, DB, branches, code standards |
| Iteration 1 | ✅ 100% | Ticket CRUD, categories, attachments, state machine, 50 tests |
| Iteration 2 | ✅ 100% | Workflow engine, process log, notifications, WebSocket+STOMP, Spring Events, SLA config + monitoring |
| Iteration 3 | ⏳ Next | Email + knowledge base + evaluation |
| Iteration 4 | ⏳ | Statistics dashboard + reports + workflow designer |

**Overall**: ~82%, 103 tests passing, frontend build passing.

### itsm-ticket Module Structure (Iteration 2 Complete)

```
itsm-ticket/src/main/java/com/itsm/ticket/
  domain/ticket/        ItTicket, ItTicketCategory, ItTicketAttachment, ItTicketLog,
                        ItTicketProcessLog, ItNotification, ItSlaConfig, ItSlaRecord, TicketTransitDTO
  domain/workflow/      WfWorkflow, WfNode, WfTransition, WfInstance, WfTask
  enums/                TicketStatus (10 states), TicketTransition (17 transitions), SlaStatus
  statemachine/         TicketStateMachine (validation engine)
  event/                TicketEvent, TicketEventListener
  mapper/ticket/        7 Mappers: ItTicket, ItTicketCategory, ItTicketAttachment, ItTicketLog,
                        ItTicketProcessLog, ItNotification, ItSlaConfig, ItSlaRecord
  mapper/workflow/      5 Mappers: WfWorkflow, WfNode, WfTransition, WfInstance, WfTask
  service/ticket/impl/  8 Service pairs: ticket, category, attachment, log, processLog,
                        notification, slaConfig, slaRecord
  service/workflow/     4 Services: IWorkflowEngine, IWfWorkflowService, IWfInstanceService,
                        IWfTaskService, WebSocketPushService
  config/               ItsmModuleConfig

itsm-admin/.../controller/itsm/
  ItTicketController          (15 APIs: CRUD + approve + assign + reassign + transit + log + transitions + process-log + sla)
  ItTicketCategoryController  (5 APIs: CRUD + tree)
  ItTicketAttachmentController(4 APIs: upload/download/list/delete)
  WfWorkflowController        (5 APIs: CRUD)
  WfTaskController            (2 APIs: pending/completed tasks)
  ItNotificationController    (5 APIs: list/unread-count/read/read-all/delete)
  ItSlaConfigController       (5 APIs: CRUD)

itsm-framework/.../
  config/WebSocketConfig.java           STOMP broker + endpoint config
  interceptor/WebSocketAuthInterceptor  JWT handshake validation (?token= param)
  interceptor/StompChannelInterceptor   STOMP CONNECT Principal registration + token expiry check on SUBSCRIBE/SEND

itsm-quartz/.../task/
  SlaMonitorJob               SLA monitoring job (cron: 0 0/5 * * * ?, WARNING/BREACHED detection)

itsm-ticket/src/test/         7 test classes, 74 tests
itsm-admin/src/test/          29 tests
```

### Frontend Pages (Iteration 2 Complete)

```
itsm-ui/src/views/itsm/
  ticket/index.vue          Ticket list (search/filter/paginate/CRUD/approve/assign + SLA status column)
  ticket/detail.vue         Ticket detail (info + SLA info + transitions + attachments + logs + reassign)
  ticket/components/        TicketProcessForm, TicketStatusTag, TicketTimeline
  category/index.vue        Category tree table (CRUD)
  workflow/index.vue        Workflow list (CRUD)
  notification/index.vue    Notification list (mark read/delete)
  sla/config.vue            SLA config list (CRUD)
  dashboard/index.vue       Dashboard placeholder

itsm-ui/src/api/itsm/
  ticket.js (15), category.js (5), workflow.js (5), notification.js (5), task.js (2), sla.js (5)

itsm-ui/src/utils/websocket.js   STOMP client (connect/subscribe/resubscribe on reconnect)
itsm-ui/src/layout/components/HeaderNotice/  Notification bell (sys notices + ITSM notifications)
```

## Ticket State Machine

10 statuses: `DRAFT → SUBMITTED → APPROVED → ASSIGNED → PROCESSING → RESOLVED → VERIFIED → CLOSED` (with REJECTED, CANCELLED branches).

17 transitions in `TicketTransition` enum (15 standard + 2 REASSIGN). `TicketStateMachine.validateTransition()` validates all state changes. Each transition has a required permission.

## Workflow Engine (Iteration 2)

Dynamic workflow backed by `wf_workflow`/`wf_node`/`wf_transition`/`wf_instance`/`wf_task` tables. Default seed data in `sql/itsm_workflow.sql` defines a 10-node standard IT ticket flow.

`WorkflowEngineImpl` provides: `startProcess()` (creates instance + initial task), `transit()` (validates transition, completes current task, creates next pending task), `getAvailableTransitions()`, `terminateProcess()`.

Task assignee resolution from `WfNode` config: `INITIATOR` type → ticket creator; `USER` type → parse `assignee_value` as user ID.

Event-driven integration: `ItTicketServiceImpl` publishes `TicketEvent` on CREATE/ASSIGN/APPROVE/transit. `TicketEventListener` (@Async, @TransactionalEventListener(AFTER_COMMIT)) handles: notification creation, SLA record lifecycle, WebSocket push.

## WebSocket + STOMP

- **Config**: `WebSocketConfig` enables STOMP over SockJS at `/ws`, simple broker on `/topic` and `/queue`, user prefix `/user`
- **Auth**: `WebSocketAuthInterceptor` validates JWT from `?token=` query param at handshake. `StompChannelInterceptor` reads `LoginUser` from session attributes at STOMP CONNECT and registers a `Principal` for `convertAndSendToUser` routing
- **Push**: `WebSocketPushService` — `pushNotification(userId, data)` (via `/user/{userId}/queue/notifications`), `broadcastTicketUpdate(ticketId, data)` (via `/topic/ticket/{id}`)
- **Frontend**: `websocket.js` — SockJS + STOMP with reconnect (5s delay), re-subscribe on reconnect, pending subscriptions queue for race conditions
- **Paths**: Frontend subscribes to `/user/queue/notifications` and `/topic/ticket/{id}`; backend pushes match these paths

## SLA Module (Iteration 2)

- **Tables**: `it_sla_config` (config by category+priority with response/resolution time thresholds), `it_sla_record` (per-ticket SLA tracking with deadlines, actual times, status)
- **Status flow**: NORMAL → WARNING (threshold %) → BREACHED (past deadline) → RESOLVED (on-time resolution)
- **Monitoring**: `SlaMonitorJob` (Quartz, cron `0 0/5 * * * ?`) scans active records, calculates WARNING by percentage elapsed of deadline, detects BREACHED when past deadline
- **Integration**: `createSlaRecord` on SUBMIT (queries config by categoryId+priority), `updateSlaResponseTime` on ASSIGN, `updateSlaResolutionTime` on RESOLVE
- **Config lookup**: `selectSlaConfigByCategoryAndPriority(categoryId, priority)` with unique constraint on categoryId+priority

## Key Architecture Patterns

### Controller Convention
All controllers extend `BaseController`. CRUD pattern:
- `GET /list` → `startPage()` + `service.selectXxxList()` → `getDataTable(list)`
- `GET /{id}` → `service.selectXxxById()` → `success(data)`
- `POST /` → `service.insertXxx()` → `toAjax(rows)`
- `PUT /` → `service.updateXxx()` → `toAjax(rows)`
- `DELETE /{ids}` → `service.deleteXxxByIds()` → `success()`
- `@PreAuthorize("@ss.hasPermi('itsm:xxx:yyy')")` on every method
- `@Log(title, businessType)` on mutation methods

### Service Layer
- Interface: `IItXxxService`, impl: `ItXxxServiceImpl` with `@Service`, `@Autowired` mapper injection
- Ticket number: `TK-yyyyMMdd-XXXX` via `RedisTemplate.opsForValue().increment()`
- Business logic (state validation, uniqueness) in service layer

### MyBatis Mapper Pattern
- Interface with `@Mapper`, XML in `resources/mapper/<module>/`
- `<resultMap>` + `<sql id="selectXxxVo">` + dynamic `<if>` conditions
- Use `<trim suffixOverrides=",">` for dynamic insert column/value lists (never leave trailing comma)
- Multi-parameter queries use `@Param` annotations on both interface and XML reference

### Database
- Table prefixes: `sys_` (system), `it_` (ITSM ticket), `wf_` (workflow)
- System/business tables use `del_flag = '0'` (active) / `'2'` (deleted). Workflow tables (`wf_*`) and SLA tables use physical delete (no del_flag column)
- JDBC URL must include `characterEncoding=UTF-8`; Druid uses `connectionInitSqls: SET NAMES utf8mb4`
- MySQL client must use `--default-character-set=utf8mb4` when importing SQL

### Security
- JWT stateless auth, token in `Authorization: Bearer <token>` header
- Permission format: `itsm:ticket:list`, `itsm:workflow:add`, `itsm:sla:query`, etc.
- `@Anonymous` on methods to bypass auth
- Notification operations must verify `receiverId == currentUserId` (enforced in service layer)
- `selectNotificationList` forces `receiver_id` filter (non-optional) to prevent data leak

### Domain Entities
- Those with DB audit fields extend `BaseEntity` (provides createBy/createTime/updateBy/updateTime/remark)
- Workflow entities (`WfInstance`, `WfTask`, `WfNode`, `WfTransition`) and SLA/notification entities do NOT extend BaseEntity (their tables lack audit columns)
- Use `@Excel` annotation for export fields with `readConverterExp` for value mapping

## Git Branch Strategy

```
main                              ← production
  └── develop                     ← integration
        ├── feature/itsm-iteration1  ← completed
        └── feature/itsm-iteration2  ← completed
```

**Commit format**: `<type>(scope): <subject>` — types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`

## Key Decisions

- Self-built state machine (not Flowable)
- Spring Event bus (not RabbitMQ) with `@TransactionalEventListener(AFTER_COMMIT)`
- SockJS + STOMP WebSocket (single-instance, not distributed)
- MySQL FULLTEXT (not Elasticsearch)
- SLA monitoring via Quartz scheduled job (not real-time timers)

## Common Pitfalls

- WfNode/WfTransition/WfInstance/WfTask tables have NO `del_flag`, `create_time`, `update_by`, `update_time`, `remark` columns — mappers must NOT reference these
- `selectInstanceByBusinessId` and `selectTasksByBusinessId` require both `businessId` AND `businessType` parameters
- `convertAndSendToUser` requires a registered Principal (set by `StompChannelInterceptor` on CONNECT)
- Notification `sendStatus` should be set to `"PENDING"` before push, then updated to `"SENT"` or `"FAILED"` after push attempt
- `v-hasPermi` directive with empty array may throw — always provide a default permission for fallback code paths
- `@EventListener` alone fires before transaction commit — use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` for DB-dependent listeners
