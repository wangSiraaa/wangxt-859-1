package com.grain.fumigation.dto;

import com.grain.fumigation.enums.OperationRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SubscribeRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    @NotNull(message = "订阅人角色不能为空")
    private OperationRole subscriberRole;

    @NotBlank(message = "订阅人ID不能为空")
    private String subscriberId;

    private String subscriberName;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public OperationRole getSubscriberRole() {
        return subscriberRole;
    }

    public void setSubscriberRole(OperationRole subscriberRole) {
        this.subscriberRole = subscriberRole;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }
}
