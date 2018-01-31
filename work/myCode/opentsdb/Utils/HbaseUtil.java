package com.sohu.opentsdb.Utils;

import com.sohu.mr.Data;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class HbaseUtil {
    String tableName;
    Configuration conf;
    public final Logger logger = LoggerFactory.getLogger(getClass());

    public HbaseUtil(String tableName,Configuration conf){
        this.tableName=tableName;
        this.conf = conf;
    }

    public void insertRow(ArrayList<Data> row) throws Exception{
        System.out.println("HBASE_TABLE_NAME表开始写数据了");
        HTable table=new HTable(conf,tableName);
        Put put = new Put(Bytes.toBytes(row.get(0).getColumnKey()));
        for(Data data:row){
            if(data.getColumnFamily().equals("rowkey"))
                continue;
            put.add(Bytes.toBytes(data.getColumnFamily()),Bytes.toBytes(data.getColumnKey()),Bytes.toBytes(data.getColumnValue()));
        }
        table.put(put);
        table.close();
        System.out.println("HBASE_TABLE_NAME表写完数据了");
    }
}
