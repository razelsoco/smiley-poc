package dbs.smileytown.poc.network;

import dbs.smileytown.poc.utils.FileLogger;
import retrofit.Call;
import retrofit.Callback;

/**
 * Created by razelsoco on 4/2/16.
 */
public abstract class CallbackWithRetry<T> implements Callback<T> {

    private static final int TOTAL_RETRIES = 2;
    private static final String TAG = CallbackWithRetry.class.getSimpleName();
    private final Call<T> call;
    private int retryCount = 0;

    public CallbackWithRetry(Call<T> call) {
        this.call = call;
    }

    @Override
    public void onFailure(Throwable t) {
        //Log.e(TAG, t.getLocalizedMessage());
        t.printStackTrace();
        FileLogger.getInstance().writeLogs("ERROR downloading balance data file error throwable  t.getMessage(): " + t.getLocalizedMessage());

        if(t.getStackTrace() != null)
            FileLogger.getInstance().writeLogs("ERROR exception => " + t.getStackTrace()[0].toString());

        if (retryCount++ < TOTAL_RETRIES) {
            //Log.v(TAG, "Retrying... (" + retryCount + " out of " + TOTAL_RETRIES + ")");
            FileLogger.getInstance().writeLogs("RETRY downloading balance data file.....");
            retry();
        }
    }

    private void retry() {
        call.clone().enqueue(this);
    }
}