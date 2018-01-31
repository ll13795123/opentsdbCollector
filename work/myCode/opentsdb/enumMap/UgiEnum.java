package com.sohu.opentsdb.enumMap;

public enum UgiEnum {
    loginSuccessNumOps("HBase.Ugi.loginSuccessNumOps"),
    loginSuccessAvgTime("HBase.Ugi.loginSuccessAvgTime"),
    loginFailureNumOps("HBase.Ugi.loginFailureNumOps"),
    loginFailureAvgTime("HBase.Ugi.loginFailureAvgTime"),
    getGroupsNumOps("HBase.Ugi.getGroupsNumOps"),
    getGroupsAvgTime("HBase.Ugi.getGroupsAvgTime")
    ;

    String value;

    UgiEnum(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }
}
