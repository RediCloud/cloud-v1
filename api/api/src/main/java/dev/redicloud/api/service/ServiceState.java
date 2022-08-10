package dev.redicloud.api.service;

public enum ServiceState {

    PREPARE,
    STARTING,
    RUNNING_UNDEFINED,
    RUNNING_DEFINED,
    STOPPING,
    UNKNOWN,
    OFFLINE;

}
