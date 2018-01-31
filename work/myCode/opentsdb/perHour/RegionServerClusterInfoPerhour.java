package com.sohu.opentsdb.perHour;

import com.sohu.json.bean.hbase.RegionObj;
import com.sohu.json.bean.hbase.Regions;
import com.sohu.json.bean.hbase.UserTable;
import com.sohu.opentsdb.hbase.PullJmxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
/**
 * created by tangyuwen
 * date 2018/01/11
 */
public class RegionServerClusterInfoPerhour implements Callable<Set<String>> {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    private static String HTTP_PORTOCOL = "http://";
    private static String REGIONSERVER_JMX_PORT =":60030/jmx";
    private static final String REGIONS = "Hadoop:service=HBase,name=RegionServer,sub=Regions";

    private String regionServer;
    private long insert_time;
    private PullJmxService service;
    private Set<String> userTables = new HashSet<String>();

    @Override
    public Set<String> call() throws Exception {
        getAndSavePerhourInfo(HTTP_PORTOCOL,regionServer,REGIONSERVER_JMX_PORT,REGIONS,Regions.class);
        return userTables;
    }

    public RegionServerClusterInfoPerhour(String regionServer,long insert_time){
        this.regionServer = regionServer;
        this.insert_time = insert_time;
    }

    public void getAndSavePerhourInfo(String protocol,String url,String port,String objectName,Class clz) throws Exception{
        if(service == null){
            service = new PullJmxService();
        }
        Object object;
        try{
            object = service.getPullJmxBean(protocol+url+port,objectName,clz);
        }catch(Exception e){
            return;
        }
        if(object==null){
            return;
        }else if(object instanceof Regions){
            Regions regions = (Regions)object;
            List<RegionObj> regionObjs = regions.getRegionObjs();

            for(RegionObj regionObj:regionObjs){
                String tableName = regionObj.getTableName();
                if(tableName!=null || tableName.length()>0 || "".equals(tableName)){
                    userTables.add(tableName);
                }
            }
        }
    }
}
