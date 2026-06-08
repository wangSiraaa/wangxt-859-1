# 粮库熏蒸作业审批 API

粮库熏蒸作业全流程审批管理系统，支持作业票创建、审批、人员撤离确认、投药、通风检测、警戒解除等完整业务流程。

## 服务入口

- **基础 URL**: `http://localhost:18080`
- **API 前缀**: `/api/fumigation`
- **H2 控制台**: `http://localhost:18080/h2-console`
  - JDBC URL: `jdbc:h2:mem:fumigationdb`
  - 用户名: `sa`
  - 密码: (空)

## 测试账号

| 角色 | 用户ID | 用户名 | 说明 |
|------|--------|--------|------|
| 保管员 | K001 | 张三 | 创建作业票、提交审批 |
| 保管员 | K002 | 李四 | 创建作业票、提交审批 |
| 安全员 | S001 | 王安全员 | 确认人员撤离 |
| 安全员 | S002 | 赵安全员 | 确认人员撤离 |
| 作业负责人 | M001 | 李经理 | 审批作业票、投药、通风检测、解除警戒 |
| 作业负责人 | M002 | 王经理 | 审批作业票、投药、通风检测、解除警戒 |

## 业务流程

```
创建作业票(DRAFT) → 提交审批(SUBMITTED) → 审批通过(APPROVED)
     ↓                    ↓(驳回)                ↓
  草稿状态              REJECTED          确认人员撤离(EVACUATION_CONFIRMED)
                                                         ↓
                                                     投药(PESTICIDE_APPLIED)
                                                         ↓
                                         通风检测(通风中/检测合格)
                                                         ↓
                                               解除警戒(ALERT_LIFTED)
```

## API 列表

### 作业票管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/fumigation/ticket` | 创建作业票 |
| POST | `/api/fumigation/ticket/submit` | 提交作业票审批 |
| POST | `/api/fumigation/ticket/approve` | 审批作业票 |
| POST | `/api/fumigation/evacuation/confirm` | 确认人员撤离 |
| POST | `/api/fumigation/pesticide/apply` | 投药 |
| POST | `/api/fumigation/ventilation/detect` | 通风检测 |
| POST | `/api/fumigation/alert/lift` | 解除警戒 |
| GET | `/api/fumigation/ticket/{id}` | 根据ID查询作业票 |
| GET | `/api/fumigation/ticket/no/{ticketNo}` | 根据编号查询作业票 |
| GET | `/api/fumigation/tickets` | 查询所有作业票 |
| GET | `/api/fumigation/tickets/status/{status}` | 按状态查询作业票 |
| GET | `/api/fumigation/tickets/warehouse/{warehouseCode}` | 按仓房查询作业票 |
| POST | `/api/fumigation/ticket/cancel/{id}` | 取消作业票 |

### 状态订阅

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/fumigation/subscribe` | 订阅作业票状态变更 |
| POST | `/api/fumigation/unsubscribe/{operationId}` | 取消订阅 |
| GET | `/api/fumigation/subscriptions/subscriber/{subscriberId}` | 查询用户订阅列表 |
| GET | `/api/fumigation/subscriptions/operation/{operationId}` | 查询作业票订阅者 |
| GET | `/api/fumigation/todos` | 查询待办事项 |
| GET | `/api/fumigation/todos/subscribed/{subscriberId}` | 查询订阅的待办事项 |
| GET | `/api/fumigation/notifications` | 查询状态变更通知队列 |
| DELETE | `/api/fumigation/notifications` | 清空通知队列 |

### 审计日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/audit/logs/operation/{operationId}` | 查询作业票审计日志 |
| GET | `/api/audit/logs/ticket/{ticketNo}` | 按票号查询审计日志 |
| GET | `/api/audit/logs/operator/{operatorId}` | 按操作人查询审计日志 |
| GET | `/api/audit/logs` | 查询所有审计日志 |

## 核心验证规则

1. **人员未撤离不能投药**: 投药前必须确认人员已撤离，否则返回错误"人员未撤离不能投药"
2. **药剂用量限制**: 最大药剂用量 50，超量返回错误码 406
3. **毒气浓度限制**: 允许最大浓度 0.5，超标则通风检测不合格
4. **状态流转校验**: 每个操作都有严格的前置状态校验

## 启动方式

```bash
# 编译
mvn clean package

# 运行
java -jar target/fumigation-approval-1.0.0.jar

# 或使用 Maven
mvn spring-boot:run
```

## 验证脚本

```bash
# 运行验证脚本
./verify-859.sh
```
