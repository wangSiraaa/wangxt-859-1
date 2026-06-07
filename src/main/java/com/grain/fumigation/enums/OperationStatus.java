package com.grain.fumigation.enums;

public enum OperationStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交待审批"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    EVACUATION_CONFIRMED("人员撤离已确认"),
    PESTICIDE_APPLIED("已投药"),
    VENTILATION_IN_PROGRESS("通风中"),
    VENTILATION_COMPLETED("通风检测合格"),
    ALERT_LIFTED("警戒已解除"),
    CANCELLED("已取消");

    private final String description;

    OperationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
