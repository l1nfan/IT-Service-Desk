当前项目处于迭代2收尾阶段，所有P0/P1缺陷已修复，17项验收标准全部通过，准备进入迭代2集成验收。

当前进度：
迭代0：100%。模块、依赖、数据库脚本、前后端结构都已成型。
迭代1：100%。工单 CRUD、分类、附件、状态机流转、日志、前端列表/详情都已实现。
迭代2：100%。四轮P0/P1缺陷修复完成，核心业务功能已可用，SLA状态展示已补全，Quartz cron已配置。

总体项目：约 82%。

已验证
后端工单核心服务已覆盖创建、编辑限制、删除限制、审批、分配、转派、通用状态流转、可用流转查询和日志记录：ItTicketServiceImpl.java
控制器已提供工单 CRUD、审批、分配、转派、流转、流转查询、日志、流程日志、SLA查询接口：ItTicketController.java (15 APIs)
状态机已有 10 个状态、17 个转换（含2个REASSIGN），并通过 transitionMap 校验：TicketTransition.java、TicketStateMachine.java
前端详情页已接入审批、分配、转派、可用流转、附件、日志、SLA信息：detail.vue
前端列表页已展示SLA状态列：index.vue

迭代2 完成项
工作流引擎：wf_workflow/wf_node/wf_transition/wf_instance/wf_task 表 + 领域 + Mapper + 服务 + WorkflowEngineImpl + WfWorkflowController + WfTaskController + 前端工作流管理页面
流程日志：it_ticket_process_log 表 + 领域 + Mapper + 服务，已集成到 ItTicketServiceImpl
通知中心：it_notification 表 + 领域 + Mapper + 服务 + ItNotificationController + 前端通知页面
WebSocket：WebSocketConfig + WebSocketAuthInterceptor (JWT) + StompChannelInterceptor (token过期检测) + WebSocketPushService + 前端 websocket.js
事件总线：TicketEvent + TicketEventListener (@Async @TransactionalEventListener(AFTER_COMMIT))，通知和 WebSocket 推送已集成
SLA：it_sla_config/it_sla_record 表 + 领域 + Mapper + 服务 + ItSlaConfigController + SlaMonitorJob (Quartz, cron 0 0/5 * * * ?) + 前端 SLA 配置页面
SLA 事件集成：工单提交→创建 SLA 记录，分配→更新响应时间，解决→更新解决时间（含RESOLVED状态）
SLA 前端展示：工单列表页 SLA 状态列（el-tag 正常/预警/超时/已解决），工单详情页 SLA 信息区域
SlaStatus 枚举：NORMAL/WARNING/BREACHED/RESOLVED 四状态枚举类已创建
转派功能：REASSIGN_ASSIGNED + REASSIGN_PROCESSING 两个转换，前端转派按钮和对话框

迭代2 四轮修复汇总（2026-05-17/18）
第一轮（XML/SQL修复）：
  C-01~C-06：字段映射和参数匹配；ItNotificationMapper列修正；ItTicketProcessLogMapper列修正
第二轮（P0阻塞缺陷）：
  NEW-04/NEW-05：insert动态SQL尾部逗号（trim suffixOverrides）
  C-07：createPendingTask从节点配置获取处理人（INITIATOR/USER类型解析）
  NEW-01：REJECTED节点创建待办任务给发起人
  H-01：通知receiverId改为approverId/operatorId
  H-09：SlaMonitorJob通知receiver从ticket.assignee_id获取
  H-07/NEW-20：SLA状态添加RESOLVED，解决时正确记录
  H-04：selectSlaConfigByCategoryId注入priority条件
  NEW-10/NEW-11：detail.vue审批/分配按钮去重+permission字段
  H-17：通知操作校验receiverId
  NEW-16：selectNotificationList强制receiver_id过滤
第三轮（FIX+完善）：
  FIX-01：USER类型assigneeName改为"用户(ID)"格式
  FIX-02：WebSocket推送SUBMIT/APPROVE目标改为n.getReceiverId()
  FIX-03：sendNotificationToAssigners语义说明（approver→分配）
  FIX-04：ItSlaRecord @Excel注解添加RESOLVED=已解决
  FIX-05：detail.vue降级路径添加默认permission
  NEW-03：startProcess创建初始待办任务
  M-14：SlaMonitorJob添加responseDeadline WARNING预警
第四轮（验收修复，2026-05-18）：
  添加REASSIGN_ASSIGNED/REASSIGN_PROCESSING转换枚举
  v-hasPermi空数组处理：hasPermi.js添加value.length > 0检查
  通知receiverId三级回退：approverId→WfTask assignee→admin用户
  通知sendStatus跟踪：PENDING→SENT/FAILED三态
  WebSocket token过期检测：StompChannelInterceptor SUBSCRIBE/SEND时校验token
  创建SlaStatus枚举类（NORMAL/WARNING/BREACHED/RESOLVED）
  SLA状态展示：列表页SLA列 + 详情页SLA信息区域
  SLA Quartz cron配置：sys_job插入slaMonitorJob.checkSlaStatus() (0 0/5 * * * ?)

迭代2 待完善项（P2，不阻塞迭代3）
前端工作流设计器（可视化节点编辑）可作为迭代4的一部分
Domain实体审计字段（H-18~H-20）待后续统一修复DDL+Domain
it_notification逻辑删除（H-23）待后续补del_flag
WfWorkflowServiceImpl物理删除混用（M-08/M-09）
SLA配置页categoryId使用el-input-number而非el-tree-select（M-26/NEW-08/NEW-09）
其他P2轻微缺陷约20项

测试结果：mvn test 全部通过（103个：74 ticket + 29 admin）；npm run build:prod 通过。
缺陷统计：原始78+新发现20+修复引入5=103个；已修复57个(完全)+10个(部分)；未修复约20个(P2)+9不适用。

下一步：执行迭代2集成验收（完整生命周期：创建→提交→审批→分配→处理→解决→验证→关闭），通过后启动迭代3。
