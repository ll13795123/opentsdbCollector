package com.sohu.opentsdb.enumMap;

public enum ThreadsEnum {
    daemonThreadCount("JVM.Threading.DeamonThreadCount"),

    peakThreadCount("JVM.Threading.DeamonThreadCount"),

    currentThreadCpuTime("JVM.Threading.currentThreadCpuTime"),

    currentThreadUserTime("JVM.Threading.currentThreadUserTime"),

    threadCount("JVM.Threading.threadCount"),

    totalStartedThreadCount("JVM.Threading.totalStartedThreadCount");

    String value;

    ThreadsEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
