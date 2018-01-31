package com.sohu.opentsdb.enumMap;

public enum IpcEnum {
    queueSize("HBase.Ipc.queueSize"),
    numCallsInGeneralQueue("HBase.Ipc.numCallsInGeneralQueue"),
    numCallsInReplicationQueue("HBase.Ipc.numCallsInReplicationQueue"),
    numCallsInPriorityQueue("HBase.Ipc.numCallsInPriorityQueue"),
    numOpenConnections("HBase.Ipc.numOpenConnections"),
    queueCallTimeNumOps("HBase.Ipc.queueCallTimeNumOps"),
    queueCallTimeMin("HBase.Ipc.queueCallTimeMin"),
    queueCallTimeMax("HBase.Ipc.queueCallTimeMax"),
    queueCallTimeMean("HBase.Ipc.queueCallTimeMean"),
    queueCallTimeMedian("HBase.Ipc.queueCallTimeMedian"),
    queueCallTime75thPercentile("HBase.Ipc.queueCallTime75thPercentile"),
    queueCallTime95thPercentile("HBase.Ipc.queueCallTime95thPercentile"),
    queueCallTime99thPercentile("HBase.Ipc.queueCallTime99thPercentile"),
    authenticationFailures("HBase.Ipc.authenticationFailures"),
    authorizationFailures("HBase.Ipc.authorizationFailures"),
    authenticationSuccesses("HBase.Ipc.authenticationSuccesses"),
    authorizationSuccesses("HBase.Ipc.authorizationSuccesses"),
    processCallTimeNumOps("HBase.Ipc.processCallTimeNumOps"),
    processCallTimeMin("HBase.Ipc.processCallTimeMin"),
    processCallTimeMax("HBase.Ipc.processCallTimeMax"),
    processCallTimeMean("HBase.Ipc.processCallTimeMean"),
    processCallTimeMedian("HBase.Ipc.processCallTimeMedian"),
    processCallTime75thPercentile("HBase.Ipc.processCallTime75thPercentile"),
    processCallTime95thPercentile("HBase.Ipc.processCallTime95thPercentile"),
    processCallTime99thPercentile("HBase.Ipc.processCallTime99thPercentile"),
    sentBytes("HBase.Ipc.sentBytes"),
    receivedBytes("HBase.Ipc.receivedBytes"),
    authenticationFallbacks("HBase.Ipc.authenticationFallbacks"),
    numActiveHandler("HBase.Ipc.numActiveHandler"),
    requestSizeNumOps("HBase.Ipc.requestSizeNumOps"),
    requestSizeMin("HBase.Ipc.requestSizeMin"),
    requestSizeMax("HBase.Ipc.requestSizeMax"),
    requestSizeMean("HBase.Ipc.requestSizeMean"),
    requestSizeMedian("HBase.Ipc.requestSizeMedian"),
    requestSize75thPercentile("HBase.Ipc.requestSize75thPercentile"),
    requestSize95thPercentile("HBase.Ipc.requestSize95thPercentile"),
    requestSize99thPercentile("HBase.Ipc.requestSize99thPercentile"),
    responseSizeNumOps("HBase.Ipc.responseSizeNumOps"),
    responseSizeMin("HBase.Ipc.responseSizeMin"),
    responseSizeMax("HBase.Ipc.responseSizeMax"),
    responseSizeMean("HBase.Ipc.responseSizeMean"),
    responseSizeMedian("HBase.Ipc.responseSizeMedian"),
    responseSize75thPercentile("HBase.Ipc.responseSize75thPercentile"),
    responseSize95thPercentile("HBase.Ipc.responseSize95thPercentile"),
    responseSize99thPercentile("HBase.Ipc.responseSize99thPercentile"),
    totalCallTimeNumOps("HBase.Ipc.totalCallTimeNumOps"),
    totalCallTimeMin("HBase.Ipc.totalCallTimeMin"),
    totalCallTimeMax("HBase.Ipc.totalCallTimeMax"),
    totalCallTimeMean("HBase.Ipc.totalCallTimeMean"),
    totalCallTimeMedian("HBase.Ipc.totalCallTimeMedian"),
    totalCallTime75thPercentile("HBase.Ipc.totalCallTime75thPercentile"),
    totalCallTime95thPercentile("HBase.Ipc.totalCallTime95thPercentile"),
    totalCallTime99thPercentile("HBase.Ipc.totalCallTime99thPercentile")

    ;


    String value;

    IpcEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
