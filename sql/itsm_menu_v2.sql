-- IT运维工单管理系统 - 迭代2菜单与权限数据
-- 在迭代1菜单基础上新增：流程管理、SLA管理、通知中心

-- 获取IT运维工单一级菜单ID
SET @itsm_parent_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = 'IT运维工单' AND `parent_id` = 0 LIMIT 1);

-- =====================================================================
-- 流程管理
-- =====================================================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('流程管理', @itsm_parent_id, 4, 'workflow', 'itsm/workflow/index', NULL, 'ItsmWorkflow', 1, 0, 'C', '0', '0',
        'itsm:workflow:list', 'cascader', 'admin', NOW());

SET @workflow_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('流程查询', @workflow_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:workflow:query', '#',
        'admin', NOW()),
       ('流程新增', @workflow_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:workflow:add', '#',
        'admin', NOW()),
       ('流程修改', @workflow_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:workflow:edit', '#',
        'admin', NOW()),
       ('流程删除', @workflow_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:workflow:remove', '#',
        'admin', NOW());

-- =====================================================================
-- SLA管理
-- =====================================================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('SLA管理', @itsm_parent_id, 5, 'sla', 'itsm/sla/config', NULL, 'ItsmSla', 1, 0, 'C', '0', '0',
        'itsm:sla:list', 'time-range', 'admin', NOW());

SET @sla_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('SLA查询', @sla_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:sla:query', '#', 'admin',
        NOW()),
       ('SLA新增', @sla_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:sla:add', '#', 'admin',
        NOW()),
       ('SLA修改', @sla_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:sla:edit', '#', 'admin',
        NOW()),
       ('SLA删除', @sla_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:sla:remove', '#', 'admin',
        NOW());

-- =====================================================================
-- 通知中心
-- =====================================================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('通知中心', @itsm_parent_id, 6, 'notification', 'itsm/notification/index', NULL, 'ItsmNotification', 1, 0, 'C',
        '0', '0', 'itsm:notification:list', 'message', 'admin', NOW());

SET @notification_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('通知查询', @notification_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:notification:query',
        '#', 'admin', NOW()),
       ('通知删除', @notification_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:notification:remove',
        '#', 'admin', NOW());

-- =====================================================================
-- 迭代2新增字典数据
-- =====================================================================

-- 节点类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('流程节点类型', 'itsm_workflow_node_type', '0', 'admin', NOW());

SET @node_type_id = LAST_INSERT_ID();

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, '开始节点', 'START', 'itsm_workflow_node_type', '0', 'admin', NOW()),
       (2, '结束节点', 'END', 'itsm_workflow_node_type', '0', 'admin', NOW()),
       (3, '审批节点', 'APPROVAL', 'itsm_workflow_node_type', '0', 'admin', NOW()),
       (4, '分配节点', 'ASSIGN', 'itsm_workflow_node_type', '0', 'admin', NOW()),
       (5, '处理节点', 'PROCESS', 'itsm_workflow_node_type', '0', 'admin', NOW()),
       (6, '审核节点', 'REVIEW', 'itsm_workflow_node_type', '0', 'admin', NOW());

-- 流程实例状态
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('流程实例状态', 'itsm_workflow_instance_status', '0', 'admin', NOW());

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, '运行中', 'RUNNING', 'itsm_workflow_instance_status', '0', 'admin', NOW()),
       (2, '已完成', 'COMPLETED', 'itsm_workflow_instance_status', '0', 'admin', NOW()),
       (3, '已终止', 'TERMINATED', 'itsm_workflow_instance_status', '0', 'admin', NOW());

-- 任务状态
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('流程任务状态', 'itsm_workflow_task_status', '0', 'admin', NOW());

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, '待处理', 'PENDING', 'itsm_workflow_task_status', '0', 'admin', NOW()),
       (2, '已完成', 'COMPLETED', 'itsm_workflow_task_status', '0', 'admin', NOW()),
       (3, '已取消', 'CANCELLED', 'itsm_workflow_task_status', '0', 'admin', NOW());

-- 通知类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('通知类型', 'itsm_notification_type', '0', 'admin', NOW());

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, '系统通知', 'SYSTEM', 'itsm_notification_type', '0', 'admin', NOW()),
       (2, '邮件通知', 'EMAIL', 'itsm_notification_type', '0', 'admin', NOW()),
       (3, '短信通知', 'SMS', 'itsm_notification_type', '0', 'admin', NOW());

-- SLA状态
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('SLA状态', 'itsm_sla_status', '0', 'admin', NOW());

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, '正常', 'NORMAL', 'itsm_sla_status', '0', 'admin', NOW()),
       (2, '预警', 'WARNING', 'itsm_sla_status', '0', 'admin', NOW()),
       (3, '超时', 'BREACHED', 'itsm_sla_status', '0', 'admin', NOW());

-- =====================================================================
-- SLA监控定时任务
-- =====================================================================
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`,
                       `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`,
                       `update_by`, `update_time`, `remark`)
VALUES (4, 'SLA监控任务', 'DEFAULT', 'slaMonitorJob.checkSlaStatus()', '0 0/5 * * * ?',
        '3', '1', '0', 'admin', NOW(), '', NULL, '每5分钟扫描SLA记录，检测预警和超时');
