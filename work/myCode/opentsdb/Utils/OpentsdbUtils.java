package com.sohu.opentsdb.Utils;


import com.sohu.opentsdb.enumMap.Metrics;
import com.sohu.util.httpClient.HttpClientUtils;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * created by tangyuwen
 * date: 2018/01/05
 */
public class OpentsdbUtils {
    private static String SUMMARY_PUT ="/api/put?summary";
    private static String DETAILS_PUT ="/api/put?details";
    private static String SYNC_PUT ="/api/put?sync";
    private static String SYNC_TIMEOUT_PUT="/api/put?sync&sync_timeout=";
    private static String HTTPPROTOCOL = "http://";
    private static int offset = 20000;
    public static Logger logger = LoggerFactory.getLogger(OpentsdbUtils.class);

//    private static String getPutToOpentsdbJson(Map<String,String> metrics,Map<String,String> tags,long timeStamp){
//        Set<Map.Entry<String,String>> metricsSet=metrics.entrySet();
//        Iterator iterator = metricsSet.iterator();
//        StringBuilder sb = new StringBuilder();
//        while(iterator.hasNext()){
//            Map.Entry<String,String> entry = (Map.Entry<String,String>)iterator.next();
//            sb.append(bePutJson(entry.getKey(),entry.getValue(),tags,timeStamp));
//            if(!iterator.hasNext()){
//                sb.delete(sb.length()-1,sb.length());
//            }
//        }
//        sb.insert(0,'[');
//        sb.append(']');
//        return sb.toString();
//    }

    private static String getPutToOpentsdbJson(List<Metrics> metrics,long timeStamp){
        if(metrics.size()==0||metrics==null){
            System.out.println("List为空啊！！！！！");
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<metrics.size();i++){
            Metrics metric=metrics.get(i);
            sb.append(bePutJson(metric.getMetricName(),metric.getMetricValue(),metric.getTags(),timeStamp));
            if(i==metrics.size()-1){
                sb.delete(sb.length()-1,sb.length());
            }
        }
        sb.insert(0,'[');
        sb.append(']');

        return sb.toString();
    }

    private static String bePutJson(String metrics,String value,Map<String,String> tags,long timeStamp){
        StringBuilder jsonBody = new StringBuilder();
        StringBuilder tagsStr = new StringBuilder();
        Set<Map.Entry<String,String>> tagsSet=tags.entrySet();
        Iterator it=tagsSet.iterator();
        while(it.hasNext()){
            Map.Entry<String,String> entry=(Map.Entry<String,String>)it.next();
            tagsStr.append("\"" + entry.getKey() + "\":" + "\"" + entry.getValue() + "\",");
            if(!it.hasNext()){
                tagsStr.delete(tagsStr.length()-1,tagsStr.length());
            }
        }
        jsonBody.append("{");
        jsonBody.append("\"metric\":"+"\""+ metrics +"\"," );
        jsonBody.append("\"timestamp\":" + timeStamp +",");
        jsonBody.append("\"value\":"+value+",");
        jsonBody.append("\"tags\":{"+tagsStr.toString()+"}");
        jsonBody.append("},");
        return jsonBody.toString();
    }

    private static String summaryPut(String jsonBody,String host)throws Exception{
        try{
            StringBuilder url = new StringBuilder();
            url.append(HTTPPROTOCOL);
            url.append(host);
            url.append(SUMMARY_PUT);
            String content=HttpClientUtils.post(url.toString(),jsonBody);
            return content;
        }catch(Exception e){
            logger.error("summaryPut Exception =" +e.getMessage(),e);
            throw new Exception(e);
        }

    }

    private static String detailsPut(String jsonBody,String host)throws Exception{
        try{
            StringBuilder url = new StringBuilder();
            url.append(HTTPPROTOCOL);
            url.append(host);
            url.append(DETAILS_PUT);
            if(jsonBody.equals("[]")){
                System.out.println("JSON空啦");
            }
            String content=HttpClientUtils.post(url.toString(),jsonBody);
            return content;
        }catch(Exception e){
            System.out.println("detailsPut Exception =" +e.getMessage());
            logger.error("detailsPut Exception =" +e.getMessage(),e);
            return null;
        }
    }

    private static String synPut(String jsonBody,String host)throws Exception{
        try{
            StringBuilder url = new StringBuilder();
            url.append(HTTPPROTOCOL);
            url.append(host);
            url.append(SYNC_PUT);
            String content=HttpClientUtils.post(url.toString(),jsonBody);
            return content;
        }catch(Exception e){
            logger.error("synPut Exception =" +e.getMessage(),e);
            throw new Exception(e);
        }

    }

    private static String synTimeOutPut(String jsonBody,String host)throws Exception{
        try{
            StringBuilder url = new StringBuilder();
            url.append(HTTPPROTOCOL);
            url.append(host);
            url.append(SYNC_TIMEOUT_PUT);
            url.append("60000");
            String content=HttpClientUtils.post(url.toString(),jsonBody);
            return content;
        }catch(Exception e){
            logger.error("synTimeOutPut Exception =" +e.getMessage(),e);
            throw new Exception(e);
        }

    }

    /**
     *
     * @param metrics key为指标名 value为指标值
     * @param tags tags为标签名，value为标签值
     * @return
     */
//    public static String insertOpentsdbMetric(Map<String,String> metrics,Map<String,String> tags,String method,long timeStamp,String host)throws Exception{
//        logger.info("{} insertOpentsdbMetric start ",OpentsdbUtils.class);
//        if (method == "summary") {
//            return summaryPut(getPutToOpentsdbJson(metrics, tags,timeStamp),host);
//        }
//        if(method == "details"){
//            return detailsPut(getPutToOpentsdbJson(metrics,tags,timeStamp),host);
//        }
//        if(method == "sync"){
//            return synPut(getPutToOpentsdbJson(metrics,tags,timeStamp),host);
//        }
//        //默认为一分钟
//        if(method == "sync_timeout"){
//            return synTimeOutPut(getPutToOpentsdbJson(metrics,tags,timeStamp),host);
//        }
//        return null;
//    }

    /**
     *
     * @param metrics 存储所有Metric的List
     * @param method  PUT方式
     * @param timeStamp 当前时间戳
     * @param host OPENTSDB服务HOST
     * @return
     * @throws Exception
     */
    public static void insertOpentsdbMetric(List<Metrics> metrics,String method,long timeStamp,String host)throws Exception {
        logger.info("{} insertOpentsdbMetric start ", OpentsdbUtils.class);
        String result;
        int i = 0;
        if (method == "details") {
            if (metrics.size() < offset) {
                if(metrics.size()>0 && metrics!=null){
                    result = detailsPut(getPutToOpentsdbJson(metrics, timeStamp), host);
                    System.out.println("--------Content-----------");
                    logger.info("Content: ----" + result + "----");
                    System.out.println(result);

                }
            } else {
                while (i + offset < metrics.size()) {
                        result = detailsPut(getPutToOpentsdbJson(metrics.subList(i, i+offset), timeStamp), host);
                        System.out.println("--------Content-----------");
                        logger.info("Content: ----" + result + "----");
                        System.out.println(result);
                        i += offset;
                }
                if(metrics.subList(i, metrics.size()).size()>0) {
                    result = detailsPut(getPutToOpentsdbJson(metrics.subList(i, metrics.size()), timeStamp), host);
                    System.out.println("--------Content-----------");
                    logger.info("Content: ----" + result + "----");
                    System.out.println(result);
                }
            }
        }
    }


    public static void main(String[] args){
        Map metrics = new HashMap();
        metrics.put("metric1","123");
        metrics.put("metric2","123");
        Map tags1 = new HashMap();
        tags1.put("server","01");
        tags1.put("regionServer","03");
        Map tags2 = new HashMap();
        tags2.put("server","02");
        tags2.put("regionServer","04");

        List list =new ArrayList();
        for(int i=0;i<4;i++){
            list.add(i);
        }
        List list2=list.subList(4,4);
        System.out.println(list2);
        System.out.println(list.size());
//        StringBuilder url = new StringBuilder();
//        url.append(HTTPPROTOCOL);
//        url.append("10.2.177.218");
//        url.append(OPENTSDB_TSD_PORT);
//        url.append(SUMMARY_PUT);
//        String testJson = "[{\n" +
//                "\t\t\"metric\": \"RegionServer.HBase.Regions.storeCount\",\n" +
//                "\t\t\"timestamp\": 1484558800,\n" +
//                "\t\t\"value\": 150,\n" +
//                "\t\t\"tags\": {\n" +
//                "\t\t\t\"host\": \"10.2.177.218\",\n" +
//                "\t\t\t\"tableName\": \"table1\"\n" +
//                "\t\t}\n" +
//                "\t},\n" +
//                "\t{\n" +
//                "\t\t\"metric\": \"RegionServer.HBase.Regions.storeCount\",\n" +
//                "\t\t\"timestamp\": 1484558800,\n" +
//                "\t\t\"value\": 150,\n" +
//                "\t\t\"tags\": {\n" +
//                "\t\t\t\"host\": \"10.2.177.218\",\n" +
//                "\t\t\t\"tableName\": \"table1\"\n" +
//                "\t\t}\n" +
//                "\t}\n" +
//                "]";



        //HttpClientUtils.post(url.toString(),getPutToOpentsdbJson(metrics,tags,System.currentTimeMillis()/1000));

    }

}
