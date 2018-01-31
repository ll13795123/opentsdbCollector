package com.sohu.opentsdb.enumMap;

public enum AssignManagerEnum {
    ritOldestAge("HBase.AssignManager.ritOldestAge"),
    ritCount("HBase.AssignManager.ritCount"),
    bulkAssignNumOps("HBase.AssignManager.bulkAssignNumOps"),
    bulkAssignMin("HBase.AssignManager.bulkAssignMin"),
    bulkAssignMax("HBase.AssignManager.bulkAssignMax"),
    bulkAssignMean("HBase.AssignManager.bulkAssignMean"),
    bulkAssignMedian("HBase.AssignManager.bulkAssignMedian"),
    bulkAssign75thPercentile("HBase.AssignManager.bulkAssign75thPercentile"),
    bulkAssign95thPercentile("HBase.AssignManager.bulkAssign95thPercentile"),
    bulkAssign99thPercentile("HBase.AssignManager.bulkAssign99thPercentile"),
    ritCountOverThreshold("HBase.AssignManager.ritCountOverThreshold"),
    assignNumOps("HBase.AssignManager.assignNumOps"),
    assignMin("HBase.AssignManager.assignMin"),
    assignMax("HBase.AssignManager.assignMax"),
    assignMean("HBase.AssignManager.assignMean"),
    assignMedian("HBase.AssignManager.assignMedian"),
    assign75thPercentile("HBase.AssignManager.assign75thPercentile"),
    assign95thPercentile("HBase.AssignManager.assign95thPercentile"),
    assign99thPercentile("HBase.AssignManager.assign99thPercentile")
    ;

    String value;

    AssignManagerEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
