package dbs.smileytown.poc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dbs.smileytown.poc.email.MailSenderAsyncTask;
import dbs.smileytown.poc.network.ApiManager;
import dbs.smileytown.poc.network.CallbackWithRetry;
import dbs.smileytown.poc.utils.AlarmScheduler;
import dbs.smileytown.poc.utils.ExcelParser;
import dbs.smileytown.poc.utils.FileLogger;
import retrofit.Retrofit;

public class FileDownloader extends BroadcastReceiver {
	private static File mFile;

	@Override
	public void onReceive(final Context context, Intent intent) {
		//get file
		downloadFile(context);
	}

	public static String unixTimeStampToDate(long timeStamp) {
		//Utils.log("2359", "Date passed: " + timeStamp);
		timeStamp = timeStamp * 1000;
		String dateFormat = "dd MMM yyyy HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
		java.util.Date resultDate = new java.util.Date(timeStamp);
		return sdf.format(resultDate);
	}

	public static void copyInputStreamToFile(Context c, InputStream in) {
		try {
			OutputStream out = new FileOutputStream(getFile(c));
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getFile(Context c){
		if(mFile == null)
			mFile = new File(c.getExternalCacheDir()+"data.xslx");

		return mFile;
	}

	public static void downloadFile(final Context context){
		//Log.d("smiley","File download =====> "+ unixTimeStampToDate(System.currentTimeMillis()/1000));
		FileLogger.getInstance().writeLogs("Balance data file download start checkInternetConnectivity =>" + checkInternetConnectivity(context));
		retrofit.Call<ResponseBody> call = ApiManager.getApiService().getFile();
		call.enqueue(new CallbackWithRetry<ResponseBody>(call) {
			@Override
			public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
				InputStream is = null;

				if (response.body() != null) {
					try {
						is = response.body().byteStream();
						copyInputStreamToFile(context, is);
						FileLogger.getInstance().writeLogs("Balance data file download finish");
						ExcelParser.getInstance().parse(context);
						//FileLogger.getInstance().writeLogs("Balance data file download finish => DATA : \n" +ExcelParser.getInstance().getBalanceDataMap());
						//Log.d("smiley", "File download finish Data SIZE ==> " + ExcelParser.getInstance().getBalanceDataMap().size());
						//Log.d("smiley", "File download finish Data==> " + ExcelParser.getInstance().getBalanceDataMap());
					} catch (IOException e) {
						e.printStackTrace();
						FileLogger.getInstance().writeLogs("ERROR downloading balance data file error I/O : " + e.getMessage());
					}
				} else {
					//Log.d("smiley", "File download fail =====> " + response.message());
					FileLogger.getInstance().writeLogs("ERROR downloading balance data file error : " + response.code() + " " + response.message());
				}

				sendLogsToEmail(context);
			}

			@Override
			public void onFailure(Throwable t) {
				super.onFailure(t);
				sendLogsToEmail(context);
			}
		});

		AlarmScheduler.scheduleAlarmForFileDownload(context);
	}

	public static boolean checkInternetConnectivity(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobileNetworkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			//if (networkInfo != null || mobileNetworkInfo != null) {
				if (networkInfo !=null && networkInfo.isConnected()) {
					return true;
				} else if (mobileNetworkInfo  !=null && mobileNetworkInfo.isConnected()) {
					return true;
				}

			//}
		}
		return false;
	}

	public static void sendLogsToEmail(Context c){
		Calendar cal = Calendar.getInstance();
		if((cal.get(Calendar.HOUR_OF_DAY)==9 && cal.get(Calendar.MINUTE) <= 30)
				|| cal.get(Calendar.HOUR_OF_DAY)== 15 )
			new MailSenderAsyncTask(c).execute();
	}

}
