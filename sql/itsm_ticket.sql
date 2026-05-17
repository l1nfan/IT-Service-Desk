-- IT运维工单管理系统 - 工单模块初始化脚本（迭代1）
-- 表前缀 it_ = ITSM，遵循RuoYi现有 sys_ 前缀规范

-- =====================================================================
-- 1. 工单分类表
-- =====================================================================
DROP TABLE IF EXISTS `it_ticket_category`;
CREATE TABLE `it_ticket_category`
(
    `category_id`   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` varchar(100) NOT NULL COMMENT '分类名称',
    `parent_id`     bigint(20)   NOT NULL DEFAULT 0 COMMENT '父分类ID（0为顶级）',
    `ancestors`     varchar(500) NOT NULL DEFAULT '' COMMENT '祖级列表',
    `order_num`     int(4)       NOT NULL DEFAULT 0 COMMENT '排序号',
    `workflow_id`   bigint(20)            DEFAULT NULL COMMENT '关联默认流程ID',
    `sla_config_id` bigint(20)            DEFAULT NULL COMMENT '关联SLA配置ID',
    `icon`          varchar(100)          DEFAULT NULL COMMENT '分类图标',
    `status`        char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `del_flag`      char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    `create_by`     varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`   datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`        varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`category_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='工单分类表';

-- 预置分类数据
INSERT INTO `it_ticket_category` (`category_id`, `category_name`, `parent_id`, `ancestors`, `order_num`, `status`,
                                  `create_by`)
VALUES (1, '硬件故障', 0, '0', 1, '0', 'admin'),
       (2, '软件问题', 0, '0', 2, '0', 'admin'),
       (3, '网络问题', 0, '0', 3, '0', 'admin'),
       (4, '账号权限', 0, '0', 4, '0', 'admin'),
       (5, '服务请求', 0, '0', 5, '0', 'admin'),
       (10, '服务器故障', 1, '0,1', 1, '0', 'admin'),
       (11, '网络设备故障', 1, '0,1', 2, '0', 'admin'),
       (12, '终端设备故障', 1, '0,1', 3, '0', 'admin'),
       (20, '系统软件', 2, '0,2', 1, '0', 'admin'),
       (21, '应用软件', 2, '0,2', 2, '0', 'admin'),
       (22, '中间件', 2, '0,2', 3, '0', 'admin'),
       (30, '网络连接', 3, '0,3', 1, '0', 'admin'),
       (31, 'VPN', 3, '0,3', 2, '0', 'admin'),
       (32, 'DNS', 3, '0,3', 3, '0', 'admin'),
       (40, '账号申请', 4, '0,4', 1, '0', 'admin'),
       (41, '权限变更', 4, '0,4', 2, '0', 'admin'),
       (42, '密码重置', 4, '0,4', 3, '0', 'admin'),
       (50, '资源申请', 5, '0,5', 1, '0', 'admin'),
       (51, '配置变更', 5, '0,5', 2, '0', 'admin'),
       (52, '信息查询', 5, '0,5', 3, '0', 'admin');

-- =====================================================================
-- 2. 工单主表
-- =====================================================================
DROP TABLE IF EXISTS `it_ticket`;
CREATE TABLE `it_ticket`
(
    `ticket_id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `ticket_no`            varchar(20)  NOT NULL COMMENT '工单编号（TK-YYYYMMDD-XXXX）',
    `title`                varchar(200) NOT NULL COMMENT '工单标题',
    `description`          longtext              DEFAULT NULL COMMENT '工单描述（富文本）',
    `category_id`          bigint(20)   NOT NULL COMMENT '工单分类ID',
    `sub_category_id`      bigint(20)            DEFAULT NULL COMMENT '子分类ID',
    `priority`             tinyint(1)   NOT NULL DEFAULT 3 COMMENT '优先级（1紧急 2高 3中 4低）',
    `status`               varchar(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '工单状态（DRAFT/SUBMITTED/APPROVED/ASSIGNED/PROCESSING/RESOLVED/VERIFIED/CLOSED/REJECTED/CANCELLED）',
    `impact_scope`         varchar(500)          DEFAULT NULL COMMENT '影响范围',
    `source`               varchar(20)  NOT NULL DEFAULT 'WEB' COMMENT '来源（WEB/EMAIL/PHONE/API）',
    `workflow_id`          bigint(20)            DEFAULT NULL COMMENT '关联流程定义ID',
    `workflow_instance_id` bigint(20)            DEFAULT NULL COMMENT '关联流程实例ID',
    `current_node`         varchar(50)           DEFAULT NULL COMMENT '当前流程节点',
    `creator_id`           bigint(20)   NOT NULL COMMENT '创建人ID',
    `creator_dept_id`      bigint(20)            DEFAULT NULL COMMENT '创建人部门ID',
    `assignee_id`          bigint(20)            DEFAULT NULL COMMENT '当前处理人ID',
    `assignee_dept_id`     bigint(20)            DEFAULT NULL COMMENT '处理人部门ID',
    `approver_id`          bigint(20)            DEFAULT NULL COMMENT '审批人ID',
    `expected_resolve_time` datetime             DEFAULT NULL COMMENT '预期解决时间',
    `actual_resolve_time`  datetime              DEFAULT NULL COMMENT '实际解决时间',
    `resolved_by`          bigint(20)            DEFAULT NULL COMMENT '解决人ID',
    `closed_by`            bigint(20)            DEFAULT NULL COMMENT '关闭人ID',
    `close_time`           datetime              DEFAULT NULL COMMENT '关闭时间',
    `total_hours`          decimal(6, 2)         DEFAULT 0 COMMENT '总工时（小时）',
    `del_flag`             char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    `create_by`            varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`          datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`            varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`          datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`               varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`ticket_id`),
    UNIQUE INDEX `uk_ticket_no` (`ticket_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_assignee_id` (`assignee_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_expected_resolve` (`expected_resolve_time`),
    FULLTEXT INDEX `ft_ticket_search` (`title`, `description`) WITH PARSER ngram
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='工单主表';

-- =====================================================================
-- 3. 工单附件表
-- =====================================================================
DROP TABLE IF EXISTS `it_ticket_attachment`;
CREATE TABLE `it_ticket_attachment`
(
    `attachment_id` bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `ticket_id`     bigint(20)   NOT NULL COMMENT '工单ID',
    `file_name`     varchar(200) NOT NULL COMMENT '文件名',
    `file_path`     varchar(500) NOT NULL COMMENT '文件路径',
    `file_size`     bigint(20)   NOT NULL COMMENT '文件大小（字节）',
    `file_type`     varchar(50)  NOT NULL COMMENT '文件类型（扩展名）',
    `upload_by`     varchar(64)  NOT NULL COMMENT '上传人',
    `upload_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `del_flag`      char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    PRIMARY KEY (`attachment_id`),
    INDEX `idx_ticket_id` (`ticket_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='工单附件表';

-- =====================================================================
-- 4. 工单操作日志表
-- =====================================================================
DROP TABLE IF EXISTS `it_ticket_log`;
CREATE TABLE `it_ticket_log`
(
    `log_id`        bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `ticket_id`     bigint(20)   NOT NULL COMMENT '工单ID',
    `ticket_no`     varchar(20)           DEFAULT NULL COMMENT '工单编号',
    `action`        varchar(50)  NOT NULL COMMENT '操作类型（SUBMIT/APPROVE/REJECT/ASSIGN/START_PROCESS/RESOLVE/VERIFY/REOPEN/CLOSE/REDRAFT/CANCEL/CREATE/UPDATE/DELETE）',
    `action_name`   varchar(100) NOT NULL COMMENT '操作名称',
    `from_status`   varchar(20)           DEFAULT NULL COMMENT '原状态',
    `to_status`     varchar(20)           DEFAULT NULL COMMENT '目标状态',
    `operator_id`   bigint(20)   NOT NULL COMMENT '操作人ID',
    `operator_name` varchar(64)  NOT NULL COMMENT '操作人姓名',
    `comment`       varchar(1000)         DEFAULT NULL COMMENT '操作备注',
    `operate_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`log_id`),
    INDEX `idx_ticket_id` (`ticket_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_operate_time` (`operate_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='工单操作日志表';
