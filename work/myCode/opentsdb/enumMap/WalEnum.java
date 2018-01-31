package com.sohu.opentsdb.enumMap;

public enum WalEnum {

    sncTimeNumOps("HBase.Wal.sncTimeNumOps"),
    syncTimeMin("HBase.Wal.syncTimeMin"),
    syncTimeMax("HBase.Wal.syncTimeMax"),
    syncTimeMean("HBase.Wal.syncTimeMean"),
    syncTimeMedian("HBase.Wal.syncTimeMedian"),
    syncTime75thPercentile("HBase.Wal.syncTime75thPercentile"),
    syncTime95thPercentile("HBase.Wal.syncTime95thPercentile"),
    syncTime99thPercentile("HBase.Wal.syncTime99thPercentile"),
    appendSizeNumOps("HBase.Wal.appendSizeNumOps"),
    appendSizeMin("HBase.Wal.appendSizeMin"),
    appendSizeMax("HBase.Wal.appendSizeMax"),
    appendSizeMean("HBase.Wal.appendSizeMean"),
    appendSizeMedian("HBase.Wal.appendSizeMedian"),
    appendSize75thPercentile("HBase.Wal.appendSize75thPercentile"),
    appendSize95thPercentile("HBase.Wal.appendSize95thPercentile"),
    appendSize99thPercentile("HBase.Wal.appendSize99thPercentile"),
    appendTimeNumOps("HBase.Wal.appendTimeNumOps"),
    appendTimeMin("HBase.Wal.appendTimeMin"),
    appendTimeMax("HBase.Wal.appendTimeMax"),
    appendTimeMean("HBase.Wal.appendTimeMean"),
    appendTimeMedian("HBase.Wal.appendTimeMedian"),
    appendTime75thPercentile("HBase.Wal.appendTime75thPercentile"),
    appendTime95thPercentile("HBase.Wal.appendTime95thPercentile"),
    appendTime99thPercentile("HBase.Wal.appendTime99thPercentile"),
    slowAppendCount("HBase.Wal.slowAppendCount"),
    appendCount("HBase.Wal.appendCount"),
    rollRequest("HBase.Wal.rollRequest"),
    lowReplicaRollRequest("HBase.Wal.lowReplicaRollRequest")
    ;

    String value;

    WalEnum(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }
}
