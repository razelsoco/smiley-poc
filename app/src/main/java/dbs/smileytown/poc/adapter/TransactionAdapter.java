package dbs.smileytown.poc.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.devnied.emvnfccard.model.EmvTransactionRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import dbs.smileytown.poc.R;


/**
 * Created by razelsoco on 16/12/15.
 */
public class TransactionAdapter extends BaseAdapter {
    private List<EmvTransactionRecord> mTransactionRecord;
    private static final String transactionPrefix="Transaction ";

    public TransactionAdapter(List<EmvTransactionRecord> mTransactionRecord) {
        this.mTransactionRecord = mTransactionRecord;
    }

    @Override
    public int getCount() {
        if(mTransactionRecord != null)
            return mTransactionRecord.size();

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mTransactionRecord.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmvTransactionRecord record = mTransactionRecord.get(position);
        if(convertView == null) {
            convertView  = View.inflate(parent.getContext(), R.layout.item_transaction, null);
            ViewHolder vh = new ViewHolder();
            //vh.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            vh.tvAmount = (TextView) convertView.findViewById(R.id.tv_amount);
            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();

        //SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        //vh.tvDate.setText(format.format(record.getDate()));
        //vh.tvDate.setText(transactionPrefix+(position+1));

        /*
        String amount;
        if (record.getCurrency() != null) {
            amount = record.getCurrency().format(record.getAmount().longValue());
        } else {
            amount = String.valueOf(record.getAmount());
        }*/

        float amount = (float)record.getAmount().longValue() / 100;
        vh.tvAmount.setText("-"+String.format("$%.2f",amount));

        if(position % 2 > 0){
            convertView.setBackgroundResource(R.drawable.bg_transaction_item);
        }else
            convertView.setBackgroundColor(Color.WHITE);

        return convertView;
    }

    public void setTransactions(List<EmvTransactionRecord> transactions) {
        this.mTransactionRecord.clear();
        this.mTransactionRecord.addAll(transactions);
    }

    public class ViewHolder{
        public TextView tvAmount;
        //public TextView tvDate;
    }
}
