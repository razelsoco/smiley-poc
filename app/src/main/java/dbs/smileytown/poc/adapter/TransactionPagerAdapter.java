package dbs.smileytown.poc.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.github.devnied.emvnfccard.model.EmvTransactionRecord;

import java.util.ArrayList;
import java.util.List;

import dbs.smileytown.poc.fragment.TransactionFragment;


/**
 * Created by razelsoco on 18/12/15.
 */
public class TransactionPagerAdapter extends FragmentStatePagerAdapter {
    List<EmvTransactionRecord> allTransactions;
    static final int TRANSACTIONS_PER_PAGE = 10;

    List<TransactionFragment> mFragments = new ArrayList<TransactionFragment>();

    public TransactionPagerAdapter(FragmentManager fm, List<EmvTransactionRecord> allTransactions) {
        super(fm);
        this.allTransactions = allTransactions;

    }

    @Override
    public Fragment getItem(int position) {
        List<EmvTransactionRecord> records = null;

        if(allTransactions !=null) {
            int start = position * TRANSACTIONS_PER_PAGE;
            int end = start + TRANSACTIONS_PER_PAGE;
            if (end > allTransactions.size())
                end = allTransactions.size();

            records = allTransactions.subList(start, end);
        }

        TransactionFragment f;
        Log.d("TransactionPagerAdapter", "fragments="+mFragments.size() + "; position ="+position);
        Log.d("TransactionPagerAdapter", "records="+records );
        if(position < mFragments.size()){
            Log.d("TransactionPagerAdapter", "get transaction fragment from list" );
            f = mFragments.get(position);
            f.refreshTransactions(records);
        }else{
            Log.d("TransactionPagerAdapter", "new instance of transaction fragment" );
            f = TransactionFragment.newInstance(records);
            mFragments.add(f);
        }
        return f;
    }



    @Override
    public int getCount() {
        int count = 1;
        if(allTransactions != null) {
            count = (allTransactions.size() / TRANSACTIONS_PER_PAGE) + (allTransactions.size() % TRANSACTIONS_PER_PAGE > 0 ? 1 : 0);
        }

        return count;
    }

    public void setAllTransactions(List<EmvTransactionRecord> allTransactions){
        this.allTransactions = allTransactions;
        Log.d("TransactionPagerAdapter", "allTransactions="+allTransactions.size() );
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
