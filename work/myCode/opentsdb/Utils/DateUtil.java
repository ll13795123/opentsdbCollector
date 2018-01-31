package com.sohu.opentsdb.Utils;

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static long currentHourInMills(){
        long currentHourMills;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(year,month,date,hourOfDay,0,0);
        calendar.set(Calendar.MILLISECOND,0);

        currentHourMills = calendar.getTime().getTime();
        long offset = System.currentTimeMillis() - calendar.getTime().getTime();
        if(offset>60000){
            currentHourMills=currentHourMills+3600000;
        }
        return currentHourMills;
    }
    public static long currentMinuteInMills(){
        long currentMinuteMills;
        Calendar calendar = Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int hourofDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = calendar.get(Calendar.MINUTE);
        if(minuteOfHour%5==0){
            minuteOfHour=5*(minuteOfHour/5);
        }else{
            minuteOfHour=5*(minuteOfHour/5)+5;
        }
        calendar.set(year,month,date,hourofDay,minuteOfHour,0);
        calendar.set(Calendar.MILLISECOND,0);

        currentMinuteMills = calendar.getTime().getTime();
        return currentMinuteMills;
    }

    public static void main(String[] args){
        try{
            System.out.println(DateUtil.currentHourInMills());
            System.out.println(new Date(DateUtil.currentHourInMills()));
            Thread.sleep(1000);
            System.out.println(DateUtil.currentHourInMills());

            System.out.println("-----------currentMinute---------------");
            System.out.println(currentMinuteInMills());
            System.out.println(new Date(currentMinuteInMills()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
