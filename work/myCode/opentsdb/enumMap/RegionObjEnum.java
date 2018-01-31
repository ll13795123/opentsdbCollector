package com.sohu.opentsdb.enumMap;

public enum RegionObjEnum {
    tableName("tableName"),
    regionNum("Hbase.regionNum"),
    storeCount("Hbase.Region.storeCount"),
    storeFileCount("Hbase.Region.storeFileCount"),
    memStoreSize("Hbase.Region.memStoreSize"),
    storeFileSize("Hbase.Region.storeFileSize"),
    compactionsCompletedCount("Hbase.Region.compactionsCompletedCount"),
    numBytesCompactedCount("Hbase.Region.numBytesCompactedCount"),
    numFilesCompactedCount("Hbase.Region.numFilesCompactedCount"),
    getNumOps("Hbase.Region.getNumOps"),
    getMin("Hbase.Region.getMin"),
    getMax("Hbase.Region.getMax"),
    getMean("Hbase.Region.getMean"),
    getMedian("Hbase.Region.getMedian"),
    get75thPercentile("Hbase.Region.get75thPercentile"),
    get95thPercentile("Hbase.Region.get95thPercentile"),
    get99thPercentile("Hbase.Region.get99thPercentile"),
    scanNextNumOps("Hbase.Region.scanNextNumOps"),
    scanNextMin("Hbase.Region.scanNextMin"),
    scanNextMax("Hbase.Region.scanNextMax"),
    scanNextMean("Hbase.Region.scanNextMean"),
    scanNextMedian("Hbase.Region.scanNextMedian"),
    scanNext75thPercentile("Hbase.Region.scanNext75thPercentile"),
    scanNext95thPercentile("Hbase.Region.scanNext95thPercentile"),
    scanNext99thPercentile("Hbase.Region.scanNext99thPercentile"),
    deleteCount("Hbase.Region.deleteCount"),
    appendCount("Hbase.Region.appendCount"),
    mutateCount("Hbase.Region.mutateCount"),
    incrementCount("Hbase.Region.incrementCount"),
    requestCount("Hbase.Region.requestCount"),
    serverName("Hbase.Region.serverName")
    ;
    String value;
    RegionObjEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
