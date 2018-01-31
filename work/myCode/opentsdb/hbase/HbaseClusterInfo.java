package com.sohu.opentsdb.hbase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sohu.hbase.HbaseReader;
import com.sohu.json.bean.hbase.*;
import com.sohu.opentsdb.Utils.DateUtil;
import com.sohu.opentsdb.Utils.OpentsdbUtils;
import com.sohu.opentsdb.enumMap.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * created by tangyuwen
 * date 2018/01/08
 */
public class HbaseClusterInfo{


    private List<Metrics> metricsList;
    private Map<String,String> hMasterTags;
    private PullJmxService service;
    private static String HTagContextType = "master";
    private static String RTagContextType = "regionserver";
    private static String HMASTER_REGIONSEVER_JMX_PORT = ":60010/jmx";
    private static Integer threadCount;
    private static final String GET = "get";
    private static final String prefix ="Master";
    private static final String ASSIGNMENT = "Hadoop:service=HBase,name=Master,sub=AssignmentManger";
    private static final String UGIMETRIC = "Hadoop:service=HBase,name=UgiMetrics";
    private static final String SYSTEMINFO = "java.lang:type=OperatingSystem";
    private static final String BALANCER = "Hadoop:service=HBase,name=Master,sub=Balancer";
    private static final String JVMMETRICS = "Hadoop:service=HBase,name=JvmMetrics";
    private static final String THREADMETRICS = "java.lang:type=Threading";
    private static final String FILESYSTEM = "Hadoop:service=HBase,name=Master,sub=FileSystem";
    private static final String HMASTER = "Hadoop:service=HBase,name=Master,sub=Server";
    private static final String IPCMETRICS = "Hadoop:service=HBase,name=Master,sub=IPC";

    private long insert_time = DateUtil.currentMinuteInMills()/1000;
    private ClusterStatus clusterStatus;
    private String opentsdb_url;
    private ThreadPoolExecutor executor;
    public final Logger logger = LoggerFactory.getLogger(getClass());

    public void getHbaseClusterInfo(String protocol, String url, String port,ClusterStatus clusterStatus,String opentsdb_url,Integer threadCount)throws Exception{
        try {
            System.out.println("getHbaseClusterInfo,protocol=" + protocol + ",url=" + url + ",port=" + port);
            logger.info("getHbaseClusterInfo,protocol=" + protocol + ",url=" + url + ",port=" + port);
            this.clusterStatus = clusterStatus;
            this.opentsdb_url = opentsdb_url;
            this.threadCount = threadCount;
            metricsList=Lists.newArrayList();

            if (hMasterTags == null) {
                hMasterTags = Maps.newHashMap();
            }
            hMasterTags.put("host", url);

            getAndSaveCommonInfo(protocol, url, port, SYSTEMINFO, OperatingSystem.class);
            getAndSaveCommonInfo(protocol, url, port, IPCMETRICS, Ipc.class);
            getAndSaveCommonInfo(protocol, url, port, JVMMETRICS, Jvm.class);
            getAndSaveCommonInfo(protocol, url, port, THREADMETRICS, Threads.class);
            getAndSaveCommonInfo(protocol, url, port, UGIMETRIC, Ugi.class);
            getAndSaveMasterInfo(protocol, url, port, ASSIGNMENT, AssignManger.class);
            getAndSaveMasterInfo(protocol, url, port, BALANCER, Balancer.class);
            getAndSaveMasterInfo(protocol, url, port, FILESYSTEM, FileSystem.class);
            getAndSaveMasterInfo(protocol, url, port, HMASTER, Master.class);
            OpentsdbUtils.insertOpentsdbMetric(metricsList,PutMethod.DETAILS_PUT, insert_time,opentsdb_url);
            System.out.println("-------inserttime-----");
            System.out.println(insert_time);
            System.out.println("----------------------");
        }catch(Exception e){
            System.out.println("getHbaseClusterInfo Exception" + e.getMessage());
            logger.error("getHbaseClusterInfo Exception" + e.getMessage(),e);
        }
    }

//    public static void main(String[] args)throws Exception{
//        HbaseClusterInfo info = new HbaseClusterInfo();
//        info.getHbaseClusterInfo("http://","dmeta2.heracles.sohuno.com",":60010/jmx",null,"10.2.177.218:4242");
//    }

    public void getAndSaveCommonInfo(String protocol,String url,String port,String objectName,Class clz) throws Exception{
        if(service==null){
            service = new PullJmxService();
        }
        Object object;
        try{
            object = service.getPullJmxBean(protocol+url+port,objectName,clz);
        }catch(Exception e){
            logger.error("getAndSaveCommonInfo() exception",e);
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
                metric.setMetricValue(declaredMethod.invoke(ipc,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
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
                metric.setMetricValue(declaredMethod.invoke(jvm,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }
        }else if(object instanceof OperatingSystem){
            OperatingSystem operatingSystem = (OperatingSystem)object;
            operatingSystem.setCreateTime(insert_time/1000);
            operatingSystem.setTagHostName(url);
            if(HMASTER_REGIONSEVER_JMX_PORT.equals(port)){
                operatingSystem.setTagContext(HTagContextType);
            }else{
                operatingSystem.setTagContext(RTagContextType);
            }

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
                metric.setMetricValue(declaredMethod.invoke(operatingSystem,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);

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
                metric.setMetricValue(declaredMethod.invoke(threads,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
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
                metric.setMetricValue(declaredMethod.invoke(ugi,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }
        }
    }
    public void getAndSaveMasterInfo(String protocol,String url,String port,String objectName,Class clz)throws Exception {
        if (service == null) {
            service = new PullJmxService();
        }
        Object object;
        try {
            object = service.getPullJmxBean(protocol + url + port, objectName, clz);
        } catch (Exception e) {
            logger.error("getAndSaveMasterInfo() exception", e);
            return;
        }
        if (object == null) {
            return;
        } else if (object instanceof AssignManger) {
            AssignManger assignManger = (AssignManger) object;
            Class clazz = assignManger.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            AssignManagerEnum[] enums = AssignManagerEnum.values();
            for (int i = 0; i < declaredFields.length; i++) {
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(assignManger,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }
        } else if (object instanceof FileSystem) {
            FileSystem fileSystem = (FileSystem) object;
            Class clazz = fileSystem.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            FileSystemEnum[] enums = FileSystemEnum.values();
            for (int i = 0; i < declaredFields.length; i++) {
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(fileSystem,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }
        } else if (object instanceof Balancer) {
            Balancer balancer = (Balancer) object;
            Class clazz = balancer.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            BalancerEnum[] enums = BalancerEnum.values();
            for (int i = 0; i < declaredFields.length; i++) {
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(balancer,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }
        } else if (object instanceof Master) {
            Master master = (Master) object;
            Class clazz = master.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            MasterEnum[] enums = MasterEnum.values();
            for (int i = 0; i < declaredFields.length; i++) {
                if(String.class.equals(declaredFields[i].getType())){
                    continue;
                }
                Method declaredMethod = clazz.getDeclaredMethod(GET + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1), null);
                Metrics metric = new Metrics();
                metric.setMetricName(prefix+"."+enums[i].getValue());
                metric.setMetricValue(declaredMethod.invoke(master,null).toString());
                metric.setTags(hMasterTags);
                metricsList.add(metric);
            }

            String regionServersInfo = master.getTagliveRegionServers();
            if (TextUtils.isEmpty(regionServersInfo) || regionServersInfo == null || regionServersInfo == "") {
                return;
            }else{
                String[] regionServers = regionServersInfo.split(";");
                System.out.println("regionServer数"+regionServers.length);
                ArrayList<FutureTask<Set<UserTable>>> tasks = new ArrayList<FutureTask<Set<UserTable>>>();
                executor=(ThreadPoolExecutor) Executors.newFixedThreadPool(regionServers.length>threadCount?threadCount:regionServers.length);

                try{
                    String regionServer;
                    for(String regionServerInfo:regionServers){
                        regionServer = regionServerInfo.split(",")[0];
                        RegionServerClusterInfo regionServerClusterInfo = new RegionServerClusterInfo(regionServer,opentsdb_url,insert_time,clusterStatus);
                        executor.execute(regionServerClusterInfo);
                        System.out.println("当前使用的线程数为"+executor.getActiveCount());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                finally {
                    executor.shutdownNow();
                }
            }
        }
    }
}
