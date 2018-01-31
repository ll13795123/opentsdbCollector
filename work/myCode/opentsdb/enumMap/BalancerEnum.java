package com.sohu.opentsdb.enumMap;

public enum BalancerEnum {
    balancerClusterNumOps("HBase.Balancer.balancerClusterNumOps"),

    balancerClusterMin("HBase.Balancer.balancerClusterMin"),

    balancerClusterMax("HBase.Balancer.balancerClusterMax"),

    balancerClusterMean("HBase.Balancer.balancerClusterMean"),

    balancerClusterMedian("HBase.Balancer.balancerClusterMedian"),

    balancerCluster75thPercentile("HBase.Balancer.balancerCluster75thPercentile"),

    balancerCluster95thPercentile("HBase.Balancer.balancerCluster95thPercentile"),

    balancerCluster99thPercentile("HBase.Balancer.balancerCluster99thPercentile"),

    miscInvocationCount("HBase.Balancer.miscInvocationCount");

    String value;

    BalancerEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
