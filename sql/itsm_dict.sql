-- IT运维工单管理系统 - 字典数据初始化
-- 字典类型使用 itsm_ 前缀，与现有 sys_ 体系共存

-- =====================================================================
-- 字典类型
-- =====================================================================
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES ('工单优先级', 'itsm_ticket_priority', '0', 'admin', NOW()),
       ('工单状态', 'itsm_ticket_status', '0', 'admin', NOW()),
       ('工单来源', 'itsm_ticket_source', '0', 'admin', NOW());

-- =====================================================================
-- 字典数据
-- =====================================================================

-- 工单优先级
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`,
                             `is_default`, `status`, `create_by`, `create_time`)
VALUES (1, '紧急', '1', 'itsm_ticket_priority', 'danger', 'danger', 'N', '0', 'admin', NOW()),
       (2, '高', '2', 'itsm_ticket_priority', 'warning', 'warning', 'N', '0', 'admin', NOW()),
       (3, '中', '3', 'itsm_ticket_priority', 'primary', 'primary', 'Y', '0', 'admin', NOW()),
       (4, '低', '4', 'itsm_ticket_priority', 'info', 'info', 'N', '0', 'admin', NOW());

-- 工单状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`,
                             `is_default`, `status`, `create_by`, `create_time`)
VALUES (1, '草稿', 'DRAFT', 'itsm_ticket_status', 'info', 'info', 'N', '0', 'admin', NOW()),
       (2, '已提交', 'SUBMITTED', 'itsm_ticket_status', 'primary', 'primary', 'N', '0', 'admin', NOW()),
       (3, '已审批', 'APPROVED', 'itsm_ticket_status', 'success', 'success', 'N', '0', 'admin', NOW()),
       (4, '已分配', 'ASSIGNED', 'itsm_ticket_status', 'primary', 'primary', 'N', '0', 'admin', NOW()),
       (5, '处理中', 'PROCESSING', 'itsm_ticket_status', 'warning', 'warning', 'N', '0', 'admin', NOW()),
       (6, '已解决', 'RESOLVED', 'itsm_ticket_status', 'success', 'success', 'N', '0', 'admin', NOW()),
       (7, '已验证', 'VERIFIED', 'itsm_ticket_status', 'success', 'success', 'N', '0', 'admin', NOW()),
       (8, '已关闭', 'CLOSED', 'itsm_ticket_status', 'info', 'info', 'N', '0', 'admin', NOW()),
       (9, '已驳回', 'REJECTED', 'itsm_ticket_status', 'danger', 'danger', 'N', '0', 'admin', NOW()),
       (10, '已取消', 'CANCELLED', 'itsm_ticket_status', 'info', 'info', 'N', '0', 'admin', NOW());

-- 工单来源
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`,
                             `is_default`, `status`, `create_by`, `create_time`)
VALUES (1, 'Web端', 'WEB', 'itsm_ticket_source', 'primary', 'primary', 'Y', '0', 'admin', NOW()),
       (2, '邮件', 'EMAIL', 'itsm_ticket_source', 'info', 'info', 'N', '0', 'admin', NOW()),
       (3, '电话', 'PHONE', 'itsm_ticket_source', 'warning', 'warning', 'N', '0', 'admin', NOW()),
       (4, 'API', 'API', 'itsm_ticket_source', 'info', 'info', 'N', '0', 'admin', NOW());
