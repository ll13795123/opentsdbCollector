package com.sohu.opentsdb.hbase;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sohu.json.bean.hbase.*;
import com.sohu.opentsdb.Utils.OpentsdbUtils;
import com.sohu.opentsdb.enumMap.*;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.RegionLoad;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * created by tangyuwen
 * date 2018/01/08
 */
public class RegionServerClusterInfo implements Runnable{
    public final Logger logger =LoggerFactory.getLogger(getClass());

    private static String HTTP_PORTOCOL = "http://";
    private static String REGIONSERVER_JMX_PORT =":60030/jmx";
    private static final String GET = "get";
    private static final String prefix = "RegionServer";
    private static final String UGIMETRIC = "Hadoop:service=HBase,name=UgiMetrics";
    private static final String SYSTEMINFO = "java.lang:type=OperatingSystem";
    private static final String JVMMETRICS = "Hadoop:service=HBase,name=JvmMetrics";
    private static final String THREADMETRICS = "java.lang:type=Threading";
    private static final String IPCMETRICS = "Hadoop:service=HBase,name=RegionServer,sub=IPC";
    private static final String WALMETRCS = "Hadoop:service=HBase,name=RegionServer,sub=WAL";
    private static final String REGIONSEVER = "Hadoop:service=HBase,name=RegionServer,sub=Server";
    private static final String REGIONS = "Hadoop:service=HBase,name=RegionServer,sub=Regions";
    private HashMap<String,String> hRegionTags;
    private Map<String,String> commonTags;
    private List<Metrics> commonList;
    private List<Metrics> metricsList;

    private String regionServer;
    private String opentsdb_url;
    private ClusterStatus clusterStatus;
    private long insert_time;
    private PullJmxService service;

    public RegionServerClusterInfo(String regionServer,String opentsdb_url,long insert_time,ClusterStatus clusterStatus){
        this.opentsdb_url = opentsdb_url;
        this.regionServer=regionServer;
        this.insert_time=insert_time;
        this.clusterStatus=clusterStatus;
        hRegionTags=Maps.newHashMap();
        hRegionTags.put("host",regionServer);
        commonTags=Maps.newHashMap();
        commonTags.put("host",regionServer);
    }

    @Override
    public void run() {
        try {
            getRegionServerInfo(regionServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getRegionServerInfo(String regionServer) throws Exception{
        System.out.println("------start collecting regionServer metrics------");
        logger.info("getRegionServerInfo,protocol ="+HTTP_PORTOCOL+",url="+regionServer+",port=" +REGIONSERVER_JMX_PORT);
        commonList = Lists.newArrayList();
        metricsList = Lists.newArrayList();
        getAndSaveCommomInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, SYSTEMINFO, OperatingSystem.class);
        getAndSaveCommomInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, IPCMETRICS, Ipc.class);
        getAndSaveCommomInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, JVMMETRICS, Jvm.class);
        getAndSaveCommomInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, THREADMETRICS, Threads.class);
        getAndSaveCommomInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, UGIMETRIC, Ugi.class);
        getAndSaveRegionServerInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, REGIONSEVER, RegionServer.class);
        getAndSaveRegionServerInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, WALMETRCS, Wal.class);
        //System.out.println(Thread.currentThread()+"抓取regions的信息之前的时间："+System.currentTimeMillis());
        getAndSaveRegionServerInfo(HTTP_PORTOCOL, regionServer, REGIONSERVER_JMX_PORT, REGIONS, Regions.class);

        OpentsdbUtils.insertOpentsdbMetric(commonList,PutMethod.SUMMARY_PUT,insert_time,opentsdb_url);
        OpentsdbUtils.insertOpentsdbMetric(metricsList,PutMethod.DETAILS_PUT,insert_time,opentsdb_url);
    }

    public void getAndSaveCommomInfo(String protocol,String url,String port,String objectName,Class clz) throws Exception{
        if(service==null){
            service=new PullJmxService();
        }
        Object object;
        try{
            object = service.getPullJmxBean(protocol+url+port,objectName,clz);
        }catch(Exception e){
            return;
        }
        if(object == null){
            return;
        }else if(object instanceof Ipc){
            Ipc ipc = (Ipc)object;
            Class clazz = ipc.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            IpcEnum[] enums=IpcEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(ipc).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof Jvm){
            Jvm jvm = (Jvm) object;
            Class clazz = jvm.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            JvmEnum[] enums=JvmEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(jvm).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof OperatingSystem){
            OperatingSystem operatingSystem = (OperatingSystem)object;
            operatingSystem.setCreateTime(insert_time/1000);
            operatingSystem.setTagHostName(url);
            operatingSystem.setTagContext("regionServer");

            Class clazz = operatingSystem.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            OperatingSystemEnum[] enums =OperatingSystemEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                if(String.class.equals(declaredFields[i].getType())){
                    continue;
                }
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(operatingSystem).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof Threads){
            Threads threads = (Threads)object;
            Class clazz = threads.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            ThreadsEnum[] enums = ThreadsEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(threads).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof Ugi){
            Ugi ugi = (Ugi)object;
            Class clazz = ugi.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            UgiEnum[] enums =UgiEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(ugi).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }
    }

    public void getAndSaveRegionServerInfo(String protocol,String url,String port,String objectName,Class clz) throws Exception{
        if(service == null){
            service = new PullJmxService();
        }
        Object object;
        try{
            object = service.getPullJmxBean(protocol+url+port,objectName,clz);
        }catch(Exception e){
            return;
        }
        if(object == null){
            return;
        }else if(object instanceof RegionServer){
            RegionServer regionServer = (RegionServer)object;
            Class clazz = regionServer.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            RegionServerEnum[] enums = RegionServerEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(regionServer).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof Wal){
            Wal wal = (Wal)object;
            Class clazz = wal.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            WalEnum[] enums =WalEnum.values();
            for(int i=0;i<declaredFields.length;i++){
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(wal).toString());
                metric.setTags(commonTags);
                commonList.add(metric);
            }
        }else if(object instanceof Regions){
            Regions regions = (Regions)object;
            String hostName = regions.getTagHostName();
            regions.setCreateTime(insert_time);

            //处理requestCount的相关信息
            Map<String,Long> tableRequestCount = new HashMap<String,Long>();
            Long serverRequestCount = 0l;
            for(ServerName serverName: clusterStatus.getServers()){
                if(serverName.getHostname().equals(regions.getTagHostName())){
                    System.out.println("serverName:"+serverName.getHostname()+",port:"+serverName.getPort()+",startCode"+serverName.getStartcode());
                    ServerLoad serverLoad = clusterStatus.getLoad(serverName);
                    for(Map.Entry<byte[],RegionLoad> entry: serverLoad.getRegionsLoad().entrySet()){
                        RegionLoad regionLoad = entry.getValue();
                        //System.out.println("regionName:"+regionLoad.getNameAsString()+",requestCount:"+regionLoad.getRequestsCount());
                        //取得的tablename是namespace:table的形式，要转换成_table_的形式
                        String tableName = regionLoad.getNameAsString().split(",")[0].replace(":","_table_");
                        Long value = regionLoad.getRequestsCount();
                        serverRequestCount+=value;
                        if(tableRequestCount.containsKey(tableName)){
                            tableRequestCount.put(tableName,value+tableRequestCount.get(tableName));
                        }else{
                            tableRequestCount.put(tableName,value);
                        }
                    }
                }
            }


            List<RegionObj> regionObjs = regions.getRegionObjs();
            long before = System.currentTimeMillis();
            for(RegionObj regionObj:regionObjs){
                String tableName = regionObj.getTableName();
                try{
                    //System.out.println(tableName+" : requestCount : "+tableRequestCount.get(tableName));
                    regionObj.setRequestCount(tableRequestCount.get(tableName));
                }catch(Exception e){
                    //e.printStackTrace();
                }
                Class clazz = regionObj.getClass();
                Field[] declaredFields = clazz.getDeclaredFields();
                RegionObjEnum[] enums = RegionObjEnum.values();
                for(int i=0;i<declaredFields.length;i++){
                    Field declaredField = declaredFields[i];
                    String metricName=enums[i].getValue();
                    if((!metricName.contains("tableName"))){
                        if(String.class.equals(declaredField.getType())){
                            continue;
                        }
                        Method declaredMethod = clazz.getDeclaredMethod(GET+declaredField.getName().substring(0,1).toUpperCase()+ declaredField.getName().substring(1),null);
                        Metrics metric = new Metrics();
                        metric.setMetricName(prefix+"."+metricName);
                        metric.setMetricValue(declaredMethod.invoke(regionObj).toString());
                        metric.setTags((Map<String,String>)hRegionTags.clone());
                        metricsList.add(metric);
                    }else if(metricName.contains("tableName")){
                        hRegionTags.put("tableName",tableName);
                    }
                }


                hRegionTags.remove("tableName");
            }

            System.out.println("收集该RegionServer下所有table数据所耗时间为"+ (System.currentTimeMillis()-before));
        }
    }


}
