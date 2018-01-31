package com.sohu.opentsdb.perHour;

import com.google.common.collect.Sets;
import com.sohu.json.bean.hbase.Master;
import com.sohu.json.bean.hbase.UserTable;
import com.sohu.mr.Data;
import com.sohu.opentsdb.Utils.DateUtil;
import com.sohu.opentsdb.Utils.HbaseUtil;
import com.sohu.opentsdb.enumMap.MasterEnum;
import com.sohu.opentsdb.hbase.PullJmxService;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * created by tangyuwen
 * date 2018/01/11
 */
public class HbaseClusterInfoPerhour {
    private static final String HMASTER = "Hadoop:service=HBase,name=Master,sub=Server";

    private PullJmxService service;
    private ThreadPoolExecutor executor;
    private Long insert_time;
    private Set<String> tableSets = Sets.newHashSet();
    private HbaseUtil hbaseUtil;
    private String COLUMN_FAMILY1="regionServer";
    private String COLUMN_FAMILY2="zk";
    private String GET ="get";
    public final Logger logger = LoggerFactory.getLogger(getClass());

    public Set<String> getHbaseClusterInfoPerhour(String protocol, String url, String port,HbaseUtil hbaseUtil,long insert_time) throws Exception{
        this.insert_time=insert_time;
        this.hbaseUtil = hbaseUtil;
        System.out.println("getHbaseClusterInfoPerhour,protocol=" + protocol + ",url=" + url + ",port=" + port);
        logger.info("getHbaseClusterInfo,protocol = " + protocol + ",url = " + url + ",port = " + port);
        getAndSavePerhourInfo(protocol, url, port, HMASTER, Master.class);

        return tableSets;
    }
    public static void main(String[] args) throws Exception{
        HbaseClusterInfoPerhour hbaseClusterInfoPerhour=new HbaseClusterInfoPerhour();
        hbaseClusterInfoPerhour.tableSets=new HashSet<String>();
        hbaseClusterInfoPerhour.getAndSavePerhourInfo("http://","dmeta2.heracles.sohuno.com",":60010/jmx","Hadoop:service=HBase,name=Master,sub=Server",Master.class);
        System.out.println(hbaseClusterInfoPerhour.tableSets);
    }


    public void getAndSavePerhourInfo(String protocol, String url, String port, String objectName, Class clz)throws Exception{
        service = new PullJmxService();
        Object object;
        try{
            object=service.getPullJmxBean(protocol+url+port,objectName,clz);
        }catch(Exception e){
            return;
        }
        if(object == null){
            return;
        }else if(object instanceof Master){
            ArrayList<Data> row = new ArrayList<Data>();
            String insert_time_str =insert_time.toString();
            int hashSalt = insert_time_str.substring(insert_time_str.length()-2,insert_time_str.length()).hashCode();
            String rowKey =hashSalt + ":" + (Long.MAX_VALUE-insert_time);
            row.add(new Data("rowkey",rowKey,rowKey));

            Master master = (Master)object;
            Class clazz=master.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            MasterEnum[] enums = MasterEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                if("tagliveRegionServers".equals(declaredFields[i].getName())){
                    Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                    row.add(new Data(COLUMN_FAMILY1,enums[i].getValue(),declaredMethod.invoke(master).toString()));
                }
                if("tagDeadRegionServers".equals(declaredFields[i].getName())){
                    Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                    row.add(new Data(COLUMN_FAMILY1,enums[i].getValue(),declaredMethod.invoke(master).toString()));
                }
                if("tagZookeeperQuorum".equals(declaredFields[i].getName())){
                    Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                    row.add(new Data(COLUMN_FAMILY2,enums[i].getValue(),declaredMethod.invoke(master).toString()));
                }
            }
            hbaseUtil.insertRow(row);
            String regionServersInfo = master.getTagliveRegionServers();
            if (TextUtils.isEmpty(regionServersInfo) || regionServersInfo == null || regionServersInfo == "") {
                return;
            } else{
                String[] regionServers = regionServersInfo.split(";");
                ArrayList<FutureTask<Set<String>>> tasks = new ArrayList<FutureTask<Set<String>>>();
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(regionServers.length > 5 ? 5 : regionServers.length);

                String regionServer;
                for(String regionServerInfo:regionServers){
                    regionServer = regionServerInfo.split(",")[0];
                    RegionServerClusterInfoPerhour regionServerClusterInfoPerhour = new RegionServerClusterInfoPerhour(regionServer,insert_time);
                    FutureTask<Set<String>> futureTask = new FutureTask<Set<String>>(regionServerClusterInfoPerhour);
                    tasks.add(futureTask);
                    executor.execute(futureTask);
                    System.out.println("当前使用的线程数为"+executor.getActiveCount());
                }
                try{
                    for (FutureTask<Set<String>> task:tasks){
                        Set<String> set = task.get();
                        tableSets.addAll(set);
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdownNow();
                }
            }
        }
    }
}
