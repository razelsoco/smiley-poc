package dbs.smileytown.poc.email;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import dbs.smileytown.poc.utils.FileLogger;

/**
 * Created by razelsoco on 4/2/16.
 */
public class MailSenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    static String[] toArr = {"razel.soco@2359media.com","gray.ang@2359media.com"}; //,"gray.ang@2359media.com"
    static String from = "2359droid@gmail.com";
    static String subject = "DBS Smiley Logs";
    public MailSenderAsyncTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Boolean doInBackground(Void... params) {


        try {
//            GMailSender sender = new GMailSender("2359droid@gmail.com", "diod2359");
//            sender.sendMail("ARS",
//                    "This is Body HEELO WORLD",
//                    "2359droid@gmail.com",
//                    "razelsoco@gmail.com");

            Mail m = new Mail(from, "diod2359");
            m.setTo(toArr);
            m.setFrom(from);
            m.setSubject(subject);
            m.setBody("Device ID : "+FileLogger.getDeviceId(mContext));

            m.addAttachment(FileLogger.getInstance().getFile().getAbsolutePath());

           // Toast.makeText(mContext, "Email was sent successfully.", Toast.LENGTH_LONG).show();
            if(m.send())
                FileLogger.getInstance().writeLogs("Email was sent successfully.");
            else {
                FileLogger.getInstance().writeLogs("Email was not sent.");
                return false;
            }
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
            // Toast.makeText(mContext, "There was a problem   sending the email.", Toast.LENGTH_LONG).show();
            FileLogger.getInstance().writeLogs("There was a problem   sending the email. Error = "+e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if(aBoolean){
            // Delete only after successful sending of logs
            Calendar cal = Calendar.getInstance();
            if((cal.get(Calendar.HOUR_OF_DAY)==9 && cal.get(Calendar.MINUTE) >= 30)
                    || cal.get(Calendar.HOUR_OF_DAY) == 15) {
                FileLogger.getInstance().writeLogs("Deleting log file... "+FileLogger.getCurrentDateTime(cal));
                if(FileLogger.getInstance().getFile().delete()){
                    FileLogger.getInstance().writeLogs("Log file deleted successfully... ");
                }else{
                    FileLogger.getInstance().writeLogs("ERROR Log file NOT deleted");
                }
            }
        }
    }
}
