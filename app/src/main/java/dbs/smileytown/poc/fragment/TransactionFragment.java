package dbs.smileytown.poc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.devnied.emvnfccard.model.EmvTransactionRecord;

import java.util.ArrayList;
import java.util.List;

import dbs.smileytown.poc.R;
import dbs.smileytown.poc.adapter.TransactionAdapter;


/**
 * Created by razelsoco on 18/12/15.
 */
public class TransactionFragment extends Fragment {

    List<EmvTransactionRecord> mTransactions;
    TransactionAdapter mAdapter;
    ListView lvTransactions;
    TextView tv_empty;

    public static TransactionFragment newInstance(List<EmvTransactionRecord> transactionRecords){
        TransactionFragment f = new TransactionFragment();
        f.addAllTransactions(transactionRecords);

        return f;

    }

    public void addAllTransactions(List<EmvTransactionRecord> transactionRecords){
        if(mTransactions == null)
            mTransactions = new ArrayList<EmvTransactionRecord>();
        else
            mTransactions.clear();

        if(transactionRecords != null)
            mTransactions.addAll(transactionRecords);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        lvTransactions = (ListView) view.findViewById(R.id.lv_transactions);
        mAdapter = new TransactionAdapter(mTransactions);
        tv_empty = (TextView) view.findViewById(R.id.empty_view);
        lvTransactions.setEmptyView(tv_empty);
        lvTransactions.setAdapter(mAdapter);

    }

    public void refreshTransactions(List<EmvTransactionRecord> transactions){
        mTransactions = transactions;
        mAdapter.setTransactions(mTransactions);
        mAdapter.notifyDataSetChanged();
    }

}
