package com.sohu.opentsdb;

import com.google.common.collect.Maps;
import com.sohu.hbase.HbaseClusterDao;
import com.sohu.hbase.HbaseReader;
import com.sohu.jdbc.DaoPaser.DaoImplProxy;
import com.sohu.json.bean.hbase.UserTable;
import com.sohu.mr.Data;
import com.sohu.opentsdb.Utils.DateUtil;
import com.sohu.opentsdb.Utils.HbaseUtil;
import com.sohu.opentsdb.hbase.HbaseClusterInfo;
import com.sohu.opentsdb.perHour.HbaseClusterInfoPerhour;
import org.apache.hadoop.hbase.ClusterStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * created by tangyuwen
 * date 2018/01/10
 */
public class OpentsdbInsertTableInfo {
    private HbaseClusterInfo clusterInfo;
    private String protocol;
    private String url;
    private String port;
    private ClusterStatus clusterStatus;
    private HbaseUtil hbaseUtil;
    private HbaseClusterInfoPerhour hbaseClusterInfoPerhour;
    private Map<String,String> tableMap;
    private String opentsdb_url;
    private Integer threadCount;
    private final String COLUMN_FAMILY1 = "tableName";
    public Logger logger = LoggerFactory.getLogger(getClass());

    public OpentsdbInsertTableInfo(HbaseClusterInfoPerhour clusterInfo, String protocol, String url, String port, HbaseUtil hbaseUtil,Integer threadCount){
        super();
        this.threadCount = threadCount;
        this.hbaseClusterInfoPerhour=clusterInfo;
        this.protocol = protocol;
        this.url = url;
        this.port = port;
        this.hbaseUtil = hbaseUtil;
    }

    public OpentsdbInsertTableInfo(HbaseClusterInfo clusterInfo,String protocol,String url,String port,ClusterStatus clusterStatus,String opentsdb_url,Integer threadCount){
        super();
        this.threadCount = threadCount;
        this.opentsdb_url = opentsdb_url;
        this.clusterInfo = clusterInfo;
        this.protocol = protocol;
        this.url = url;
        this.port = port;
        this.clusterStatus = clusterStatus;
    }

    public void saveHbaseClusterInfo() throws Exception{
        System.out.println("saveHbaseClusterInfo,protocol="+protocol+",url="+url+",port="+port);
        logger.info("saveHbaseClusterInfo,protocol="+protocol+",url="+url+",port="+port);
        clusterInfo.getHbaseClusterInfo(protocol,url,port,clusterStatus,opentsdb_url,threadCount);
    }

    public void saveHbaseClusterInfoPerhour() throws Exception{
        try{
            Long insert_time = DateUtil.currentHourInMills();

            System.out.println("SaveHbaseTableNameInfo,protocol="+protocol+",url="+url+",port="+port);
            Set<String> set =hbaseClusterInfoPerhour.getHbaseClusterInfoPerhour(protocol,url,port,hbaseUtil,insert_time);
            System.out.println("一共有"+ set.size() +"张表");
            //key为namespace value为表名
            tableMap=Maps.newHashMap();

            Iterator<String> iterator = set.iterator();
            String nameSpace;
            String tableName;
            String appender;
            while(iterator.hasNext()){
                String fullTableName=iterator.next();
                nameSpace=fullTableName.replaceAll("_table_.*","");
                tableName=fullTableName.replaceAll("_table_",",").split(",")[1];
                if(tableMap.containsKey(nameSpace)){
                    appender=tableMap.get(nameSpace);
                    appender=appender+","+tableName;
                    tableMap.put(nameSpace,appender);
                }else{
                    tableMap.put(nameSpace,tableName);
                }
            }

            String insert_time_str =insert_time.toString();
            int hashSalt = insert_time_str.substring(insert_time_str.length()-2,insert_time_str.length()).hashCode();
            String rowKey =hashSalt + ":" + (Long.MAX_VALUE-insert_time);

            ArrayList<Data> row = new ArrayList<Data>();
            row.add(new Data("rowkey",rowKey,rowKey));
            Iterator<Map.Entry<String,String>> it= tableMap.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String,String> entry = it.next();
                row.add(new Data(COLUMN_FAMILY1,entry.getKey(),entry.getValue()));
            }
            hbaseUtil.insertRow(row);
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

}
