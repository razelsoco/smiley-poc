package dbs.smileytown.poc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import dbs.smileytown.poc.network.ApiManager;
import dbs.smileytown.poc.utils.AlarmScheduler;
import dbs.smileytown.poc.utils.ExcelParser;
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
		Log.d("smiley","File download =====> "+ unixTimeStampToDate(System.currentTimeMillis()/1000));
		ApiManager.getApiService().getFile().enqueue(new retrofit.Callback<ResponseBody>() {
			@Override
			public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
				InputStream is = null;

				if (response.body() != null) {
					try {
						is = response.body().byteStream();
						copyInputStreamToFile(context, is);
						ExcelParser.getInstance().parse(context);

						Log.d("smiley", "File download finish Data==> " + ExcelParser.getInstance().getBalanceDataMap());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Log.d("smiley", "File download fail =====> " + response.message());
				}
			}

			@Override
			public void onFailure(Throwable t) {

			}
		});

		AlarmScheduler.scheduleAlarmForFileDownload(context);
	}

}
