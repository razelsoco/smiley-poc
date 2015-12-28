package dbs.smileytown.poc.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by razelsoco on 17/12/15.
 */
public abstract class SimpleTask extends AsyncTask<Void, Void, Object> {

    private ProgressDialog pd;
    private Context c;

    public SimpleTask(Context c) {
        this.c = c;
    }

    @Override
    protected Object doInBackground(Void... params) {
        Object result=null;
        try {
            doInBackground();
        }catch (Exception e){
            result = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        pd.dismiss();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pd == null) {
            pd = new ProgressDialog(c);
            pd.setMessage("Reading card...");
            pd.setCancelable(false);
        }
        pd.show();
    }

    public abstract void doInBackground();
}
