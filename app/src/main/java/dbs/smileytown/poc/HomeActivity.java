package dbs.smileytown.poc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.utils.AtrUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import dbs.smileytown.poc.fragment.TransactionFragment;
import dbs.smileytown.poc.provider.Provider;
import dbs.smileytown.poc.task.SimpleTask;
import dbs.smileytown.poc.utils.NFCUtils;
import fr.devnied.bitlib.BytesUtils;

public class HomeActivity extends AppCompatActivity {

    private static final float INITIAL_BALANCE = 1000.00f;

    /**
     * IsoDep provider
     */
    private Provider mProvider = new Provider();
    private byte[] lastAts;

    private EmvCard mReadCard;
    private NFCUtils mNfcUtils;
    private View layoutCardDetail, layoutTapCard;
    private TextView tvBalance;//, tvCurrentPage;
    private InactivityTimer mTimer;
    //private ViewPager vpTransactions;
    //private TransactionPagerAdapter mPagerAdapter;
    private CoordinatorLayout root;
    private TransactionFragment mTransactionFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mNfcUtils = new NFCUtils(this);
        root = (CoordinatorLayout) findViewById(R.id.root);
        layoutCardDetail = findViewById(R.id.layout_card_detail);
        layoutTapCard = findViewById(R.id.layout_tap_card);
        tvBalance = (TextView) findViewById(R.id.tv_balance);

        mTransactionFragment = TransactionFragment.newInstance(mTransactionRecordsToday);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.detail_container, mTransactionFragment).commit();

        mTimer  = new InactivityTimer(10000,1000);
        Log.d(HomeActivity.class.getName(), "ST oncreate");
    }

    /*
    private void nextPage() {
        int nextPage = vpTransactions.getCurrentItem()+1;
        vpTransactions.setCurrentItem(nextPage);
        if(nextPage == (mPagerAdapter.getCount() - 1))
            btnNext.setEnabled(false);

        if(nextPage == 1)
            btnPrev.setEnabled(true);


        tvCurrentPage.setText(++nextPage+"/"+mPagerAdapter.getCount());

    }

    private void prevPage() {
        int prevPage = vpTransactions.getCurrentItem()-1;
        vpTransactions.setCurrentItem(prevPage);
        if(prevPage == 0)
            btnPrev.setEnabled(false);

        if(prevPage == 0)
            btnNext.setEnabled(true);

        tvCurrentPage.setText(++prevPage+"/"+mPagerAdapter.getCount());
    } */

    @Override
    protected void onResume() {
        super.onResume();

        mNfcUtils.enableForegroundDispatch();
        if(!NFCUtils.isNFCEnabled(this)){
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setTitle("");
            alertbox.setMessage("NFC Disabled");
            alertbox.setPositiveButton("activate now", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    } else {
                        intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    }
                    dialog.dismiss();
                    startActivity(intent);
                }
            });
            alertbox.setCancelable(false);
            alertbox.show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mNfcUtils.disableForegroundDispatch();

    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        readCard(mTag);

    }

    private void readCard(final Tag mTag){
        if (mTag != null) {

            new SimpleTask(HomeActivity.this) {

                IsoDep mtagComm;
                boolean exception;

                @Override
                public void doInBackground() {

                    mtagComm = IsoDep.get(mTag);
                    if (mtagComm == null) {
                        //Toast.makeText(HomeActivity.this, "Error reading card!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(root, "Error reading card!", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    exception = false;
                    mReadCard = null;

                    try {
                        mtagComm.connect();
                        lastAts = getAts(mtagComm);
                        mProvider.setTagCom(mtagComm);

                        EmvParser parser = new EmvParser(mProvider, true);
                        mReadCard = parser.readEmvCard();
                        if (mReadCard != null) {
                            mReadCard.setAtrDescription(extractAtsDescription(lastAts));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        exception = true;
                    }
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);
                    if (exception) {
                        //Toast.makeText(HomeActivity.this, "Error reading card!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(root, "Please do not move card. Try again.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }


                    if (mReadCard != null) {
                        if (!TextUtils.isEmpty(mReadCard.getCardNumber())) {
                            Snackbar.make(root, "Success", Snackbar.LENGTH_SHORT).show();
                        } else if (mReadCard.isNfcLocked()) {
                            Snackbar.make(root, "NFC is locked on your card", Snackbar.LENGTH_SHORT).show();
                            mReadCard = null;
                        }
                    } else {
                        Snackbar.make(root, "Unknown EMV card", Snackbar.LENGTH_SHORT).show();
                    }

                    showContent();

                }
            }.execute();

        }
    }

    /**
     * Get ATS from isoDep
     *
     * @param pIso
     *            isodep
     * @return ATS byte array
     */
    private byte[] getAts(final IsoDep pIso) {
        byte[] ret = null;
        if (pIso.isConnected()) {
            // Extract ATS from NFC-A
            ret = pIso.getHistoricalBytes();
            if (ret == null) {
                // Extract ATS from NFC-B
                ret = pIso.getHiLayerResponse();
            }
        }
        return ret;
    }

    /**
     * Method used to get description from ATS
     *
     * @param pAts
     *            ATS byte
     */
    public Collection<String> extractAtsDescription(final byte[] pAts) {
        return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts));
    }

    private void showContent() {
        layoutTapCard.setVisibility(View.INVISIBLE);
        mTransactionRecordsToday.clear();


        if(mReadCard != null) {
            //for testing
            if (mReadCard.getListTransactions() != null)
                mTransactionRecordsToday = mReadCard.getListTransactions();

            //filter records today
            //setTodaysTransactions();
            Log.d(HomeActivity.class.getName(), "ST mTransactionRecordsToday after filter = " + mTransactionRecordsToday.size());
        }

        mTransactionFragment.refreshTransactions(mTransactionRecordsToday);

        float balance = INITIAL_BALANCE - getTotalTransactionAmount(mTransactionRecordsToday);
        tvBalance.setText(String.format("$%.2f", balance));

        mTimer.start();

//        if(mPagerAdapter.getCount() <= 1 ) {
//            btnNext.setEnabled(false);
//            btnPrev.setEnabled(false);
//            if(mPagerAdapter.getCount() == 0)
//                tvCurrentPage.setVisibility(View.INVISIBLE);
//            else {
//                tvCurrentPage.setText("1/" + mPagerAdapter.getCount());
//                tvCurrentPage.setVisibility(View.VISIBLE);
//            }
//        }else{
//            btnNext.setEnabled(true);
//            btnPrev.setEnabled(false);
//            tvCurrentPage.setText("1/"+mPagerAdapter.getCount());
//            tvCurrentPage.setVisibility(View.VISIBLE);
//        }

    }

    List<EmvTransactionRecord> mTransactionRecordsToday = new ArrayList<EmvTransactionRecord>();
    Date dateToday = new Date();

    private void setTodaysTransactions(){
        dateToday.setTime(System.currentTimeMillis());

        Log.d(HomeActivity.class.getName(), "ST card transactions = " + mReadCard.getListTransactions());
        if(mReadCard.getListTransactions() != null && mReadCard.getListTransactions().size() > 0){
            this.mTransactionRecordsToday.clear();
            for(int i =0; i < mReadCard.getListTransactions().size(); i++){
                EmvTransactionRecord transactionRecord = mReadCard.getListTransactions().get(i);

                if (isDateToday(transactionRecord.getDate())) {
                    this.mTransactionRecordsToday.add(transactionRecord);
                }
            }
        }

    }

    private boolean isDateToday(Date recordDate){
        Calendar calToday = Calendar.getInstance();
        Calendar calRecord = Calendar.getInstance();
        calToday.setTime(dateToday);
        calRecord.setTime(recordDate);
        Log.d(HomeActivity.class.getName(),"ST Year : today = "+calToday.get(Calendar.YEAR) +" transaction = "+calRecord.get(Calendar.YEAR));
        Log.d(HomeActivity.class.getName(),"ST Day of Year : today = "+calToday.get(Calendar.DAY_OF_YEAR) +" transaction = "+calRecord.get(Calendar.DAY_OF_YEAR));
        return (calToday.get(Calendar.YEAR) == calRecord.get(Calendar.YEAR))
                && (calToday.get(Calendar.DAY_OF_YEAR)==calRecord.get(Calendar.DAY_OF_YEAR));

    }

    private float getTotalTransactionAmount(List<EmvTransactionRecord> transactionRecordsToday){
        float totalTransactionAmount = 0;
        if(transactionRecordsToday != null && transactionRecordsToday.size() > 0){

            for(int i =0; i < transactionRecordsToday.size(); i++){
                EmvTransactionRecord transactionRecord = transactionRecordsToday.get(i);
                float amount=0;
                if (transactionRecord.getCurrency() != null) {
                    amount = (float)transactionRecord.getAmount().longValue()/ 100;
                }
                totalTransactionAmount += amount;
            }
        }

        return totalTransactionAmount;
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mTimer.cancel();
        mTimer.start();
    }

    public class InactivityTimer extends CountDownTimer {

        public InactivityTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            resetUI();
        }
    }

    private void resetUI(){
        //layoutCardDetail.setVisibility(View.INVISIBLE);
        layoutTapCard.setVisibility(View.VISIBLE);
    }
}
