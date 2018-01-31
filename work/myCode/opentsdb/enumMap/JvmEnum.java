package com.sohu.opentsdb.enumMap;

public enum JvmEnum {
    memNonHeapUsedM("HBase.JvmMetrics.memNonHeapUsedM"),
    memNonHeapCommittedM("HBase.JvmMetrics.memNonHeapCommittedM"),
    memHeapUsedM("HBase.JvmMetrics.memHeapUsedM"),
    memHeapCommittedM("HBase.JvmMetrics.memHeapCommittedM"),
    memMaxM("HBase.JvmMetrics.memMaxM"),
    gcCountParNew("HBase.JvmMetrics.gcCountParNew"),
    gcTimeMillisParNew("HBase.JvmMetrics.gcTimeMillisParNew"),
    gcCountConcurrentMarkSweep("HBase.JvmMetrics.gcCountConcurrentMarkSweep"),
    gcTimeMillisConcurrentMarkSweep("HBase.JvmMetrics.gcTimeMillisConcurrentMarkSweep"),
    gcCount("HBase.JvmMetrics.gcCount"),
    gcTimeMillis("HBase.JvmMetrics.gcTimeMillis"),
    threadsNew("HBase.JvmMetrics.threadsNew"),
    threadsRunnable("HBase.JvmMetrics.threadsRunnable"),
    threadsBlocked("HBase.JvmMetrics.threadsBlocked"),
    threadsWaiting("HBase.JvmMetrics.threadWaiting"),
    threadsTimedWaiting("HBase.JvmMetrics.threadsTimedWaiting"),
    threadsTerminated("HBase.JvmMetrics.threadsTerminated"),
    logFatal("HBase.JvmMetrics.logFatal"),
    logError("HBase.JvmMetrics.logError"),
    logWarn("HBase.JvmMetrics.logWarn"),
    logInfo("HBase.JvmMetrics.logInfo"),
    memNonHeapMaxM("HBase.JvmMetrics.memNonHeapMaxM"),
    memHeapMaxM("HBase.JvmMetrics.memHeapMaxM")
    ;


    String value;
    JvmEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }

}
