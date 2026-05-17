# ITSM 工单管理系统 —— 系统性学习文档

> 本文档面向具有 JavaSE 基础但 SpringBoot 零基础的开发者，围绕工单模块对项目已完成功能进行系统性分析。

---

## 目录

- [一、项目整体架构与核心组件的工作原理](#一项目整体架构与核心组件的工作原理)
  - [1.1 什么是 Spring Boot？—— 从 JavaSE 视角理解](#11-什么是-spring-boot--从-javase-视角理解)
  - [1.2 模块划分 —— 像搭积木一样组织代码](#12-模块划分----像搭积木一样组织代码)
  - [1.3 核心组件工作原理](#13-核心组件工作原理)
- [二、工单模块的业务流程与数据流转路径](#二工单模块的业务流程与数据流转路径)
  - [2.1 工单状态机 —— 10 个状态、15 条转换](#21-工单状态机----10-个状态15-条转换)
  - [2.2 数据的完整生命周期：从产生到存储](#22-数据的完整生命周期从产生到存储)
  - [2.3 状态转换的数据流转（以"审批工单"为例）](#23-状态转换的数据流转以审批工单为例)
- [三、关键技术点在工单模块中的具体应用](#三关键技术点在工单模块中的具体应用)
  - [3.1 注解（Annotation）—— JavaSE 知识的延伸](#31-注解annotation----javase-知识的延伸)
  - [3.2 状态机模式 —— 用枚举实现有限状态自动机](#32-状态机模式----用枚举实现有限状态自动机)
  - [3.3 Redis 生成工单编号 —— 分布式计数器](#33-redis-生成工单编号----分布式计数器)
  - [3.4 Spring Event 事件机制 —— 观察者模式](#34-spring-event-事件机制----观察者模式)
  - [3.5 AOP 切面编程 —— 在不修改代码的情况下增加功能](#35-aop-切面编程----在不修改代码的情况下增加功能)
- [四、各功能模块间的交互方式及接口设计](#四各功能模块间的交互方式及接口设计)
  - [4.1 前后端交互 —— RESTful API](#41-前后端交互----restful-api)
  - [4.2 模块间交互图](#42-模块间交互图)
  - [4.3 认证鉴权交互流程](#43-认证鉴权交互流程)
- [五、SpringBoot 实现工单功能的核心代码逻辑与执行流程](#五springboot-实现工单功能的核心代码逻辑与执行流程)
  - [5.1 完整的"创建工单"执行流程](#51-完整的创建工单执行流程)
  - [5.2 通用状态转换 —— transitTicket 的精巧设计](#52-通用状态转换----transitticket-的精巧设计)
  - [5.3 特殊状态字段处理](#53-特殊状态字段处理)
  - [5.4 数据权限过滤 —— 看不见的 SQL 拼接](#54-数据权限过滤----看不见的-sql-拼接)
  - [5.5 逻辑删除 —— 数据不会真正消失](#55-逻辑删除----数据不会真正消失)
- [六、从 JavaSE 到 Spring Boot 的思维转换](#六从-javase-到-spring-boot-的思维转换)

---

## 一、项目整体架构与核心组件的工作原理

### 1.1 什么是 Spring Boot？—— 从 JavaSE 视角理解

你在 JavaSE 中写过程序，入口是 `public static void main(String[] args)`。Spring Boot 也是这样启动的，但它做了两件 JavaSE 做不到的事：

- **自动装配**：你不需要手动 `new` 对象，Spring 会自动帮你创建和管理所有对象（叫"Bean"）
- **内嵌服务器**：不需要部署到 Tomcat，程序自己就是一个 Web 服务器

看入口文件 `itsm-admin/src/main/java/com/itsm/ItsmApplication.java`：

```java
@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ItsmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItsmApplication.class, args);
    }
}
```

**JavaSE 类比**：就像你写了一个 `main` 方法，但这个方法不只是启动一个程序，而是启动了一整个"工厂"。`@SpringBootApplication` 是一个组合注解，相当于告诉工厂："扫描我所在的包及子包，把所有标了 `@Component`、`@Service`、`@Controller` 等注解的类都实例化并管理起来"。

### 1.2 模块划分 —— 像搭积木一样组织代码

项目有 7 个 Maven 模块，它们的依赖关系如下：

```
itsm-admin（入口，组装所有模块）
  ├── itsm-framework（框架层：安全、Redis、MyBatis配置）
  │     └── itsm-system（系统层：用户、角色、菜单、部门）
  │           └── itsm-common（公共层：工具类、基础实体、异常）
  ├── itsm-ticket（工单模块：核心业务）
  │     └── itsm-common
  ├── itsm-quartz（定时任务）
  │     └── itsm-common
  └── itsm-generator（代码生成器）
        └── itsm-common
```

**JavaSE 类比**：就像你写一个大项目时，把工具类放在 `utils` 包，把数据模型放在 `model` 包，把业务逻辑放在 `service` 包。Maven 模块就是更大规模的"包"，每个模块有自己的 `pom.xml`（相当于包的说明书，声明它依赖哪些其他模块和第三方库）。

各模块职责一览：

| 模块 | 职责 | 类比 |
|------|------|------|
| `itsm-admin` | 应用入口，包含所有 Controller | 程序的"前台"，接收用户请求 |
| `itsm-framework` | 安全认证、Redis配置、MyBatis配置、AOP切面 | 程序的"基础设施"，水电煤 |
| `itsm-system` | 用户、角色、菜单、部门、字典管理 | 程序的"人事部"，管人管权限 |
| `itsm-common` | 工具类、基础实体、异常定义、通用注解 | 程序的"工具箱"，谁都能用 |
| `itsm-ticket` | 工单、分类、附件、状态机、通知 | 程序的"核心业务"，工单管理 |
| `itsm-quartz` | 定时任务管理 | 程序的"闹钟"，定时执行任务 |
| `itsm-generator` | 代码生成器 | 程序的"模具厂"，快速生成代码 |

### 1.3 核心组件工作原理

| 组件 | 作用 | JavaSE 类比 |
|------|------|-------------|
| **Spring IoC 容器** | 管理所有对象的创建和依赖注入 | 一个全局的 `Map<String, Object>`，key 是类名，value 是实例。`@Autowired` 相当于从 Map 中取出需要的对象 |
| **Spring MVC** | 处理 HTTP 请求，路由到对应方法 | 类似一个大的 `if-else`：如果 URL 是 `/itsm/ticket/list`，就调用 `list()` 方法 |
| **Spring Security** | 认证和授权 | 类似一个"门卫"，每个请求进来先检查你有没有带"通行证"（JWT Token），再检查你有没有权限 |
| **MyBatis Plus** | 数据库操作 | 类似你用 JDBC 写 `PreparedStatement`，但只需要写接口方法，SQL 在 XML 中定义，框架自动帮你执行 |
| **Redis** | 缓存和计数器 | 类似一个全局的 `ConcurrentHashMap`，但它是独立进程，重启不丢失（如果做了持久化） |
| **JWT** | 无状态身份令牌 | 类似一张"加密的身份证"，里面存了你的 UUID，服务器不需要记住你，只要验证这张身份证是真的就行 |

---

## 二、工单模块的业务流程与数据流转路径

### 2.1 工单状态机 —— 10 个状态、15 条转换

这是整个工单模块的核心逻辑。用流程图表示：

```
                    ┌──────────┐
                    │  DRAFT   │ (草稿)
                    └────┬─────┘
                         │ SUBMIT (提交)
                         ▼
                    ┌──────────┐
              ┌─────│SUBMITTED │ (已提交)
              │     └──┬───┬───┘
              │        │   │
     CANCEL   │ APPROVE│   │REJECT
              │        ▼   ▼
              │  ┌─────────┐  ┌──────────┐
              │  │ APPROVED│  │ REJECTED │
              │  └────┬────┘  └────┬─────┘
              │       │ASSIGN      │REDRAFT
              │       ▼            ▼
              │  ┌──────────┐   回到 DRAFT
              │  │ ASSIGNED │ (已分配)
              │  └────┬─────┘
              │       │START_PROCESS
              │       ▼
              │  ┌───────────┐
              │  │PROCESSING │ (处理中)
              │  └──┬────┬───┘
              │     │    │REOPEN
              │     │    └──────► 回到 PROCESSING
              │     │RESOLVE
              │     ▼
              │  ┌──────────┐
              │  │ RESOLVED │ (已解决)
              │  └──┬───┬───┘
              │     │   │
              │     │   └── REOPEN ──► 回到 PROCESSING
              │     │VERIFY
              │     ▼
              │  ┌──────────┐
              │  │ VERIFIED │ (已验证)
              │  └────┬─────┘
              │       │CLOSE
              │       ▼
              │  ┌──────────┐
              └─►│ CLOSED   │ (已关闭)
                 └──────────┘
                    ▲
                    │ (从多个状态可取消)
              DRAFT/SUBMITTED/APPROVED/ASSIGNED/PROCESSING ──CANCEL──► CANCELLED
```

10 个状态及含义：

| 状态码 | 中文名 | 含义 |
|--------|--------|------|
| `DRAFT` | 草稿 | 工单刚创建，尚未提交 |
| `SUBMITTED` | 已提交 | 工单已提交，等待审批 |
| `APPROVED` | 已审批 | 审批通过，等待分配处理人 |
| `ASSIGNED` | 已分配 | 已指定处理人，等待开始处理 |
| `PROCESSING` | 处理中 | 处理人正在处理工单 |
| `RESOLVED` | 已解决 | 处理人认为问题已解决 |
| `VERIFIED` | 已验证 | 创建人确认问题已解决 |
| `CLOSED` | 已关闭 | 工单流程结束 |
| `REJECTED` | 已驳回 | 审批人认为工单不通过 |
| `CANCELLED` | 已取消 | 工单被取消 |

15 条转换规则及所需权限：

| 转换 | 起始状态 → 目标状态 | 所需权限 | 操作名称 |
|------|---------------------|----------|----------|
| `SUBMIT` | DRAFT → SUBMITTED | `itsm:ticket:edit` | 提交工单 |
| `APPROVE` | SUBMITTED → APPROVED | `itsm:ticket:approve` | 审批通过 |
| `REJECT` | SUBMITTED → REJECTED | `itsm:ticket:approve` | 审批驳回 |
| `ASSIGN` | APPROVED → ASSIGNED | `itsm:ticket:assign` | 分配工单 |
| `START_PROCESS` | ASSIGNED → PROCESSING | `itsm:ticket:edit` | 开始处理 |
| `RESOLVE` | PROCESSING → RESOLVED | `itsm:ticket:edit` | 解决工单 |
| `VERIFY` | RESOLVED → VERIFIED | `itsm:ticket:approve` | 验证通过 |
| `REOPEN` | RESOLVED → PROCESSING | `itsm:ticket:approve` | 重新处理 |
| `CLOSE` | VERIFIED → CLOSED | `itsm:ticket:approve` | 关闭工单 |
| `REDRAFT` | REJECTED → DRAFT | `itsm:ticket:edit` | 重新编辑 |
| `CANCEL_FROM_DRAFT` | DRAFT → CANCELLED | `itsm:ticket:edit` | 取消工单 |
| `CANCEL_FROM_SUBMITTED` | SUBMITTED → CANCELLED | `itsm:ticket:edit` | 取消工单 |
| `CANCEL_FROM_APPROVED` | APPROVED → CANCELLED | `itsm:ticket:assign` | 取消工单 |
| `CANCEL_FROM_ASSIGNED` | ASSIGNED → CANCELLED | `itsm:ticket:assign` | 取消工单 |
| `CANCEL_FROM_PROCESSING` | PROCESSING → CANCELLED | `itsm:ticket:edit` | 取消工单 |

### 2.2 数据的完整生命周期：从产生到存储

以"创建一个工单"为例，追踪数据的完整流转：

```
用户点击"新建工单"
    │
    ▼
[1] 前端 Vue 页面
    │  ticket.js: addTicket(data) → POST /itsm/ticket
    │  data = { title, description, categoryId, priority, ... }
    ▼
[2] Controller 层 (ItTicketController.add)
    │  @PreAuthorize 检查权限: itsm:ticket:add
    │  @Log 记录操作日志
    │  @Validated 校验参数 (title不能为空, 不超过200字)
    │  调用 ticketService.insertTicket(ticket)
    ▼
[3] Service 层 (ItTicketServiceImpl.insertTicket)
    │  ① 生成工单编号: TK-20260517-0001 (Redis自增)
    │  ② 设置初始状态: DRAFT
    │  ③ 设置创建人: 从 SecurityUtils 获取当前登录用户
    │  ④ 查找关联工作流: workflowEngine.getWorkflowIdByCategory()
    │  ⑤ 插入数据库: ticketMapper.insertTicket(ticket)
    │  ⑥ 记录操作日志: recordLog("CREATE", ...)
    │  ⑦ 启动工作流实例: workflowEngine.startProcess()
    │  ⑧ 记录流程日志: recordProcessLog("CREATE", ...)
    ▼
[4] Mapper 层 (ItTicketMapper)
    │  执行 SQL: INSERT INTO it_ticket (ticket_no, title, status, ...) VALUES (...)
    │  useGeneratedKeys=true → 自动回填 ticketId
    ▼
[5] MySQL 数据库
    │  数据持久化到 it_ticket 表
    │  同时 it_ticket_log 表记录了操作日志
    │  同时 it_ticket_process_log 表记录了流程日志
    ▼
[6] 返回响应
    │  Controller: toAjax(rows) → { code: 200, msg: "操作成功" }
    ▼
[7] 前端收到响应，刷新列表
```

### 2.3 状态转换的数据流转（以"审批工单"为例）

```
前端: approveTicket({ ticketId, status: "APPROVED", remark: "同意" })
    │
    ▼
Controller: approve(@RequestBody ItTicket ticket)
    │  权限检查: itsm:ticket:approve
    ▼
Service: approveTicket(ticket)
    │  ① 查出原工单: getAndValidate(ticketId)
    │  ② 状态机验证: stateMachine.validateTransition(SUBMITTED → APPROVED)
    │     如果不合法，抛出 IllegalStateException
    │  ③ 设置审批人: ticket.setApproverId(当前用户ID)
    │  ④ 更新数据库: ticketMapper.approveTicket(ticket)
    │  ⑤ 记录操作日志: recordLog("APPROVE", "审批通过", SUBMITTED→APPROVED)
    │  ⑥ 更新工作流节点: updateWorkflowNode()
    │  ⑦ 记录流程日志: recordProcessLog()
    │  ⑧ 发布事件: eventPublisher.publishEvent(new TicketEvent(...))
    ▼
事件监听器: TicketEventListener.handleTicketEvent()
    │  检测到 action="APPROVE"
    │  ① 创建通知: "工单待分配: xxx"
    │  ② WebSocket推送: webSocketPushService.pushNotification()
    │  ③ 广播工单更新: webSocketPushService.broadcastTicketUpdate()
```

---

## 三、关键技术点在工单模块中的具体应用

### 3.1 注解（Annotation）—— JavaSE 知识的延伸

你在 JavaSE 中学过 `@Override`、`@Deprecated` 这些内置注解。Spring Boot 大量使用自定义注解来实现"声明式编程"——你只需要"声明"你想要什么，框架帮你实现。

**工单模块中的注解一览**：

| 注解 | 位置 | 作用 | JavaSE 类比 |
|------|------|------|-------------|
| `@RestController` | Controller类 | 告诉Spring这个类处理HTTP请求，返回JSON | 类似给类贴标签："我是HTTP处理器" |
| `@RequestMapping` | Controller类 | 指定URL前缀 | 类似注册一个URL路由 |
| `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` | 方法 | 指定HTTP方法和路径 | 类似 `if(method=="GET" && path=="/list")` |
| `@Autowired` | 字段 | 自动注入依赖对象 | 类似 `this.service = BeanFactory.get("ItTicketService")` |
| `@PreAuthorize` | 方法 | 权限检查 | 类似 `if(!user.hasPermission("itsm:ticket:add")) throw new Exception()` |
| `@Transactional` | 方法 | 事务管理，出错自动回滚 | 类似 `try { ... } catch { connection.rollback(); }` |
| `@Validated` | 参数 | 参数校验 | 类似 `if(title == null) throw new Exception("标题不能为空")` |
| `@Service` | Service类 | 标记为业务层组件 | 类似给类贴标签："我是业务逻辑类，请管理我" |
| `@Component` | 通用组件 | 标记为Spring管理的组件 | 类似给类贴标签："请把我放进Bean工厂" |
| `@DataScope` | Service方法 | 数据权限过滤 | 类似在SQL后面自动拼接 `WHERE dept_id = ?` |
| `@Log` | Controller方法 | 操作日志记录 | 类似在方法前后自动记录日志 |
| `@Async` | 事件监听方法 | 异步执行 | 类似 `new Thread(() -> handleEvent()).start()` |
| `@EventListener` | 事件监听方法 | 监听Spring事件 | 类似注册一个回调函数 |
| `@Excel` | 实体字段 | Excel导入导出标记 | 类似给字段贴标签："这个字段要导出到Excel" |
| `@JsonFormat` | 实体字段 | JSON日期格式化 | 类似 `SimpleDateFormat.format(date)` |
| `@NotBlank` | 实体字段 | 校验：不能为空 | 类似 `if(str == null || str.isEmpty()) throw ...` |
| `@Size` | 实体字段 | 校验：长度限制 | 类似 `if(str.length() > max) throw ...` |
| `@NotNull` | DTO字段 | 校验：不能为null | 类似 `if(obj == null) throw ...` |

### 3.2 状态机模式 —— 用枚举实现有限状态自动机

#### 3.2.1 状态枚举 — TicketStatus

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/enums/TicketStatus.java`

```java
public enum TicketStatus {
    DRAFT("DRAFT", "草稿"),
    SUBMITTED("SUBMITTED", "已提交"),
    APPROVED("APPROVED", "已审批"),
    ASSIGNED("ASSIGNED", "已分配"),
    PROCESSING("PROCESSING", "处理中"),
    RESOLVED("RESOLVED", "已解决"),
    VERIFIED("VERIFIED", "已验证"),
    CLOSED("CLOSED", "已关闭"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;  // 存入数据库的值
    private final String desc;  // 给用户看的中文描述

    TicketStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据code反查枚举
    public static TicketStatus fromCode(String code) {
        for (TicketStatus status : values()) {
            if (status.code.equals(code)) return status;
        }
        return null;
    }
}
```

**JavaSE 类比**：枚举你一定学过，比如 `enum Day { MON, TUE, WED }`。这里的枚举只是多了两个字段：`code`（存数据库的值）和 `desc`（给用户看的中文描述）。就像你给每个枚举值绑定了额外的数据。`fromCode()` 方法就像一个查找函数——输入字符串，输出对应的枚举值。

#### 3.2.2 转换规则枚举 — TicketTransition

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/enums/TicketTransition.java`

```java
public enum TicketTransition {
    SUBMIT(DRAFT, SUBMITTED, "itsm:ticket:edit", "提交工单"),
    APPROVE(SUBMITTED, APPROVED, "itsm:ticket:approve", "审批通过"),
    REJECT(SUBMITTED, REJECTED, "itsm:ticket:approve", "审批驳回"),
    // ... 共15条
    ;

    private final TicketStatus from;      // 起始状态
    private final TicketStatus to;        // 目标状态
    private final String permission;      // 所需权限
    private final String action;          // 操作名称

    // 查找从 from 到 to 的转换规则
    public static TicketTransition findTransition(TicketStatus from, TicketStatus to) {
        for (TicketTransition transition : values()) {
            if (transition.from == from && transition.to == to) return transition;
        }
        return null;
    }

    // 判断转换是否合法
    public static boolean isValidTransition(TicketStatus from, TicketStatus to) {
        return findTransition(from, to) != null;
    }
}
```

**JavaSE 类比**：这就像一个"规则表"，每条规则记录了：从哪个状态 → 到哪个状态 → 需要什么权限 → 操作叫什么名字。`findTransition()` 就是在规则表中查找匹配项。

#### 3.2.3 状态机引擎 — TicketStateMachine

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/statemachine/TicketStateMachine.java`

```java
@Component  // 告诉Spring：请创建这个类的实例并管理
public class TicketStateMachine {
    // EnumMap — JavaSE中你学过HashMap，EnumMap是专门为枚举key优化的HashMap
    private final Map<TicketStatus, List<TicketTransition>> transitionMap = new EnumMap<>(TicketStatus.class);

    public TicketStateMachine() {
        // 构造时把所有转换规则按"起始状态"分组
        for (TicketTransition transition : TicketTransition.values()) {
            transitionMap.computeIfAbsent(transition.getFrom(), k -> new ArrayList<>()).add(transition);
        }
    }

    // 验证转换是否合法，不合法则抛异常
    public TicketTransition validateTransition(TicketStatus from, TicketStatus to) {
        List<TicketTransition> transitions = transitionMap.get(from);
        if (transitions != null) {
            for (TicketTransition t : transitions) {
                if (t.getTo() == to) return t;
            }
        }
        throw new IllegalStateException(
            String.format("不允许从状态[%s]转换到[%s]", from.getDesc(), to.getDesc()));
    }

    // 获取当前状态可用的所有转换
    public List<TicketTransition> getAvailableTransitions(TicketStatus currentStatus) {
        return transitionMap.getOrDefault(currentStatus, new ArrayList<>());
    }

    // 获取当前状态可到达的所有目标状态
    public List<TicketStatus> getAvailableTargetStatuses(TicketStatus currentStatus) {
        List<TicketTransition> transitions = getAvailableTransitions(currentStatus);
        List<TicketStatus> targets = new ArrayList<>();
        for (TicketTransition transition : transitions) {
            targets.add(transition.getTo());
        }
        return targets;
    }
}
```

**JavaSE 类比**：这就像你写了一个"状态检查器"——输入当前状态和目标状态，它查表告诉你"行不行"。如果不行就抛异常。跟你在 JavaSE 中写 `if (status == DRAFT && target == SUBMITTED) { ... }` 是一样的思路，只是把所有规则集中管理了。

**为什么用 EnumMap 而不是 HashMap？** `EnumMap` 是 JavaSE 中专门为枚举 key 设计的 Map，内部用数组实现，比 HashMap 更快更省内存。因为枚举值的数量是固定的，可以直接用枚举的 ordinal 作为数组下标。

### 3.3 Redis 生成工单编号 —— 分布式计数器

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/service/ticket/impl/ItTicketServiceImpl.java`

```java
private static final String TICKET_NO_PREFIX = "TK";

public String generateTicketNo() {
    // 1. 获取当前日期字符串，如 "20260517"
    String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // 2. 拼接Redis的key，如 "itsm:ticket:seq:20260517"
    String cacheKey = "itsm:ticket:seq:" + dateStr;

    // 3. Redis原子自增，每次调用+1，返回自增后的值
    Long seq = redisTemplate.opsForValue().increment(cacheKey, 1);

    // 4. 设置key的过期时间为25小时（保证跨天前一定过期）
    redisTemplate.expire(cacheKey, 25, TimeUnit.HOURS);

    // 5. 防止编号溢出
    if (seq > 9999) {
        throw new ServiceException("当日工单编号已用尽，请联系管理员");
    }

    // 6. 拼接最终编号，如 "TK-20260517-0001"
    return TICKET_NO_PREFIX + "-" + dateStr + "-" + String.format("%04d", seq);
}
```

**JavaSE 类比**：如果用 JavaSE 实现，你可能会写 `static int counter = 0; counter++;`。但问题是：如果两个用户同时创建工单，可能拿到相同的编号（并发问题）。Redis 的 `increment` 是**原子操作**——就像 `AtomicInteger.incrementAndGet()`，但它是跨进程的，多个服务器也能保证不重复。

**为什么用 Redis 而不用数据库自增？** 数据库自增是连续的，但 Redis 更灵活：每天重置计数、格式自定义、性能更高（内存操作 vs 磁盘操作）。

### 3.4 Spring Event 事件机制 —— 观察者模式

**JavaSE 类比**：就像你在 JavaSE 中写的事件监听器——按钮点击事件 `button.addActionListener(e -> {...})`。Spring Event 也是这个模式，只是更通用。

#### 3.4.1 事件定义 — TicketEvent

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/event/TicketEvent.java`

```java
public class TicketEvent extends ApplicationEvent {
    private final ItTicket ticket;       // 关联的工单
    private final String action;         // 操作类型（如 SUBMIT, APPROVE）
    private final String fromStatus;     // 原状态
    private final String toStatus;       // 目标状态
    private final Long operatorId;       // 操作人ID
    private final String operatorName;   // 操作人姓名

    public TicketEvent(Object source, ItTicket ticket, String action,
                       String fromStatus, String toStatus,
                       Long operatorId, String operatorName) {
        super(source);
        this.ticket = ticket;
        this.action = action;
        // ...
    }
}
```

**JavaSE 类比**：`ApplicationEvent` 就像 `java.util.EventObject`，是所有事件的基类。`TicketEvent` 是自定义的事件，携带了工单状态变更的所有信息。

#### 3.4.2 事件发布

在 Service 层的状态变更方法中发布事件：

```java
eventPublisher.publishEvent(new TicketEvent(this, existing, transition.name(),
    existing.getStatus(), transition.getTo().getCode(),
    SecurityUtils.getUserId(), SecurityUtils.getUsername()));
```

**JavaSE 类比**：就像 `button.fireEvent(new ActionEvent(...))`，触发一个事件。

#### 3.4.3 事件监听 — TicketEventListener

文件位置：`itsm-ticket/src/main/java/com/itsm/ticket/event/TicketEventListener.java`

```java
@Component
public class TicketEventListener {
    @Autowired
    private IItNotificationService notificationService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    @Async         // 异步执行，不阻塞主流程
    @EventListener // 标记为事件监听器
    public void handleTicketEvent(TicketEvent event) {
        ItTicket ticket = event.getTicket();
        String action = event.getAction();

        if ("SUBMIT".equals(action)) {
            // 提交后通知审批人
            ItNotification n = sendNotificationToApprovers(ticket, event);
            webSocketPushService.pushNotification(1L, n);
        } else if ("APPROVE".equals(action)) {
            // 审批通过后通知分配人
            ItNotification n = sendNotificationToAssigners(ticket, event);
            webSocketPushService.pushNotification(1L, n);
        } else if ("ASSIGN".equals(action)) {
            // 分配后通知处理人
            ItNotification n = sendNotificationToAssignee(ticket, event);
            if (n != null) webSocketPushService.pushNotification(ticket.getAssigneeId(), n);
        }
        // ... RESOLVE, REJECT, CLOSE 等类似处理

        // 广播工单更新（让其他在线用户看到状态变化）
        webSocketPushService.broadcastTicketUpdate(event);
    }
}
```

**为什么用事件而不是直接调用？** 解耦。Service 层只管"业务逻辑"，通知推送是"副作用"，放在监听器里，主流程更清晰。如果以后要加新的副作用（比如发邮件），只需要加一个新的监听器，不用改 Service 代码。

**`@Async` 的作用**：让监听器在另一个线程执行，不影响主流程的响应速度。就像 `new Thread(() -> handleEvent()).start()`，但 Spring 帮你管理线程池。

### 3.5 AOP 切面编程 —— 在不修改代码的情况下增加功能

**JavaSE 类比**：想象你写了一个方法 `void transferMoney()`，现在需要在执行前后加日志。JavaSE 的做法是修改方法内部代码。AOP 的做法是"在外面套一层"，完全不碰原方法。

#### 3.5.1 操作日志切面 — LogAspect

文件位置：`itsm-framework/src/main/java/com/itsm/framework/aspectj/LogAspect.java`

当 Controller 方法标了 `@Log(title="工单管理", businessType=BusinessType.INSERT)` 时，切面自动执行：

```
方法执行前 (@Before):
  记录开始时间

方法正常返回后 (@AfterReturning):
  获取当前用户、请求参数、返回结果、耗时
  异步写入 sys_oper_log 表

方法抛异常后 (@AfterThrowing):
  获取异常信息
  异步写入 sys_oper_log 表（状态为失败）
```

**JavaSE 类比**：就像你写了一个代理类：

```java
// JavaSE 的做法
class TicketServiceProxy {
    TicketService realService;

    public int insertTicket(ItTicket ticket) {
        long start = System.currentTimeMillis();
        try {
            int result = realService.insertTicket(ticket);
            long cost = System.currentTimeMillis() - start;
            saveLog("insertTicket", cost, result, null);
            return result;
        } catch (Exception e) {
            saveLog("insertTicket", 0, null, e.getMessage());
            throw e;
        }
    }
}
```

AOP 就是 Spring 帮你自动生成这个代理类，你只需要加个 `@Log` 注解。

#### 3.5.2 数据权限切面 — DataScopeAspect

文件位置：`itsm-framework/src/main/java/com/itsm/framework/aspectj/DataScopeAspect.java`

当 Service 方法标了 `@DataScope(deptAlias="t", userAlias="t", deptField="creator_dept_id", userField="creator_id")` 时，切面自动执行：

1. 获取当前登录用户
2. 根据用户的角色数据权限范围，拼接 SQL 条件
3. 把条件塞进 `ticket.params.put("dataScope", " AND (t.creator_dept_id = 103)")`

不同角色的数据权限范围：

| 数据权限 | SQL 拼接效果 | 含义 |
|----------|-------------|------|
| 全部数据 | 不拼接条件 | 管理员可以看所有工单 |
| 自定义数据 | `AND dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)` | 指定部门的数据 |
| 本部门数据 | `AND t.creator_dept_id = 103` | 只看本部门创建的工单 |
| 本部门及以下 | `AND t.creator_dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id=103 or find_in_set(103, ancestors))` | 本部门+子部门 |
| 仅本人 | `AND t.creator_id = 1` | 只看自己创建的工单 |

---

## 四、各功能模块间的交互方式及接口设计

### 4.1 前后端交互 —— RESTful API

前端通过 HTTP 请求与后端交互，遵循 RESTful 风格：

| HTTP方法 | URL | 含义 | 对应前端API |
|----------|-----|------|-------------|
| `GET` | `/itsm/ticket/list` | 查询工单列表 | `listTicket(query)` |
| `GET` | `/itsm/ticket/{id}` | 查询单个工单 | `getTicket(ticketId)` |
| `POST` | `/itsm/ticket` | 新建工单 | `addTicket(data)` |
| `PUT` | `/itsm/ticket` | 编辑工单 | `updateTicket(data)` |
| `DELETE` | `/itsm/ticket/{ids}` | 删除工单 | `delTicket(ticketId)` |
| `PUT` | `/itsm/ticket/assign` | 分配工单 | `assignTicket(data)` |
| `PUT` | `/itsm/ticket/approve` | 审批工单 | `approveTicket(data)` |
| `PUT` | `/itsm/ticket/transit` | 通用状态转换 | `transitTicket(data)` |
| `GET` | `/itsm/ticket/transitions/{id}` | 获取可用转换 | `getAvailableTransitions(ticketId)` |
| `GET` | `/itsm/ticket/workflow-transitions/{id}` | 获取工作流转换 | `getAvailableWorkflowTransitions(ticketId)` |
| `GET` | `/itsm/ticket/log/{id}` | 获取操作日志 | `listTicketLog(ticketId)` |
| `GET` | `/itsm/ticket/process-log/{id}` | 获取流程日志 | `listProcessLog(ticketId)` |
| `POST` | `/itsm/ticket/export` | 导出Excel | - |

**JavaSE 类比**：RESTful 风格就是用 HTTP 方法区分操作类型——GET 是读、POST 是增、PUT 是改、DELETE 是删。就像你设计一个接口：`interface TicketDao { Ticket get(id); void add(Ticket); void update(Ticket); void delete(id); }`，RESTful 只是用 URL 和 HTTP 方法来表达同样的意思。

**前端 API 文件结构**：

```
itsm-ui/src/api/itsm/
  ticket.js       — 工单相关 12 个 API
  category.js     — 分类相关 5 个 API
  notification.js — 通知相关 API
  task.js         — 工作流任务 API
  workflow.js     — 工作流定义 API
```

前端 API 调用示例（`ticket.js`）：

```javascript
import request from '@/utils/request'

export function listTicket(query) {
  return request({
    url: '/itsm/ticket/list',
    method: 'get',
    params: query
  })
}

export function addTicket(data) {
  return request({
    url: '/itsm/ticket',
    method: 'post',
    data: data
  })
}
```

### 4.2 模块间交互图

```
┌─────────────────────────────────────────────────────────────────┐
│                        itsm-admin (Controller层)                 │
│  ItTicketController ←→ ItTicketCategoryController               │
│  ItTicketAttachmentController ←→ ItNotificationController       │
│  WfWorkflowController ←→ WfTaskController                       │
└──────────┬──────────────────────────────────────────────────────┘
           │ 调用
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     itsm-ticket (Service层)                      │
│  IItTicketService ←→ IItTicketLogService                        │
│  IItTicketCategoryService ←→ IItTicketAttachmentService         │
│  IItNotificationService ←→ IItTicketProcessLogService           │
│  IWorkflowEngine ←→ IWfInstanceService ←→ IWfTaskService        │
│  WebSocketPushService (WebSocket推送)                            │
│                                                                  │
│  TicketStateMachine (状态验证)                                    │
│  TicketEvent → TicketEventListener (事件驱动通知)                 │
└──────────┬──────────────────────────────────────────────────────┘
           │ 调用
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     itsm-ticket (Mapper层)                       │
│  ItTicketMapper ←→ ItTicketLogMapper                            │
│  ItTicketCategoryMapper ←→ ItTicketAttachmentMapper             │
│  ItNotificationMapper ←→ ItTicketProcessLogMapper               │
│  WfWorkflowMapper ←→ WfInstanceMapper ←→ WfTaskMapper           │
│  WfNodeMapper ←→ WfTransitionMapper                             │
└──────────┬──────────────────────────────────────────────────────┘
           │ SQL
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MySQL 数据库                               │
│  it_ticket | it_ticket_log | it_ticket_category                 │
│  it_ticket_attachment | it_notification | it_ticket_process_log  │
│  wf_workflow | wf_instance | wf_node | wf_task | wf_transition  │
└─────────────────────────────────────────────────────────────────┘
```

**跨模块交互**：

- `itsm-ticket` → `itsm-common`：使用 BaseEntity、AjaxResult、SecurityUtils 等公共组件
- `itsm-ticket` → `itsm-framework`：通过 `@DataScope` 注解使用数据权限切面，通过 `@Log` 使用操作日志切面
- `itsm-admin` → `itsm-ticket`：Controller 调用 Service 接口
- `itsm-ticket` → Redis：工单编号生成、登录用户缓存

### 4.3 认证鉴权交互流程

#### 4.3.1 登录流程

```
[登录]
  前端 POST /login { username, password }
    → SysLoginService.login()
      → AuthenticationManager.authenticate()  // 验证用户名密码
      → TokenService.createToken(loginUser)   // 创建JWT令牌
        → 生成UUID，存入Redis（key=login_tokens:uuid, value=loginUser, TTL=30分钟）
        → 用JWT库签名生成token字符串
    → 返回 { token: "eyJhbGciOi..." }
```

#### 4.3.2 请求鉴权流程

```
[后续请求]
  前端每次请求 Header: Authorization: Bearer eyJhbGciOi...
    → JwtAuthenticationTokenFilter 拦截
      → 从Header取token
      → TokenService.getLoginUser(request)
        → 解析JWT，取出UUID
        → 从Redis取出LoginUser对象
      → 验证token有效期，不足20分钟自动续期
      → 将LoginUser存入SecurityContext
    → 到达Controller方法
      → @PreAuthorize("@ss.hasPermi('itsm:ticket:list')")
        → PermissionService检查当前用户是否有此权限
```

**JavaSE 类比**：
- JWT 就像一张"加密身份证"，上面只写了一个 UUID（你的编号）
- Redis 就像一个"档案柜"，用 UUID 能查到你的完整信息
- 每次请求就像"出示身份证 → 查档案 → 确认身份 → 检查权限"

#### 4.3.3 TokenService 核心代码解读

文件位置：`itsm-framework/src/main/java/com/itsm/framework/web/service/TokenService.java`

```java
@Component
public class TokenService {
    @Value("${token.header}")     // 从配置文件读取: "Authorization"
    private String header;

    @Value("${token.secret}")     // 从配置文件读取: 64字节密钥
    private String secret;

    @Value("${token.expireTime}") // 从配置文件读取: 30 (分钟)
    private int expireTime;

    // 创建令牌
    public String createToken(LoginUser loginUser) {
        String token = IdUtils.fastUUID();  // 生成UUID
        loginUser.setToken(token);
        refreshToken(loginUser);            // 存入Redis

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);  // JWT中只存UUID
        return createToken(claims);          // 用HS512签名生成JWT字符串
    }

    // 从请求中获取登录用户
    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);    // 从Header取JWT
        Claims claims = parseToken(token);   // 解析JWT
        String uuid = claims.get(Constants.LOGIN_USER_KEY);  // 取出UUID
        String userKey = getTokenKey(uuid);  // 拼接Redis key
        return redisCache.getCacheObject(userKey);  // 从Redis取用户信息
    }

    // 刷新令牌有效期（不足20分钟时自动续期）
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY) {
            refreshToken(loginUser);
        }
    }
}
```

---

## 五、SpringBoot 实现工单功能的核心代码逻辑与执行流程

### 5.1 完整的"创建工单"执行流程

```
1. 用户在前端填写表单，点击"提交"
2. 前端调用 addTicket(data)，发送 POST /itsm/ticket
3. 请求经过过滤器链：
   CorsFilter → JwtAuthenticationTokenFilter → Security权限检查
4. 到达 ItTicketController.add()：
   a. @PreAuthorize("@ss.hasPermi('itsm:ticket:add')") — 检查权限
   b. @Log(title="工单管理", businessType=INSERT) — 标记需要记录日志
   c. @Validated — 校验参数（title不能为空等）
   d. 调用 ticketService.insertTicket(ticket)
5. ItTicketServiceImpl.insertTicket()：
   a. generateTicketNo() — Redis自增生成编号 TK-20260517-0001
   b. 设置初始值：status=DRAFT, source=WEB, creatorId=当前用户
   c. 查找关联工作流：workflowEngine.getWorkflowIdByCategory()
   d. ticketMapper.insertTicket(ticket) — 执行INSERT SQL
   e. recordLog() — 记录操作日志到 it_ticket_log
   f. workflowEngine.startProcess() — 启动工作流实例
   g. 更新 workflowInstanceId 到工单
   h. recordProcessLog() — 记录流程日志到 it_ticket_process_log
6. LogAspect 切面异步记录操作日志到 sys_oper_log
7. 返回 AjaxResult.success() → { code: 200, msg: "操作成功" }
8. 前端收到成功响应，刷新工单列表
```

### 5.2 通用状态转换 —— transitTicket 的精巧设计

Controller 端只有一个通用入口：

```java
@PutMapping("/transit")
public AjaxResult transit(@Validated @RequestBody TicketTransitDTO dto) {
    return toAjax(ticketService.transitTicket(dto));
}
```

前端传入 `TicketTransitDTO`：

```json
{ "ticketId": 1, "targetStatus": "PROCESSING", "remark": "开始处理" }
```

Service 层 `transitTicket(TicketTransitDTO)` 的执行逻辑：

```java
public int transitTicket(TicketTransitDTO dto) {
    // 1. 把字符串转为枚举
    TicketStatus targetStatus = TicketStatus.fromCode(dto.getTargetStatus());

    // 2. 查出原工单
    ItTicket existing = getAndValidate(dto.getTicketId());

    // 3. 查找转换规则
    TicketTransition transition = TicketTransition.findTransition(
        TicketStatus.fromCode(existing.getStatus()), targetStatus);

    // 4. 检查权限 — 每条转换规则都绑定了所需权限
    String permission = transition.getPermission();
    if (!SecurityUtils.hasPermi(permission)) {
        throw new ServiceException("没有执行此操作的权限");
    }

    // 5. 执行转换
    ItTicket ticket = new ItTicket();
    ticket.setTicketId(dto.getTicketId());
    ticket.setRemark(dto.getRemark());
    return transitTicket(ticket, targetStatus);
}
```

**为什么这样设计？** 因为工单有15种状态转换，如果每种都写一个 Controller 方法，代码会非常冗余。`transitTicket` 是一个**通用入口**，前端只需要传"目标状态"，后端自动验证合法性、检查权限、处理特殊字段。

### 5.3 特殊状态字段处理

不同的状态转换需要设置不同的业务字段：

```java
private void handleSpecialStatusFields(ItTicket ticket, ItTicket existing, TicketTransition transition) {
    switch (transition) {
        case ASSIGN -> {
            // 分配时：设置处理部门
            if (ticket.getAssigneeId() != null) {
                ticket.setAssigneeDeptId(existing.getAssigneeDeptId());
            }
        }
        case RESOLVE -> {
            // 解决时：记录解决人和解决时间
            ticket.setResolvedBy(SecurityUtils.getUserId());
            ticket.setActualResolveTime(new Date());
        }
        case CLOSE -> {
            // 关闭时：记录关闭人和关闭时间
            ticket.setClosedBy(SecurityUtils.getUserId());
            ticket.setCloseTime(new Date());
        }
        default -> {}
    }
}
```

**JavaSE 类比**：这就是一个 `switch` 语句，根据不同的转换类型设置不同的字段。跟你在 JavaSE 中写的逻辑完全一样，只是用了 Java 14+ 的箭头语法 `case X -> {}`。

### 5.4 数据权限过滤 —— 看不见的 SQL 拼接

Service 层方法声明：

```java
@DataScope(deptAlias = "t", userAlias = "t", deptField = "creator_dept_id", userField = "creator_id")
public List<ItTicket> selectTicketList(ItTicket ticket) {
    return ticketMapper.selectTicketList(ticket);
}
```

当调用这个方法时，`DataScopeAspect` 切面会**在方法执行前**自动执行：
1. 获取当前登录用户
2. 根据用户的角色数据权限范围，拼接 SQL 条件
3. 把条件塞进 `ticket.params.put("dataScope", " AND (t.creator_dept_id = 103)")`

然后在 Mapper XML 中：

```xml
<select id="selectTicketList" parameterType="ItTicket" resultMap="ItTicketResult">
    <include refid="selectTicketVo"/>
    <where>
        t.del_flag = '0'
        <!-- 各种查询条件 -->
        ${params.dataScope}  <!-- 这里插入权限过滤SQL -->
    </where>
    order by t.create_time desc
</select>
```

这行 `${params.dataScope}` 会把拼接好的权限 SQL 追加到查询条件后面。

**效果**：不同用户查询同一个列表接口，看到的工单范围不同——管理员看全部，部门经理看本部门，普通员工只看自己创建的。

### 5.5 逻辑删除 —— 数据不会真正消失

Mapper XML 中的"删除"操作：

```xml
<delete id="deleteTicketById" parameterType="Long">
    update it_ticket set del_flag = '2' where ticket_id = #{ticketId} and del_flag = '0'
</delete>
```

注意：虽然方法名叫 `delete`，但实际执行的是 `UPDATE`——把 `del_flag` 从 `'0'`（正常）改为 `'2'`（已删除）。所有查询都带 `del_flag = '0'` 条件，所以"已删除"的数据对用户不可见，但数据库里还保留着。

Service 层还做了前置校验：

```java
public void deleteTicketByIds(Long[] ticketIds) {
    for (Long ticketId : ticketIds) {
        ItTicket existing = ticketMapper.selectTicketById(ticketId);
        if (existing == null) continue;

        TicketStatus currentStatus = TicketStatus.fromCode(existing.getStatus());
        // 只有草稿、已驳回或已取消的工单才能删除
        if (currentStatus != TicketStatus.DRAFT
            && currentStatus != TicketStatus.REJECTED
            && currentStatus != TicketStatus.CANCELLED) {
            throw new ServiceException("只有草稿、已驳回或已取消的工单才能删除");
        }

        // 如果关联了工作流，先终止工作流
        if (existing.getWorkflowInstanceId() != null) {
            workflowEngine.terminateProcess(existing.getWorkflowInstanceId());
        }

        ticketMapper.deleteTicketById(ticketId);  // 逻辑删除
        recordLog(ticketId, existing.getTicketNo(), "DELETE", "删除工单", ...);
    }
}
```

**JavaSE 类比**：就像你用一个 `List<Ticket>`，但删除时不调用 `list.remove()`，而是 `ticket.setDeleted(true)`，查询时 `list.stream().filter(t -> !t.isDeleted())`。

---

## 六、从 JavaSE 到 Spring Boot 的思维转换

| JavaSE 思维 | Spring Boot 思维 |
|-------------|-----------------|
| `new XxxService()` 手动创建对象 | `@Autowired` 让 Spring 自动注入 |
| `if-else` 路由请求 | `@GetMapping` 注解声明路由 |
| `try-catch-finally` 管事务 | `@Transactional` 声明事务 |
| 修改代码加日志 | `@Log` 注解 + AOP 切面自动加日志 |
| `synchronized` 或 `AtomicInteger` 保证并发安全 | Redis 原子操作保证分布式并发安全 |
| 接口回调/观察者模式 | Spring Event 事件机制 |
| 手写 JDBC/SQL | MyBatis XML 映射 + Mapper 接口 |
| `session` 存登录状态 | JWT + Redis 无状态认证 |
| 物理删除 `list.remove()` | 逻辑删除 `del_flag = '2'` |
| 硬编码权限检查 | `@PreAuthorize` 注解声明权限 |
| 手动分页 `subList()` | `startPage()` + MyBatis Plus 分页插件 |

**核心思想**：Spring Boot 的核心理念是**约定优于配置**和**声明式编程**。你不需要写大量样板代码，只需要用注解"声明"你的意图，框架帮你完成具体实现。这就像从"手动挡"换到了"自动挡"——底层原理还是一样的，但操作更简洁了。

---

## 附录：工单模块核心文件索引

### 后端文件

| 分层 | 文件路径 | 说明 |
|------|----------|------|
| 入口 | `itsm-admin/.../ItsmApplication.java` | Spring Boot 启动类 |
| 配置 | `itsm-admin/.../application.yml` | 主配置文件 |
| 配置 | `itsm-admin/.../application-druid.yml` | 数据源配置 |
| 实体 | `itsm-ticket/.../domain/ticket/ItTicket.java` | 工单主表实体 |
| 实体 | `itsm-ticket/.../domain/ticket/ItTicketCategory.java` | 工单分类实体 |
| 实体 | `itsm-ticket/.../domain/ticket/ItTicketLog.java` | 工单操作日志实体 |
| 实体 | `itsm-ticket/.../domain/ticket/ItTicketAttachment.java` | 工单附件实体 |
| 实体 | `itsm-ticket/.../domain/ticket/ItTicketProcessLog.java` | 工单流程日志实体 |
| 实体 | `itsm-ticket/.../domain/ticket/ItNotification.java` | 通知实体 |
| 实体 | `itsm-ticket/.../domain/ticket/TicketTransitDTO.java` | 状态转换DTO |
| 枚举 | `itsm-ticket/.../enums/TicketStatus.java` | 工单状态枚举（10个状态） |
| 枚举 | `itsm-ticket/.../enums/TicketTransition.java` | 状态转换枚举（15条规则） |
| 状态机 | `itsm-ticket/.../statemachine/TicketStateMachine.java` | 状态机引擎 |
| 事件 | `itsm-ticket/.../event/TicketEvent.java` | 工单事件定义 |
| 事件 | `itsm-ticket/.../event/TicketEventListener.java` | 工单事件监听器 |
| Service | `itsm-ticket/.../service/ticket/IItTicketService.java` | 工单Service接口 |
| Service | `itsm-ticket/.../service/ticket/impl/ItTicketServiceImpl.java` | 工单Service实现 |
| Controller | `itsm-admin/.../controller/itsm/ItTicketController.java` | 工单Controller |
| Controller | `itsm-admin/.../controller/itsm/ItTicketCategoryController.java` | 分类Controller |
| Controller | `itsm-admin/.../controller/itsm/ItTicketAttachmentController.java` | 附件Controller |
| Controller | `itsm-admin/.../controller/itsm/ItNotificationController.java` | 通知Controller |
| Mapper | `itsm-ticket/.../mapper/ticket/ItTicketMapper.java` | 工单Mapper接口 |
| Mapper XML | `itsm-ticket/.../resources/mapper/ticket/ItTicketMapper.xml` | 工单SQL映射 |
| 安全 | `itsm-framework/.../config/SecurityConfig.java` | Spring Security配置 |
| 安全 | `itsm-framework/.../web/service/TokenService.java` | JWT令牌服务 |
| 切面 | `itsm-framework/.../aspectj/LogAspect.java` | 操作日志切面 |
| 切面 | `itsm-framework/.../aspectj/DataScopeAspect.java` | 数据权限切面 |
| 基类 | `itsm-common/.../core/domain/BaseEntity.java` | 实体基类 |
| 基类 | `itsm-common/.../core/controller/BaseController.java` | Controller基类 |

### 前端文件

| 分层 | 文件路径 | 说明 |
|------|----------|------|
| API | `itsm-ui/src/api/itsm/ticket.js` | 工单API（12个接口） |
| API | `itsm-ui/src/api/itsm/category.js` | 分类API（5个接口） |
| API | `itsm-ui/src/api/itsm/notification.js` | 通知API |
| 页面 | `itsm-ui/src/views/itsm/ticket/index.vue` | 工单列表页 |
| 页面 | `itsm-ui/src/views/itsm/ticket/detail.vue` | 工单详情页 |
| 页面 | `itsm-ui/src/views/itsm/category/index.vue` | 分类管理页 |

### 测试文件

| 文件路径 | 说明 |
|----------|------|
| `itsm-ticket/.../enums/TicketStatusTest.java` | 状态枚举测试 |
| `itsm-ticket/.../enums/TicketTransitionTest.java` | 转换规则测试 |
| `itsm-ticket/.../statemachine/TicketStateMachineTest.java` | 状态机测试 |
| `itsm-ticket/.../config/ItsmModuleConfigTest.java` | 模块配置测试 |
| `itsm-admin/.../controller/ItTicketControllerTest.java` | Controller测试 |
