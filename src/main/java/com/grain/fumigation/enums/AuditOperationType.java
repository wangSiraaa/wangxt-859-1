package com.grain.fumigation.enums;

public enum AuditOperationType {
    CREATE_TICKET("创建作业票"),
    SUBMIT_TICKET("提交作业票"),
    APPROVE_TICKET("审批作业票"),
    REJECT_TICKET("驳回作业票"),
    CONFIRM_EVACUATION("确认人员撤离"),
    APPLY_PESTICIDE("投药"),
    START_VENTILATION("开始通风"),
    COMPLETE_VENTILATION("完成通风检测"),
    LIFT_ALERT("解除警戒"),
    CANCEL_TICKET("取消作业票");

    private final String description;

    AuditOperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
