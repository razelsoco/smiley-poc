package dbs.smileytown.poc.utils;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileLogger {

    private static FileLogger fileLogger;

    public static FileLogger getInstance(){
        if(fileLogger == null)
            fileLogger = new FileLogger();
        return fileLogger;
    }

    //File directory = Environment.getExternalStorageDirectory();
    String fpath = "DBSSmileyLog.txt";
    File file = new File(Environment.getExternalStorageDirectory(), fpath);

    public void writeLogs(String fcontent) {
        //if(StatusClass.debugging){
            try {
                //File file = new File(directory,fpath);
                // If file does not exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(getCurrentDateTime()+fcontent);
                bw.write("\n");
                bw.close();
                Log.i("Smiley", getCurrentDateTime() + fcontent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}

    }

    public static String getCurrentDateTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) + ": ";
    }

    public static String getCurrentDateTime(Calendar cal){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
    }

    public File getFile() {
        return file;
    }

    public static String getDeviceId(Context c){
        String deviceId;

        TelephonyManager mngr = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = mngr.getDeviceId();

        if(TextUtils.isEmpty(deviceId))
            deviceId = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);

        return deviceId;
    }
}
