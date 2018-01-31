package com.sohu.opentsdb;

import com.sohu.jdbc.DbcpConnection;
import com.sohu.opentsdb.Utils.HbaseUtil;
import com.sohu.opentsdb.hbase.HbaseClusterInfo;
import com.sohu.opentsdb.perHour.HbaseClusterInfoPerhour;
import com.sohu.util.PropertyUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.zookeeper.MasterAddressTracker;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * created by tangyuwen
 * date: 2018/01/10
 */
public class OpentsdbMain {

    private static String HMASTER_JMX_PORT =":60010/jmx";
    private static String HMASTER_URL;
    private static String HTTP_PROTOCOL = "http://";
    private static final String HBASE_ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";
    private static final String HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT = "hbase.zookeeper.property.clientPort";
    private static final String ZOOKEEPER_ZNODE_PARENT = "zookeeper.znode.parent";
    private static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";
    private static final String HBASE_MASTER_KERBEROS_PRINCIPAL = "hbase.master.kerberos.principal";
    private static final String HBASE_REGIONSERVER_KERBEROS_PRINCIPAL = "hbase.regionserver.kerberos.principal";
    private static final String HBASE_SECURITY_AUTHENTICATION = "hbase.security.authentication";
    private static String THREAD_COUNT;
    private static String TABLE_NAME="";
    private static String OPENTSDB_SERVER="";
    private static Configuration conf;
    public static Logger logger = LoggerFactory.getLogger(OpentsdbMain.class);

    public static void main(String[] args) throws Exception{
        logger.info(OpentsdbMain.class.getName());
        if(args == null || args.length == 0){
            System.out.println("error main args,must special conf path and type");
            return;
        }
        PropertyUtil.load(args[0]);
        TABLE_NAME = PropertyUtil.getProperty("HBASE_TABLE_NAME");
        OPENTSDB_SERVER = PropertyUtil.getProperty("OPENTSDB_SERVER_URL");
        THREAD_COUNT = PropertyUtil.getProperty("THREAD_COUNT");
        System.out.println("-------opentsdb url-----------");
        System.out.println(OPENTSDB_SERVER);
        conf = loadInitInfo();
        int type =Integer.parseInt(args[1].split(":")[1]);
        System.out.println("type="+type);
        if(conf==null){
            System.out.println("conf is not load correctly");
            return;
        }else{
            if(type==1){
                System.out.println("---------executeJmxInsert begin---------------");
                executeJmxInsert(conf);
            }else if(type==2){
                System.out.println("---------executeJmxInsertPerhour begin--------------");
                executeJmxInsertPerhour(conf);
            }
        }

    }

    public static Configuration loadInitInfo() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        String zk = PropertyUtil.getProperty("ZK_ADDRESS");
        String zkClientPort = PropertyUtil.getProperty("ZK_PORT");
        String zkParent = PropertyUtil.getProperty("ZK_PARENT");
        conf.set(HBASE_ZOOKEEPER_QUORUM,zk);
        conf.set(HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT,zkClientPort);
        conf.set(ZOOKEEPER_ZNODE_PARENT,zkParent);
        conf.set(HBASE_ZOOKEEPER_QUORUM,zk);
        conf.set(HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT,zkClientPort);
        conf.set(ZOOKEEPER_ZNODE_PARENT,zkParent);
        conf.set(HADOOP_SECURITY_AUTHENTICATION,PropertyUtil.getProperty("HADOOP_SERCURITY_AUTHENTICATION"));
        conf.set(HBASE_MASTER_KERBEROS_PRINCIPAL,PropertyUtil.getProperty("HBASE_MASTER_KERBEROS_PRINCIPAL"));
        conf.set(HBASE_REGIONSERVER_KERBEROS_PRINCIPAL,PropertyUtil.getProperty("HBASE_REGIONSERVER_KERBEROS_PRINCIPAL"));
        conf.set(HBASE_SECURITY_AUTHENTICATION,PropertyUtil.getProperty("HBASE_SERCURITY_AUTHENTICATION"));
        UserGroupInformation.setConfiguration(conf);

        return conf;
    }

    private static void executeJmxInsert(Configuration conf){

        ZooKeeperWatcher zooKeeperWatcher = null;
        HBaseAdmin hBaseAdmin = null;
        HbaseClusterInfo clusterInfo;
        try{
            System.out.println("----------executeJmxInsert start-------------");
            clusterInfo = new HbaseClusterInfo();
            zooKeeperWatcher = new ZooKeeperWatcher(conf,null,null);
            hBaseAdmin = new HBaseAdmin(conf);
            System.out.println("start getClusterStatus");
            ClusterStatus clusterStatus = hBaseAdmin.getClusterStatus();
            System.out.println("end getClusterStatus");

            logger.info("zooKeeperWatcher=" + zooKeeperWatcher);

            ServerName hmasterAddress = MasterAddressTracker.getMasterAddress(zooKeeperWatcher);
            HMASTER_URL = hmasterAddress.getHostname();
            logger.info("hmasterAddress=" + HMASTER_URL);

            System.out.println("--------clusterStatus-----------");
            System.out.println(clusterStatus);
            System.out.println("--------clusterStatus-----------");
            OpentsdbInsertTableInfo opentsdbInsertTableInfo = new OpentsdbInsertTableInfo(clusterInfo,HTTP_PROTOCOL,HMASTER_URL,HMASTER_JMX_PORT,clusterStatus,OPENTSDB_SERVER,Integer.parseInt(THREAD_COUNT));
            opentsdbInsertTableInfo.saveHbaseClusterInfo();
            //获取backup hmaster的信息
            Collection<ServerName> backup_masters = clusterStatus.getBackupMasters();
            ServerName[] backupServerNames = backup_masters.toArray(new ServerName[backup_masters.size()]);
            Arrays.sort(backupServerNames);
            logger.info("backupServerNames =" + backupServerNames);
            System.out.println("backupServerNames=" + backupServerNames);
            for(ServerName serverName :backupServerNames){
                clusterInfo = new HbaseClusterInfo();
                opentsdbInsertTableInfo = new OpentsdbInsertTableInfo(clusterInfo,HTTP_PROTOCOL,serverName.getHostname(),HMASTER_JMX_PORT,clusterStatus,OPENTSDB_SERVER,Integer.parseInt(THREAD_COUNT));
                opentsdbInsertTableInfo.saveHbaseClusterInfo();
            }

            closeHbaseAdmin(hBaseAdmin);
            closeZookWatcher(zooKeeperWatcher);
            System.out.println("----------executeJmxInsert end-------------");
        }catch(Exception e){
            System.out.println("OpentsdbMain, e="+e.getMessage());
            logger.error("opentsdbMain executeJmxInsert Exception="+e.getMessage(),e);
        }finally {
            if(hBaseAdmin != null){
                try {
                    hBaseAdmin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(zooKeeperWatcher != null){
                zooKeeperWatcher.close();
            }
        }
    }

    private static void executeJmxInsertPerhour(Configuration conf){

        ZooKeeperWatcher zooKeeperWatcher = null;
        HBaseAdmin hBaseAdmin = null;
        HbaseClusterInfoPerhour clusterInfoPerhour;
        HbaseUtil hbaseUtil;
        try{
            clusterInfoPerhour= new HbaseClusterInfoPerhour();
            zooKeeperWatcher = new ZooKeeperWatcher(conf,null,null);
            hBaseAdmin = new HBaseAdmin(conf);
            System.out.println("start get getClusterStatus");
            ClusterStatus clusterStatus = hBaseAdmin.getClusterStatus();
            System.out.println("end get getClusterStatus");

            logger.info("zooKeeperWatcher=" + zooKeeperWatcher);
            System.out.println("zooKeeperWatch=" + zooKeeperWatcher);
            ServerName hmasterAddress = MasterAddressTracker.getMasterAddress(zooKeeperWatcher);
            HMASTER_URL = hmasterAddress.getHostname();
            logger.info("hmasterAddress=" + HMASTER_URL);
            System.out.println("hmasterAddress=" + HMASTER_URL);
            hbaseUtil = new HbaseUtil(TABLE_NAME,conf);
            OpentsdbInsertTableInfo opentsdbInsertTableInfo = new OpentsdbInsertTableInfo(clusterInfoPerhour,HTTP_PROTOCOL,HMASTER_URL,HMASTER_JMX_PORT,hbaseUtil,Integer.parseInt(THREAD_COUNT));
            opentsdbInsertTableInfo.saveHbaseClusterInfoPerhour();

            //获取backup hmaster的信息
            Collection<ServerName> backup_masters = clusterStatus.getBackupMasters();
            ServerName[] backupServerNames = backup_masters.toArray(new ServerName[backup_masters.size()]);
            Arrays.sort(backupServerNames);
            logger.info("backupServerNames =" + backupServerNames);
            System.out.println("backupServerNames=" + backupServerNames);
            for(ServerName serverName :backupServerNames){
                clusterInfoPerhour = new HbaseClusterInfoPerhour();
                opentsdbInsertTableInfo = new OpentsdbInsertTableInfo(clusterInfoPerhour,HTTP_PROTOCOL,serverName.getHostname(),HMASTER_JMX_PORT,hbaseUtil,Integer.parseInt(THREAD_COUNT));
                opentsdbInsertTableInfo.saveHbaseClusterInfo();
            }

            closeHbaseAdmin(hBaseAdmin);
            closeZookWatcher(zooKeeperWatcher);
        }catch(Exception e){
            System.out.println("opentsdbMain executeJmxInsertPerhour Exception="+e.getMessage());
            logger.error("opentsdbMain executeJmxInsertPerhour Exception="+e.getMessage(),e);
        }finally {
            if(hBaseAdmin != null){
                try {
                    hBaseAdmin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(zooKeeperWatcher != null){
                zooKeeperWatcher.close();
            }
        }
    }


    private static void closeHbaseAdmin(HBaseAdmin admin){
        try{
            if(admin != null){
                admin.close();
            }
        }catch(Exception e){
            logger.error(e.toString());
        }
    }

    private static void closeZookWatcher(ZooKeeperWatcher zooKeeperWatcher){
        try{
            if(zooKeeperWatcher != null){
                zooKeeperWatcher.close();;
            }
        }catch(Exception e){
            logger.error(e.toString());
        }
    }
}
