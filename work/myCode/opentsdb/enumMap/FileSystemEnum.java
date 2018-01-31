package com.sohu.opentsdb.enumMap;

public enum FileSystemEnum {

    hlogSplitTimeNumOps("Hbase.FileSystem.hlogSplitTimeNumOps"),
    hlogSplitTimeMin("Hbase.FileSystem.hlogSplitTimeMin"),
    hlogSplitTimeMax("Hbase.FileSystem.hlogSplitTimeMax"),
    hlogSplitTimeMean("Hbase.FileSystem.hlogSplitTimeMean"),
    hlogSplitTimeMedian("Hbase.FileSystem.hlogSplitTimeMedian"),
    hlogSplitTime75thPercentile("Hbase.FileSystem.hlogSplitTime75thPercentile"),
    hlogSplitTime95thPercentile("Hbase.FileSystem.hlogSplitTime95thPercentile"),
    hlogSplitTime99thPercentile("Hbase.FileSystem.hlogSplitTime99thPercentile"),
    metaHlogSplitTimeNumOps("Hbase.FileSystem.metaHlogSplitTimeNumOps"),
    metaHlogSplitTimeMin("Hbase.FileSystem.metaHlogSplitTimeMin"),
    metaHlogSplitTimeMax("Hbase.FileSystem.metaHlogSplitTimeMax"),
    metaHlogSplitTimeMean("Hbase.FileSystem.metaHlogSplitTimeMean"),
    metaHlogSplitTimeMedian("Hbase.FileSystem.metaHlogSplitTimeMedian"),
    metaHlogSplitTime75thPercentile("Hbase.FileSystem.metaHlogSplitTime75thPercentile"),
    metaHlogSplitTime95thPercentile("Hbase.FileSystem.metaHlogSplitTime95thPercentile"),
    metaHlogSplitTime99thPercentile("Hbase.FileSystem.metaHlogSplitTime99thPercentile"),
    metaHlogSplitSizeNumOps("Hbase.FileSystem.metaHlogSplitSizeNumOps"),
    metaHlogSplitSizeMin("Hbase.FileSystem.metaHlogSplitSizeMin"),
    metaHlogSplitSizeMax("Hbase.FileSystem.metaHlogSplitSizeMax"),
    metaHlogSplitSizeMean("Hbase.FileSystem.metaHlogSplitSizeMean"),
    metaHlogSplitSizeMedian("Hbase.FileSystem.metaHlogSplitSizeMedian"),
    metaHlogSplitSize75thPercentile("Hbase.FileSystem.metaHlogSplitSize75thPercentile"),
    metaHlogSplitSize95thPercentile("Hbase.FileSystem.metaHlogSplitSize95thPercentile"),
    metaHlogSplitSize99thPercentile("Hbase.FileSystem.metaHlogSplitSize99thPercentile"),
    hlogSplitSizeNumOps("Hbase.FileSystem.hlogSplitSizeNumOps"),
    hlogSplitSizeMin("Hbase.FileSystem.hlogSplitSizeMin"),
    hlogSplitSizeMax("Hbase.FileSystem.hlogSplitSizeMax"),
    hlogSplitSizeMean("Hbase.FileSystem.hlogSplitSizeMean"),
    hlogSplitSizeMedian("Hbase.FileSystem.hlogSplitSizeMedian"),
    hlogSplitSize75thPercentile("Hbase.FileSystem.hlogSplitSize75thPercentile"),
    hlogSplitSize95thPercentile("Hbase.FileSystem.hlogSplitSize95thPercentile"),
    hlogSplitSize99thPercentile("Hbase.FileSystem.hlogSplitSize99thPercentile")
    ;

    String value;

    FileSystemEnum(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }
}
