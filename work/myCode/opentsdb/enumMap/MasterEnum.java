package com.sohu.opentsdb.enumMap;

public enum MasterEnum {
    tagliveRegionServers("liveRegionServers"),
    tagDeadRegionServers("deadRegionServers"),
    tagZooKeeperQuorm("zookeeperQuorm"),
    masterStartTime("HBase.Server.masterStartTime"),
    masterActiveTime("HBase.Server.masterActiveTime"),
    numRegionServers("HBase.Server.numRegionServers"),
    numDeadRegionServers("HBase.Server.numDeadRegionServers"),
    tagIsActiveMaster("HBase.Server.tagIsActiveMaster"),
    clusterRequests("HBase.Server.clusterRequests"),
    averageLoad("HBase.Server.averageLoad");

    String value;

    MasterEnum(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }
}
