package com.grain.fumigation.service;

import com.grain.fumigation.dto.SubscribeRequest;
import com.grain.fumigation.dto.TodoItemVO;
import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.entity.StatusSubscription;
import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;
import com.grain.fumigation.event.StatusChangedEvent;
import com.grain.fumigation.exception.BusinessException;
import com.grain.fumigation.repository.FumigationOperationRepository;
import com.grain.fumigation.repository.StatusSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StatusSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(StatusSubscriptionService.class);

    @Autowired
    private StatusSubscriptionRepository subscriptionRepository;

    @Autowired
    private FumigationOperationRepository operationRepository;

    private final List<Map<String, Object>> notificationQueue = new CopyOnWriteArrayList<>();

    private static final Map<OperationStatus, OperationRole> STATUS_REQUIRED_ROLE_MAP = new HashMap<>();
    private static final Map<OperationStatus, String> STATUS_ACTION_MAP = new HashMap<>();

    static {
        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.SUBMITTED, OperationRole.OPERATION_MANAGER);
        STATUS_ACTION_MAP.put(OperationStatus.SUBMITTED, "待审批");

        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.APPROVED, OperationRole.SAFETY_OFFICER);
        STATUS_ACTION_MAP.put(OperationStatus.APPROVED, "待确认人员撤离");

        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.EVACUATION_CONFIRMED, OperationRole.OPERATION_MANAGER);
        STATUS_ACTION_MAP.put(OperationStatus.EVACUATION_CONFIRMED, "待投药");

        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.PESTICIDE_APPLIED, OperationRole.OPERATION_MANAGER);
        STATUS_ACTION_MAP.put(OperationStatus.PESTICIDE_APPLIED, "待通风检测");

        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.VENTILATION_IN_PROGRESS, OperationRole.OPERATION_MANAGER);
        STATUS_ACTION_MAP.put(OperationStatus.VENTILATION_IN_PROGRESS, "待重新通风检测");

        STATUS_REQUIRED_ROLE_MAP.put(OperationStatus.VENTILATION_COMPLETED, OperationRole.OPERATION_MANAGER);
        STATUS_ACTION_MAP.put(OperationStatus.VENTILATION_COMPLETED, "待解除警戒");
    }

    @Transactional
    public StatusSubscription subscribe(SubscribeRequest request) {
        FumigationOperation operation = operationRepository.findById(request.getOperationId())
                .orElseThrow(() -> new BusinessException("作业票不存在，ID:" + request.getOperationId()));

        if (subscriptionRepository.existsByOperationIdAndSubscriberIdAndActiveTrue(
                request.getOperationId(), request.getSubscriberId())) {
            throw new BusinessException("已订阅该作业票状态，无需重复订阅");
        }

        StatusSubscription subscription = new StatusSubscription();
        subscription.setOperationId(request.getOperationId());
        subscription.setTicketNo(operation.getTicketNo());
        subscription.setSubscriberRole(request.getSubscriberRole());
        subscription.setSubscriberId(request.getSubscriberId());
        subscription.setSubscriberName(request.getSubscriberName());
        subscription.setActive(true);

        StatusSubscription saved = subscriptionRepository.save(subscription);
        log.info("订阅状态成功: operationId={}, subscriberId={}", request.getOperationId(), request.getSubscriberId());
        return saved;
    }

    @Transactional
    public void unsubscribe(Long operationId, String subscriberId) {
        StatusSubscription subscription = subscriptionRepository
                .findByOperationIdAndSubscriberIdAndActiveTrue(operationId, subscriberId)
                .orElseThrow(() -> new BusinessException("未找到有效的订阅记录"));

        subscription.setActive(false);
        subscriptionRepository.save(subscription);
        log.info("取消订阅成功: operationId={}, subscriberId={}", operationId, subscriberId);
    }

    public List<StatusSubscription> getSubscriptionsBySubscriberId(String subscriberId) {
        return subscriptionRepository.findBySubscriberIdAndActiveTrue(subscriberId);
    }

    public List<StatusSubscription> getSubscriptionsByOperationId(Long operationId) {
        return subscriptionRepository.findByOperationIdAndActiveTrue(operationId);
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStatusChanged(StatusChangedEvent event) {
        Long operationId = event.getOperation().getId();
        List<StatusSubscription> subscriptions = subscriptionRepository.findByOperationIdAndActiveTrue(operationId);

        for (StatusSubscription subscription : subscriptions) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("operationId", operationId);
            notification.put("ticketNo", event.getOperation().getTicketNo());
            notification.put("beforeStatus", event.getBeforeStatus() != null ? event.getBeforeStatus().name() : null);
            notification.put("afterStatus", event.getAfterStatus() != null ? event.getAfterStatus().name() : null);
            notification.put("operationType", event.getOperationType() != null ? event.getOperationType().name() : null);
            notification.put("success", event.isSuccess());
            notification.put("failureReason", event.getFailureReason());
            notification.put("subscriberId", subscription.getSubscriberId());
            notification.put("subscriberRole", subscription.getSubscriberRole() != null ? subscription.getSubscriberRole().name() : null);
            notification.put("timestamp", System.currentTimeMillis());

            notificationQueue.add(notification);
            log.info("状态变更通知已入队: operationId={}, subscriberId={}, status={}",
                    operationId, subscription.getSubscriberId(), event.getAfterStatus());
        }
    }

    public List<Map<String, Object>> getNotificationQueue() {
        return new ArrayList<>(notificationQueue);
    }

    public void clearNotificationQueue() {
        notificationQueue.clear();
    }

    public List<TodoItemVO> getTodoList(String operatorId, OperationRole role) {
        List<TodoItemVO> todoList = new ArrayList<>();

        List<OperationStatus> relevantStatuses = new ArrayList<>();
        for (Map.Entry<OperationStatus, OperationRole> entry : STATUS_REQUIRED_ROLE_MAP.entrySet()) {
            if (entry.getValue() == role) {
                relevantStatuses.add(entry.getKey());
            }
        }

        for (OperationStatus status : relevantStatuses) {
            List<FumigationOperation> operations = operationRepository.findByStatus(status);
            for (FumigationOperation operation : operations) {
                if (isRelevantTodo(operation, operatorId, role)) {
                    TodoItemVO todo = convertToTodoItem(operation, role);
                    todoList.add(todo);
                }
            }
        }

        todoList.sort((a, b) -> {
            if (a.getUpdateTime() == null && b.getUpdateTime() == null) return 0;
            if (a.getUpdateTime() == null) return 1;
            if (b.getUpdateTime() == null) return -1;
            return b.getUpdateTime().compareTo(a.getUpdateTime());
        });

        return todoList;
    }

    private boolean isRelevantTodo(FumigationOperation operation, String operatorId, OperationRole role) {
        switch (role) {
            case KEEPER:
                return operatorId.equals(operation.getKeeperId());
            case SAFETY_OFFICER:
                return operatorId.equals(operation.getSafetyOfficerId())
                        || operation.getSafetyOfficerId() == null;
            case OPERATION_MANAGER:
                return operatorId.equals(operation.getOperationManagerId())
                        || operation.getOperationManagerId() == null;
            default:
                return false;
        }
    }

    private TodoItemVO convertToTodoItem(FumigationOperation operation, OperationRole role) {
        TodoItemVO todo = new TodoItemVO();
        todo.setOperationId(operation.getId());
        todo.setTicketNo(operation.getTicketNo());
        todo.setWarehouseCode(operation.getWarehouseCode());
        todo.setWarehouseName(operation.getWarehouseName());
        todo.setGrainType(operation.getGrainType());
        todo.setStatus(operation.getStatus());
        todo.setStatusDescription(operation.getStatus().getDescription());
        todo.setRequiredRole(STATUS_REQUIRED_ROLE_MAP.get(operation.getStatus()));
        todo.setRequiredRoleDescription(STATUS_REQUIRED_ROLE_MAP.get(operation.getStatus()) != null
                ? STATUS_REQUIRED_ROLE_MAP.get(operation.getStatus()).getDescription() : null);
        todo.setActionName(STATUS_ACTION_MAP.get(operation.getStatus()));
        todo.setCreateTime(operation.getCreateTime());
        todo.setUpdateTime(operation.getUpdateTime());
        return todo;
    }

    public List<TodoItemVO> getSubscribedTodoList(String subscriberId) {
        List<StatusSubscription> subscriptions = getSubscriptionsBySubscriberId(subscriberId);
        List<TodoItemVO> todoList = new ArrayList<>();

        for (StatusSubscription subscription : subscriptions) {
            FumigationOperation operation = operationRepository.findById(subscription.getOperationId()).orElse(null);
            if (operation != null && isTodoStatus(operation.getStatus())) {
                TodoItemVO todo = convertToTodoItem(operation, subscription.getSubscriberRole());
                todoList.add(todo);
            }
        }

        return todoList;
    }

    private boolean isTodoStatus(OperationStatus status) {
        return STATUS_REQUIRED_ROLE_MAP.containsKey(status);
    }
}
