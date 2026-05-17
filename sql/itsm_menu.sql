-- IT运维工单管理系统 - 菜单与权限数据
-- 菜单级联关系：IT运维工单 > 子菜单

-- =====================================================================
-- 一级菜单：IT运维工单
-- =====================================================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('IT运维工单', 0, 10, 'itsm', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'guide', 'admin',
        NOW());

-- 获取父级菜单ID
SET @itsm_parent_id = LAST_INSERT_ID();

-- =====================================================================
-- 二级菜单
-- =====================================================================

-- 工单仪表盘
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('工单仪表盘', @itsm_parent_id, 1, 'dashboard', 'itsm/dashboard/index', NULL, 'ItsmDashboard', 1, 0, 'C', '0',
        '0', 'itsm:dashboard:view', 'dashboard', 'admin', NOW());

-- 工单管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('工单管理', @itsm_parent_id, 2, 'ticket', 'itsm/ticket/index', NULL, 'ItsmTicket', 1, 0, 'C', '0', '0',
        'itsm:ticket:list', 'component', 'admin', NOW());

SET @ticket_menu_id = LAST_INSERT_ID();

-- 工单管理 - 按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('工单查询', @ticket_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:query', '#',
        'admin', NOW()),
       ('工单新增', @ticket_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:add', '#',
        'admin', NOW()),
       ('工单修改', @ticket_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:edit', '#',
        'admin', NOW()),
       ('工单删除', @ticket_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:remove', '#',
        'admin', NOW()),
       ('工单导出', @ticket_menu_id, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:export', '#',
        'admin', NOW()),
       ('工单分配', @ticket_menu_id, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:assign', '#',
        'admin', NOW()),
       ('工单审批', @ticket_menu_id, 7, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:ticket:approve', '#',
        'admin', NOW());

-- 工单分类管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('工单分类', @itsm_parent_id, 3, 'category', 'itsm/category/index', NULL, 'ItsmCategory', 1, 0, 'C', '0', '0',
        'itsm:category:list', 'tree-table', 'admin', NOW());

SET @category_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
                        `create_time`)
VALUES ('分类查询', @category_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:category:query', '#',
        'admin', NOW()),
       ('分类新增', @category_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:category:add', '#',
        'admin', NOW()),
       ('分类修改', @category_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:category:edit', '#',
        'admin', NOW()),
       ('分类删除', @category_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'itsm:category:remove', '#',
        'admin', NOW());
