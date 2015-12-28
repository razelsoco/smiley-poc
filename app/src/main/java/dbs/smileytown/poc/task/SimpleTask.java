package dbs.smileytown.poc.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by razelsoco on 17/12/15.
 */
public abstract class SimpleTask extends AsyncTask<Void, Void, Object> {



    @Override
    protected Object doInBackground(final Void... params) {

        Object result = null;

        try {
            doInBackground();
        } catch (Exception e) {
            result = e;
            Log.e(SimpleTask.class.getName(), e.getMessage(), e);
        }

        return result;
    }

    protected abstract void doInBackground();

}
