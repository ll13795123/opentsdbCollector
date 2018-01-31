package com.sohu.opentsdb.hbase;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.sohu.json.bean.hbase.MBean;
import com.sohu.json.bean.hbase.RegionObj;
import com.sohu.json.bean.hbase.Regions;
import com.sohu.util.BeanUtil;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * create by tangyuwen
 * date 2018/01/04
 */

public class PullJmxService<T> {
    private BeanUtil beanUtil = null;
    private Gson gson = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    ParameterizedType type(final Class raw,final Type...args){
        return new ParameterizedType(){
            @Override
            public Type[] getActualTypeArguments() {
                return args;
            }

            @Override
            public Type getRawType() {
                return raw;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    public T getPullJmxBean(String url,String qryName,Class T) throws Exception{
        String jsonString = null;
        beanUtil = new BeanUtil();
        try{
            Long startTime = System.currentTimeMillis();
            jsonString =beanUtil.getMBean(url,qryName);
        }catch(Exception e){
            logger.info("getPullJmxBean, Exception , e ="+e.getMessage()+", url ="+url+"qryName="+qryName);
        }
        if(jsonString==null){
            return null;
        }else{
            if(Regions.class.getName().equals(T.getName())){
                Long startTime = System.currentTimeMillis();
                Regions regions = new Regions();
//                jsonString = jsonString.replaceAll("[a-zA-Z]{4}space_", "namespace_").replaceAll("_metric_", ",").concat("\"");
                JSONObject jObject = new JSONObject(jsonString);
                JSONObject regionsObject = new JSONObject(jObject.getJSONArray("beans").get(0).toString());
                Iterator keys = regionsObject.keys();
                regions.setTagContext(regionsObject.getString("tag.Context"));
                regions.setTagHostName(regionsObject.getString("tag.Hostname"));
                Map<String,RegionObj> regionObjMap = new HashMap<String,RegionObj>();
                Map<String,String> tableOperationMap = new HashMap<String,String>();
                List<RegionObj> regionObjs=new ArrayList<RegionObj>();
                Set<String> regionNames = new HashSet<String>();
                Map<String,Set<String>> regionToNums =new HashMap<String,Set<String>>();
                while(keys.hasNext()){
                    String key = (String)keys.next();
                    if(key.matches("^[a-zA-Z]{4}space_.*$")){
                        String mapKey = key.replaceFirst("^[N|n]amespace_", "").replaceAll("_region_.*_",",");
                        String regionName = key.replace("_region_","~").replaceAll("Namespace_.*~","").replaceAll("_metric_.*","");
                        regionNames.add(regionName);

                        String tableName = mapKey.split(",")[0];
                        if(regionToNums.containsKey(tableName)){
                            regionToNums.get(tableName).add(regionName);
                        }else{
                            regionToNums.put(tableName, Sets.<String>newHashSet(regionName));
                        }

                        if(!tableOperationMap.containsKey(mapKey)){
                            tableOperationMap.put(mapKey,regionsObject.getString(key));
                        }else{
                            if(mapKey.contains("memStoreSize") || mapKey.contains("storeFileSize") || mapKey.contains("compactionsCompletedCount") || mapKey.contains("numBytesCompactedCount") || mapKey.contains("numFilesCompactedCount")){
                                Long keyValue = regionsObject.getLong(key);
                                keyValue += Long.valueOf(tableOperationMap.get(mapKey).toString());
                                tableOperationMap.put(mapKey,String.valueOf(keyValue));
                            }
                            if (mapKey.contains("storeCount") || mapKey.contains("storeFileCount") || mapKey.contains("get_num_ops") || mapKey.contains("get_min") || mapKey.contains("get_max") || mapKey.contains("scanNext_num_ops") || mapKey.contains("scanNext_min") || mapKey.contains("scanNext_max") || mapKey.contains("deleteCount") || mapKey.contains("appendCount") || mapKey.contains("mutateCount") || mapKey.contains("incrementCount")) {
                                Integer keyValue = regionsObject.getInt(key);
                                keyValue += Integer.valueOf(tableOperationMap.get(mapKey).toString());
                                tableOperationMap.put(mapKey, String.valueOf(keyValue));
                            }
                            if (mapKey.contains("get_mean") || mapKey.contains("get_median") || mapKey.contains("get_75th_percentile") || mapKey.contains("get_95th_percentile") || mapKey.contains("get_99th_percentile") || mapKey.contains("scanNext_mean") || mapKey.contains("scanNext_median") || mapKey.contains("scanNext_75th_percentile") || mapKey.contains("scanNext_95th_percentile") || mapKey.contains("scanNext_99th_percentile")) {
                                Double keyValue = regionsObject.getDouble(key);
                                keyValue += Double.valueOf(tableOperationMap.get(mapKey).toString());
                                tableOperationMap.put(mapKey, String.valueOf(keyValue));
                            }
                        }
                    }
                }
                regions.setRegionNum(regionNames.size());
                Iterator<Map.Entry<String,String>> iterator = tableOperationMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> iteratorStr = iterator.next();
                    String[] split = iteratorStr.getKey().split(",");
                    String tableName =split[0];
                    String operationName = split[1];
                    RegionObj regionObj = new RegionObj();
                    regionObj.setTableName(tableName);
                    regionObj.setRegionNum(regionToNums.get(tableName).size());
                    if(regionObjMap.containsKey(tableName)){
                        regionObj = regionObjMap.get(tableName);
                    }

                    if (operationName.contains("storeCount")) {
                        regionObj.setStoreCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("storeFileCount")) {
                        regionObj.setStoreFileCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("memStoreSize")) {
                        regionObj.setMemStoreSize(Long.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("storeFileSize")) {
                        regionObj.setStoreFileSize(Long.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("compactionsCompletedCount")) {
                        regionObj.setCompactionsCompletedCount(Long.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("numBytesCompactedCount")) {
                        regionObj.setNumBytesCompactedCount(Long.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("numFilesCompactedCount")) {
                        regionObj.setNumFilesCompactedCount(Long.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("get_num_ops")) {
                        regionObj.setGetNumOps(Long.parseLong(iteratorStr.getValue()));
                    }
                    if (operationName.contains("get_min")) {
                        regionObj.setGetMin(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("get_max")) {
                        regionObj.setGetMax(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("get_mean")) {
                        regionObj.setGetMean(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("get_median")) {
                        regionObj.setGetMedian(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("get_75th_percentile")) {
                        regionObj.setGet75thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("get_95th_percentile")) {
                        regionObj.setGet95thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("get_99th_percentile")) {
                        regionObj.setGet99thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("scanNext_num_ops")) {
                        regionObj.setScanNextNumOps(Long.parseLong(iteratorStr.getValue()));
                    }
                    if (operationName.contains("scanNext_min")) {
                        regionObj.setScanNextMin(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("scanNext_max")) {
                        regionObj.setScanNextMax(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("scanNext_mean")) {
                        regionObj.setScanNextMean(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("scanNext_median")) {
                        regionObj.setScanNextMedian(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("scanNext_75th_percentile")) {
                        regionObj.setScanNext75thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("scanNext_95th_percentile")) {
                        regionObj.setScanNext95thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("scanNext_99th_percentile")) {
                        regionObj.setScanNext99thPercentile(Double.valueOf(iteratorStr.getValue().toString()));
                    }
                    if (operationName.contains("deleteCount")) {
                        regionObj.setDeleteCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("appendCount")) {
                        regionObj.setAppendCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("mutateCount")) {
                        regionObj.setMutateCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    if (operationName.contains("incrementCount")) {
                        regionObj.setIncrementCount(Integer.parseInt(iteratorStr.getValue()));
                    }
                    regionObjMap.put(regionObj.getTableName(), regionObj);
                }
                for (String key : regionObjMap.keySet()) {
                    regionObjs.add(regionObjMap.get(key));
                }
                regions.setRegionObjs(regionObjs);
                System.out.println("聚合regions的table中各region所花时间："+(System.currentTimeMillis()-startTime)+"ms");
                return (T) regions;
            }else{
                gson = new Gson();
                Type type =type(MBean.class,T);
                MBean<T> mBean = (MBean)gson.fromJson(jsonString,type);
                if(mBean==null || mBean.getBeans().size()==0){
                    return null;
                }
                T t =(T) mBean.getBeans().get(0);
                return t;
            }
        }
    }
    public static void main(String[] args)throws Exception{
        PullJmxService<Regions> service = new PullJmxService<Regions>();
        Regions regions=service.getPullJmxBean("http://10.2.177.215:60030/jmx","Hadoop:service=HBase,name=RegionServer,sub=Regions",Regions.class);
        List<RegionObj>  list=regions.getRegionObjs();
        String nameSpaceTableName=list.get(1).getTableName();
        String nameSpace;
        String tableName;
        nameSpace=nameSpaceTableName.replaceAll("_table_.*","");
        tableName=nameSpaceTableName.replaceAll("_table_",",").split(",")[1];
        System.out.println(list.get(1));
        System.out.println(nameSpace);
        System.out.println(tableName);

//        String str= "{\"Namespace_APP_table_jobhistory_region_6aaf0e14db2ad31ca1e0b0c69d13ba98_metric_storeCount\" : 3,\n" +
//                "    \"Namespace_APP_table_jobhistory_region_6aaf0e14db2ad31ca1e0b0c69d13ba98_metric_storeFileCount\" : 12}";
//        JSONObject jsonObject = new JSONObject(str);
//        Iterator iterator=jsonObject.keys();
//        while(iterator.hasNext()){
//            String key=(String)iterator.next();
//            String mapKey = key.replaceFirst("^Namespace_", "").replaceAll("_region_.*_",",");
//            String regionName = key.replace("_region_","~").replaceAll("Namespace_.*~","").replaceAll("_metric_.*","");
//            System.out.println(mapKey);
//            String tableName=mapKey.split(",")[0];
//
//            System.out.println(tableName);
//        }

    }
}
