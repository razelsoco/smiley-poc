package dbs.smileytown.poc.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;

/**
 * Created by razelsoco on 16/12/15.
 */
public class NFCUtils {
    private final NfcAdapter mNfcAdapter;
    private final PendingIntent mPendingIntent;
    private final Activity mAcitvity;

    private static final IntentFilter[] INTENT_FILTER = new IntentFilter[] {new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
    private static final String [][] TECH_LIST = new String[][]{{IsoDep.class.getName()}};

    public static boolean isNFCEnabled(Context c){
        boolean ret;
        try {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(c);
            ret = (adapter != null && adapter.isEnabled());
        } catch(UnsupportedOperationException e){
            ret = false;
        }

        return ret;
    }

    public NFCUtils(Activity mAcitvity) {
        this.mAcitvity = mAcitvity;
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(mAcitvity);
        this.mPendingIntent = PendingIntent.getActivity(mAcitvity, 0,
                new Intent(mAcitvity, mAcitvity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0 );
    }

    public void enableForegroundDispatch(){
        if(mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(mAcitvity, mPendingIntent, INTENT_FILTER, TECH_LIST );
    }
    public void disableForegroundDispatch(){
        if(mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(mAcitvity);
    }
}
