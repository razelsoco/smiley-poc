package dbs.smileytown.poc.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import dbs.smileytown.poc.receiver.FileDownloader;

/**
 * Created by razelsoco on 27/1/16.
 */
public class AlarmScheduler {

    public static void scheduleAlarmForFileDownload(Context c) {

        Calendar cal = Calendar.getInstance();
        int currMinute = cal.get(Calendar.MINUTE);
        int setMinute=0;


        if(cal.get(Calendar.HOUR_OF_DAY) == 9 && currMinute <=25 ){
            setMinute = currMinute + 5;
        }else {

            if(currMinute >=0 && currMinute <5){

                if(cal.get(Calendar.HOUR_OF_DAY) >= 15 || cal.get(Calendar.HOUR_OF_DAY) < 9){
                    setMinute = 0;
                    cal.set(Calendar.HOUR_OF_DAY, 9);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }else{
                    setMinute = 5;
                }
            }else if(currMinute >=5 && currMinute < 35 ){

                if(cal.get(Calendar.HOUR_OF_DAY) >= 15 || cal.get(Calendar.HOUR_OF_DAY) < 9){
                    setMinute = 0;
                    cal.set(Calendar.HOUR_OF_DAY, 9);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }else{
                    setMinute = 35;
                }

            }else if(currMinute >= 35 && currMinute <= 59) {

                if(cal.get(Calendar.HOUR_OF_DAY) >= 15 || cal.get(Calendar.HOUR_OF_DAY) < 9){
                    setMinute = 0;
                    cal.set(Calendar.HOUR_OF_DAY, 9);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }else{
                    setMinute = 5;
                    cal.add(Calendar.HOUR, 1);
                }
            }
        }

        cal.set(Calendar.MINUTE, setMinute);
        cal.set(Calendar.SECOND, 0);

        Intent i = new Intent(c, FileDownloader.class);

        PendingIntent pi = PendingIntent.getBroadcast(c, 12345, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager)c.getSystemService(c.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        FileLogger.getInstance().writeLogs("Schedule next alarm time => " + FileLogger.getCurrentDateTime(cal));
    }
}
