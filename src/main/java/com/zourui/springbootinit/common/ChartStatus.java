package com.zourui.springbootinit.common;

public enum ChartStatus {
    WAIT("wait"),
    RUNNING("running"),
    SUCCEED("succeed"),
    FAILED("failed");

    private String value;

    ChartStatus(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}