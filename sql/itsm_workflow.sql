-- IT运维工单管理系统 - 工作流模块初始化脚本（迭代2）
-- 表前缀 wf_ = 工作流，it_ = ITSM业务表
-- 执行顺序：先执行 itsm_ticket.sql，再执行本脚本

-- =====================================================================
-- 1. 流程定义表
-- =====================================================================
DROP TABLE IF EXISTS `wf_workflow`;
CREATE TABLE `wf_workflow`
(
    `workflow_id`   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '流程ID',
    `workflow_name` varchar(100) NOT NULL COMMENT '流程名称',
    `workflow_key`  varchar(50)  NOT NULL COMMENT '流程标识',
    `description`   varchar(500)          DEFAULT NULL COMMENT '描述',
    `version`       int(4)       NOT NULL DEFAULT 1 COMMENT '版本号',
    `status`        char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
    `is_default`    char(1)      NOT NULL DEFAULT '0' COMMENT '是否默认流程（0否 1是）',
    `del_flag`      char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    `create_by`     varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`   datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`        varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`workflow_id`),
    UNIQUE INDEX `uk_workflow_key` (`workflow_key`, `version`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='流程定义表';

-- =====================================================================
-- 2. 流程节点表
-- =====================================================================
DROP TABLE IF EXISTS `wf_node`;
CREATE TABLE `wf_node`
(
    `node_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    `workflow_id`   bigint(20)   NOT NULL COMMENT '流程ID',
    `node_key`      varchar(50)  NOT NULL COMMENT '节点标识',
    `node_name`     varchar(100) NOT NULL COMMENT '节点名称',
    `node_type`     varchar(20)  NOT NULL COMMENT '节点类型（START/END/APPROVAL/ASSIGN/PROCESS/REVIEW）',
    `assignee_type` varchar(20)           DEFAULT NULL COMMENT '分配类型（USER/ROLE/DEPT/INITIATOR）',
    `assignee_value` varchar(200)         DEFAULT NULL COMMENT '分配值',
    `position_x`    int                   DEFAULT NULL COMMENT '设计器X坐标',
    `position_y`    int                   DEFAULT NULL COMMENT '设计器Y坐标',
    `order_num`     int(4)       NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`node_id`),
    INDEX `idx_workflow_id` (`workflow_id`),
    UNIQUE INDEX `uk_workflow_node_key` (`workflow_id`, `node_key`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='流程节点表';

-- =====================================================================
-- 3. 流程流转规则表
-- =====================================================================
DROP TABLE IF EXISTS `wf_transition`;
CREATE TABLE `wf_transition`
(
    `transition_id`   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `workflow_id`     bigint(20)   NOT NULL COMMENT '流程ID',
    `from_node_key`   varchar(50)  NOT NULL COMMENT '源节点标识',
    `to_node_key`     varchar(50)  NOT NULL COMMENT '目标节点标识',
    `action`          varchar(50)  NOT NULL COMMENT '触发动作',
    `transition_name` varchar(100) NOT NULL COMMENT '流转名称',
    `condition_expr`  varchar(500)          DEFAULT NULL COMMENT '流转条件（SpEL表达式）',
    `permission`      varchar(100)          DEFAULT NULL COMMENT '所需权限标识',
    `order_num`       int(4)       NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`transition_id`),
    INDEX `idx_workflow_id` (`workflow_id`),
    INDEX `idx_from_node` (`workflow_id`, `from_node_key`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='流程流转规则表';

-- =====================================================================
-- 4. 流程实例表
-- =====================================================================
DROP TABLE IF EXISTS `wf_instance`;
CREATE TABLE `wf_instance`
(
    `instance_id`      bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '实例ID',
    `workflow_id`      bigint(20)  NOT NULL COMMENT '流程ID',
    `business_id`      bigint(20)  NOT NULL COMMENT '业务ID（工单ID）',
    `business_type`    varchar(20) NOT NULL DEFAULT 'TICKET' COMMENT '业务类型',
    `current_node_key` varchar(50) NOT NULL COMMENT '当前节点标识',
    `status`           varchar(20) NOT NULL DEFAULT 'RUNNING' COMMENT '实例状态（RUNNING/COMPLETED/TERMINATED）',
    `start_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time`         datetime             DEFAULT NULL COMMENT '结束时间',
    `create_by`        varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`      datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`instance_id`),
    INDEX `idx_workflow_id` (`workflow_id`),
    INDEX `idx_business` (`business_id`, `business_type`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='流程实例表';

-- =====================================================================
-- 5. 流程任务表
-- =====================================================================
DROP TABLE IF EXISTS `wf_task`;
CREATE TABLE `wf_task`
(
    `task_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `instance_id`   bigint(20)   NOT NULL COMMENT '实例ID',
    `workflow_id`   bigint(20)   NOT NULL COMMENT '流程ID',
    `business_id`   bigint(20)   NOT NULL COMMENT '业务ID（工单ID）',
    `node_key`      varchar(50)  NOT NULL COMMENT '节点标识',
    `node_name`     varchar(100) NOT NULL COMMENT '节点名称',
    `assignee_id`   bigint(20)   NOT NULL COMMENT '处理人ID',
    `assignee_name` varchar(64)  NOT NULL COMMENT '处理人姓名',
    `action`        varchar(50)           DEFAULT NULL COMMENT '执行动作',
    `comment`       varchar(500)          DEFAULT NULL COMMENT '处理意见',
    `status`        varchar(20)  NOT NULL DEFAULT 'PENDING' COMMENT '任务状态（PENDING/COMPLETED/CANCELLED）',
    `claim_time`    datetime              DEFAULT NULL COMMENT '认领时间',
    `complete_time` datetime              DEFAULT NULL COMMENT '完成时间',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`task_id`),
    INDEX `idx_instance_id` (`instance_id`),
    INDEX `idx_assignee` (`assignee_id`, `status`),
    INDEX `idx_business` (`business_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='流程任务表';

-- =====================================================================
-- 6. 工单处理记录表
-- =====================================================================
DROP TABLE IF EXISTS `it_ticket_process_log`;
CREATE TABLE `it_ticket_process_log`
(
    `log_id`        bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `ticket_id`     bigint(20)    NOT NULL COMMENT '工单ID',
    `action`        varchar(50)   NOT NULL COMMENT '操作类型（ASSIGN/PROCESS/TRANSFER/RESOLVE/VERIFY等）',
    `action_name`   varchar(100)  NOT NULL COMMENT '操作名称',
    `content`       longtext               DEFAULT NULL COMMENT '处理内容（富文本）',
    `from_status`   varchar(20)            DEFAULT NULL COMMENT '原状态',
    `to_status`     varchar(20)            DEFAULT NULL COMMENT '新状态',
    `from_assignee` bigint(20)             DEFAULT NULL COMMENT '原处理人ID',
    `to_assignee`   bigint(20)             DEFAULT NULL COMMENT '新处理人ID',
    `work_hours`    decimal(4, 2)          DEFAULT NULL COMMENT '工时（小时）',
    `operator_id`   bigint(20)    NOT NULL COMMENT '操作人ID',
    `operator_name` varchar(64)   NOT NULL COMMENT '操作人姓名',
    `operate_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`log_id`),
    INDEX `idx_ticket_id` (`ticket_id`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_operate_time` (`operate_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='工单处理记录表';

-- =====================================================================
-- 7. 通知记录表
-- =====================================================================
DROP TABLE IF EXISTS `it_notification`;
CREATE TABLE `it_notification`
(
    `notification_id`   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `title`             varchar(200) NOT NULL COMMENT '通知标题',
    `content`           text                  DEFAULT NULL COMMENT '通知内容',
    `notification_type` varchar(20)  NOT NULL DEFAULT 'SYSTEM' COMMENT '类型（SYSTEM/EMAIL/SMS）',
    `business_id`       bigint(20)            DEFAULT NULL COMMENT '关联业务ID',
    `business_type`     varchar(20)           DEFAULT NULL COMMENT '业务类型（TICKET/SLA等）',
    `sender_id`         bigint(20)   NOT NULL DEFAULT 0 COMMENT '发送人ID（0=系统）',
    `receiver_id`       bigint(20)   NOT NULL COMMENT '接收人ID',
    `is_read`           char(1)      NOT NULL DEFAULT '0' COMMENT '是否已读（0未读 1已读）',
    `read_time`         datetime              DEFAULT NULL COMMENT '已读时间',
    `send_status`       varchar(20)  NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（PENDING/SENT/FAILED）',
    `send_time`         datetime              DEFAULT NULL COMMENT '发送时间',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`notification_id`),
    INDEX `idx_receiver_id` (`receiver_id`, `is_read`),
    INDEX `idx_business` (`business_id`, `business_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='通知记录表';

-- =====================================================================
-- 默认流程初始化数据
-- =====================================================================

-- 默认IT工单流程
INSERT INTO `wf_workflow` (`workflow_id`, `workflow_name`, `workflow_key`, `description`, `version`, `status`,
                           `is_default`, `create_by`)
VALUES (1, 'IT工单标准流程', 'it_ticket_standard', 'IT运维工单标准处理流程：提交→审批→分配→处理→验证→关闭', 1, '0', '1',
        'admin');

-- 流程节点
INSERT INTO `wf_node` (`node_id`, `workflow_id`, `node_key`, `node_name`, `node_type`, `assignee_type`,
                       `assignee_value`, `position_x`, `position_y`, `order_num`)
VALUES (1, 1, 'DRAFT', '草稿', 'START', 'INITIATOR', NULL, 50, 200, 1),
       (2, 1, 'SUBMITTED', '已提交', 'APPROVAL', 'ROLE', 'admin', 200, 200, 2),
       (3, 1, 'APPROVED', '已审批', 'ASSIGN', 'ROLE', 'admin', 350, 200, 3),
       (4, 1, 'ASSIGNED', '已分配', 'ASSIGN', 'USER', NULL, 500, 200, 4),
       (5, 1, 'PROCESSING', '处理中', 'PROCESS', 'USER', NULL, 650, 200, 5),
       (6, 1, 'RESOLVED', '已解决', 'REVIEW', 'INITIATOR', NULL, 800, 200, 6),
       (7, 1, 'VERIFIED', '已验证', 'END', 'INITIATOR', NULL, 950, 200, 7),
       (8, 1, 'CLOSED', '已关闭', 'END', NULL, NULL, 1100, 200, 8),
       (9, 1, 'REJECTED', '已驳回', 'END', 'INITIATOR', NULL, 200, 350, 9),
       (10, 1, 'CANCELLED', '已取消', 'END', NULL, NULL, 500, 350, 10);

-- 流程流转规则（与TicketTransition枚举对应）
INSERT INTO `wf_transition` (`workflow_id`, `from_node_key`, `to_node_key`, `action`, `transition_name`,
                             `permission`, `order_num`)
VALUES (1, 'DRAFT', 'SUBMITTED', 'SUBMIT', '提交工单', 'itsm:ticket:edit', 1),
       (1, 'SUBMITTED', 'APPROVED', 'APPROVE', '审批通过', 'itsm:ticket:approve', 2),
       (1, 'SUBMITTED', 'REJECTED', 'REJECT', '审批驳回', 'itsm:ticket:approve', 3),
       (1, 'REJECTED', 'DRAFT', 'REDRAFT', '重新编辑', 'itsm:ticket:edit', 4),
       (1, 'APPROVED', 'ASSIGNED', 'ASSIGN', '分配工单', 'itsm:ticket:assign', 5),
       (1, 'ASSIGNED', 'PROCESSING', 'START_PROCESS', '开始处理', 'itsm:ticket:edit', 6),
       (1, 'PROCESSING', 'RESOLVED', 'RESOLVE', '解决工单', 'itsm:ticket:edit', 7),
       (1, 'RESOLVED', 'VERIFIED', 'VERIFY', '验证通过', 'itsm:ticket:approve', 8),
       (1, 'RESOLVED', 'PROCESSING', 'REOPEN', '重新处理', 'itsm:ticket:approve', 9),
       (1, 'VERIFIED', 'CLOSED', 'CLOSE', '关闭工单', 'itsm:ticket:approve', 10),
       (1, 'DRAFT', 'CANCELLED', 'CANCEL', '取消工单', 'itsm:ticket:edit', 11),
       (1, 'SUBMITTED', 'CANCELLED', 'CANCEL', '取消工单', 'itsm:ticket:edit', 12),
       (1, 'APPROVED', 'CANCELLED', 'CANCEL', '取消工单', 'itsm:ticket:assign', 13),
       (1, 'ASSIGNED', 'CANCELLED', 'CANCEL', '取消工单', 'itsm:ticket:assign', 14),
       (1, 'PROCESSING', 'CANCELLED', 'CANCEL', '取消工单', 'itsm:ticket:edit', 15);

INSERT INTO `wf_transition` (`transition_id`, `workflow_id`, `from_node_key`, `to_node_key`, `action`, `transition_name`,
                             `permission`, `condition_expr`, `order_num`)
VALUES (16, 1, 'ASSIGNED', 'ASSIGNED', 'REASSIGN', '转派', 'itsm:ticket:assign', 0, 16),
       (17, 1, 'PROCESSING', 'ASSIGNED', 'REASSIGN', '转派', 'itsm:ticket:assign', 0, 17);

-- 更新分类表关联默认流程
UPDATE `it_ticket_category` SET `workflow_id` = 1 WHERE `workflow_id` IS NULL;

-- =====================================================================
-- 8. SLA配置表
-- =====================================================================
DROP TABLE IF EXISTS `it_sla_config`;
CREATE TABLE `it_sla_config`
(
    `sla_id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT 'SLA ID',
    `sla_name`          varchar(100) NOT NULL COMMENT 'SLA名称',
    `category_id`       bigint(20)            DEFAULT NULL COMMENT '分类ID',
    `priority`          varchar(20)           DEFAULT NULL COMMENT '优先级（LOW/MEDIUM/HIGH/URGENT）',
    `response_time`     int(11)               DEFAULT NULL COMMENT '响应时限（分钟）',
    `resolution_time`   int(11)               DEFAULT NULL COMMENT '解决时限（分钟）',
    `warning_threshold` int(3)       NOT NULL DEFAULT 80 COMMENT '预警阈值（百分比）',
    `status`            char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
    `del_flag`          char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    `create_by`         varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`       datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`       datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`            varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`sla_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='SLA配置表';

-- =====================================================================
-- 9. SLA记录表
-- =====================================================================
DROP TABLE IF EXISTS `it_sla_record`;
CREATE TABLE `it_sla_record`
(
    `record_id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `ticket_id`           bigint(20)   NOT NULL COMMENT '工单ID',
    `sla_config_id`       bigint(20)   NOT NULL COMMENT 'SLA配置ID',
    `sla_start_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'SLA开始时间',
    `response_deadline`   datetime              DEFAULT NULL COMMENT '响应截止时间',
    `resolution_deadline` datetime              DEFAULT NULL COMMENT '解决截止时间',
    `response_actual`     datetime              DEFAULT NULL COMMENT '实际响应时间',
    `resolution_actual`   datetime              DEFAULT NULL COMMENT '实际解决时间',
    `status`              varchar(20)  NOT NULL DEFAULT 'NORMAL' COMMENT 'SLA状态（NORMAL/WARNING/BREACHED）',
    `warning_count`       int(2)       NOT NULL DEFAULT 0 COMMENT '预警次数',
    `warning_time`        datetime              DEFAULT NULL COMMENT '最近预警时间',
    `breached_time`       datetime              DEFAULT NULL COMMENT '超时时间',
    PRIMARY KEY (`record_id`),
    INDEX `idx_ticket_id` (`ticket_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='SLA记录表';
