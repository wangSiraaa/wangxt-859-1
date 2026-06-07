package com.grain.fumigation.enums;

public enum OperationRole {
    KEEPER("保管员"),
    SAFETY_OFFICER("安全员"),
    OPERATION_MANAGER("作业负责人");

    private final String description;

    OperationRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
