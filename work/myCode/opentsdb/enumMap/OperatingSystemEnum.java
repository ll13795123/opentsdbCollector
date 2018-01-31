package com.sohu.opentsdb.enumMap;

public enum OperatingSystemEnum {

    tagHostName("tagHostName"),
    createTime("createTime"),
    tagContext("tagContext"),
    maxFileDescriptorCount("Jvm.OperatingSystem.maxFileDescriptorCount"),
    openFileDescriptorCount("Jvm.OperatingSystem.openFileDescriptorCount"),
    processCpuTime("Jvm.OperatingSystem.processCpuTime"),
    systemCpuLoad("Jvm.OperatingSystem.systemCpuLoad"),
    totalPhysicalMemorySize("Jvm.OperatingSystem.totalPhysicalMemorySize"),
    freePhysicalMemorySize("Jvm.OperatingSystem.freePhysicalMemorySize"),
    committedVirtualMemorySize("Jvm.OperatingSystem.committedVirtualMemorySize"),
    freeSwapSpaceSize("Jvm.OperatingSystem.freeSwapSpaceSize"),
    processCpuLoad("Jvm.OperatingSystem.processCpuLoad"),
    totalSwapSpaceSize("Jvm.OperatingSystem.totalSwapSpaceSize"),
    systemLoadAverage("Jvm.OperatingSystem.systemLoadAverage"),
    availableProcessors("Jvm.OperatingSystem.availableProcessors"),
    version("Jvm.OperatingSystem.version")
    ;

    String value;

    OperatingSystemEnum(String value) {
        this.value=value;
    }

    public String getValue(){
        return value;
    }
}
