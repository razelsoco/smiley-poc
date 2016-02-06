package dbs.smileytown.poc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.utils.AtrUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import dbs.smileytown.poc.email.GMailSender;
import dbs.smileytown.poc.email.MailSenderAsyncTask;
import dbs.smileytown.poc.fragment.TransactionFragment;
import dbs.smileytown.poc.provider.Provider;
import dbs.smileytown.poc.receiver.FileDownloader;
import dbs.smileytown.poc.task.SimpleTask;
import dbs.smileytown.poc.utils.BalanceData;
import dbs.smileytown.poc.utils.ExcelParser;
import dbs.smileytown.poc.utils.FileLogger;
import dbs.smileytown.poc.utils.NFCUtils;
import fr.devnied.bitlib.BytesUtils;

public class HomeActivity extends AppCompatActivity {

    private static final float INITIAL_BALANCE = 5.00f;
    private static final String INITIAL_BALANCE_STR = "$5.00";
    private static final int MAX_TRANSACTIONS_TO_DISPLAY = 10;

    /**
     * IsoDep provider
     */
    private Provider mProvider = new Provider();
    private byte[] lastAts;

    private EmvCard mReadCard;
    private NFCUtils mNfcUtils;
    private InactivityTimer mTimer;
    private List<EmvTransactionRecord> mTransactionRecordsToday = new ArrayList<EmvTransactionRecord>();
    private Date dateToday = new Date();

    //homepage
    private View layoutTapCard;
    private ProgressDialog mDialog;
    private TextView tvMessage;
    private String mErrorMsg;

    //transaction page
    private TextView tvBalance;
    private TransactionFragment mTransactionFragment;
    private View btBack;
    private TextView tvUpdateDate;


    //private final MediaPlayer mMediaPlayer = new MediaPlayer();
    ExcelParser mExcelFileParser;
    FileLogger mFileLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mNfcUtils = new NFCUtils(this);
        layoutTapCard = findViewById(R.id.layout_tap_card);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        tvMessage = (TextView) findViewById(R.id.tv_error_message);
        btBack = findViewById(R.id.bt_back);
        tvUpdateDate = (TextView) findViewById(R.id.tv_date);

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
//        tvMessage.setClickable(true);
//        tvMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new MailSenderAsyncTask(HomeActivity.this).execute();
//            }
//        });
//        mTransactionFragment = TransactionFragment.newInstance(mTransactionRecordsToday);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.detail_container, mTransactionFragment).commit();

        mTimer  = new InactivityTimer(5000,1000);
        Log.d(HomeActivity.class.getName(), "ST oncreate");

        // Read card on launch
        if (getIntent().getAction() == NfcAdapter.ACTION_TECH_DISCOVERED) {
            onNewIntent(getIntent());
        }

        //mExcelFileParser.parse(this);
        mExcelFileParser = ExcelParser.getInstance();
        mFileLogger = FileLogger.getInstance();
        //initDropbox();
        mFileLogger.writeLogs("App start");
        getFile();
        //AlarmScheduler.scheduleAlarmForFileDownload(this);
        //FileLogger.getInstance().getFile().delete();
    }

    @Override
    protected void onResume() {


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


        super.onResume();

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

            new SimpleTask() {

                IsoDep mtagComm;
                boolean exception;
                private EmvCard mCard;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mErrorMsg = null;
                    // Show dialog
                    if (mDialog == null) {
                        mDialog = ProgressDialog.show(HomeActivity.this, "Reading in progress..",
                                "Please do not remove or move card during reading.", true, false);
                    } else {
                        mDialog.show();
                    }
                }

                @Override
                public void doInBackground() {

                    mtagComm = IsoDep.get(mTag);
                    if (mtagComm == null) {
                        //Toast.makeText(HomeActivity.this, "Error reading card!", Toast.LENGTH_SHORT).show();
                        //Snackbar.make(root, "Please do not move card, try again!", Snackbar.LENGTH_SHORT).show();
                        mErrorMsg = getString(R.string.err_do_not_move);//"Please do not move card,\ntry again!";
                        return;
                    }

                    exception = false;

                    try {
                        mReadCard = null;

                        //open connection
                        mtagComm.connect();
                        lastAts = getAts(mtagComm);

                        mProvider.setTagCom(mtagComm);

                        EmvParser parser = new EmvParser(mProvider, true);
                        mCard = parser.readEmvCard();
                        if (mCard != null) {
                            mCard.setAtrDescription(extractAtsDescription(lastAts));
                        }

                    } catch (IOException e) {
                        exception = true;
                    } finally {
                        IOUtils.closeQuietly(mtagComm);
                    }
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);

                    if (mDialog != null) {
                        mDialog.cancel();
                    }

                    if (!exception) {

                        if (mCard != null) {
                            if (!TextUtils.isEmpty(mCard.getCardNumber())) {
                                mReadCard = mCard;
                            } else if (mCard.isNfcLocked()) {
                                mErrorMsg = getString(R.string.err_card_nonreadable);//"NFC is locked on your card";
                            }
                        } else {
                            mErrorMsg = getString(R.string.err_card_nonreadable);//"Unknown card";
                        }
                    }else{
                        mErrorMsg = getString(R.string.err_do_not_move);
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

        mTransactionRecordsToday.clear();


        if(mReadCard != null && !TextUtils.isEmpty(mReadCard.getCardNumber())) {
            //beepSound();

            String card = mReadCard.getCardNumber();
            BalanceData data = mExcelFileParser.getBalanceDataMap().get(card.substring(card.length()-4, card.length()));
            if(data != null) {
                layoutTapCard.setVisibility(View.INVISIBLE);

                //==========OLD CODE START==============

////            for testing
//            if (mReadCard.getListTransactions() != null){
//               mTransactionRecordsToday = mReadCard.getListTransactions();
//               mTransactionRecordsToday.addAll(mReadCard.getListTransactions());
//            }
//
//
//            //filter records today
//            setTodaysTransactions();
//
//            float balance = INITIAL_BALANCE - getTotalTransactionAmount(mTransactionRecordsToday);
//            tvBalance.setText(String.format("$%.2f", balance));
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTransactionFragment.refreshTransactions(mTransactionRecordsToday);
//                }
//            });

                //==========OLD CODE END==============

                //==========NEW CODE START================
                //new code is reading balance from file
                tvUpdateDate.setText("As at "+data.date);
                tvBalance.setText(data.balance);
                //==========NEW CODE END==================
            }else{
                mErrorMsg = getString(R.string.err_card_nonreadable);
                layoutTapCard.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(mErrorMsg)) {
                    showErrorMessage(mErrorMsg);
                }
            }

        }else{
            mFileLogger.writeLogs("READ ERROR : "+ mErrorMsg);
            layoutTapCard.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(mErrorMsg)) {
                showErrorMessage(mErrorMsg);
            }
        }
        mTimer.cancel();
        mTimer.start();
    }

    private void setTodaysTransactions(){
        dateToday.setTime(System.currentTimeMillis());

        if(mReadCard.getListTransactions() != null && mReadCard.getListTransactions().size() > 0){
            for(int i =0; i < mReadCard.getListTransactions().size(); i++){
                EmvTransactionRecord transactionRecord = mReadCard.getListTransactions().get(i);

                if (isDateToday(transactionRecord.getDate())) {
                    this.mTransactionRecordsToday.add(transactionRecord);
                }

                if(mTransactionRecordsToday.size() == MAX_TRANSACTIONS_TO_DISPLAY)
                    break;
            }
        }

    }

    private boolean isDateToday(Date recordDate){
        Calendar calToday = Calendar.getInstance();
        Calendar calRecord = Calendar.getInstance();
        calToday.setTime(dateToday);
        calRecord.setTime(recordDate);
        //Log.d(HomeActivity.class.getName(),"ST Year : today = "+calToday.get(Calendar.YEAR) +" transaction = "+calRecord.get(Calendar.YEAR));
        //Log.d(HomeActivity.class.getName(),"ST Day of Year : today = "+calToday.get(Calendar.DAY_OF_YEAR) +" transaction = "+calRecord.get(Calendar.DAY_OF_YEAR));
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
        if(mTimer!= null) {
            mTimer.cancel();
            mTimer.start();
        }
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
        layoutTapCard.setVisibility(View.VISIBLE);
        showInstructionMessage();
    }

    private void showInstructionMessage(){
        tvMessage.setText(getString(R.string.instructions));
        tvMessage.setTextColor(getResources().getColor(R.color.posb_lightblue));
    }

    private void showErrorMessage(String errorMsg){
        tvMessage.setText(errorMsg);
        tvMessage.setTextColor(getResources().getColor(R.color.posb_grey));
    }


//    private void beepSound(){
//        if(mMediaPlayer.isPlaying())
//        {
//            mMediaPlayer.stop();
//        }
//
//        try {
//            mMediaPlayer.reset();
//            AssetFileDescriptor afd;
//            afd = getAssets().openFd("beep.mp3");
//            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//            mMediaPlayer.prepare();
//            mMediaPlayer.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    private void onBack() {
        mTimer.cancel();
        resetUI();
    }

    private void getFile() {
        if(FileDownloader.getFile(this).exists()){
            //Log.d("smiley", "Data file exists.. parsing data from file...");
            mFileLogger.writeLogs("Balance data file exists.. parsing balance data from file...");
            mExcelFileParser.parse(this);
        }

        FileDownloader.downloadFile(this);


    }

//    private void scheduleFileDownload(){
//        Timer timer = new Timer();
//
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                getFile();
//            }
//        };
//
//
//        //get delay : current minute -> 30th minute or 0th minute
//        int delay;
//        Calendar c = Calendar.getInstance();
//        int minute = c.get(Calendar.MINUTE);
//
//        if(minute > 0 && minute < 30 )
//            delay = 30 - minute;
//        else if(minute > 30 && minute < 59)
//            delay = (59 - minute) + 1;
//        else
//            delay = 5;
//
//        delay = delay * 1000;
//
//
//        //timer.schedule(task,delay,1000*60*30);
//
//    }

}

