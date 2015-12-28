package dbs.smileytown.poc.adapter;

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
            vh.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            vh.tvAmount = (TextView) convertView.findViewById(R.id.tv_amount);
            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();

        SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        vh.tvDate.setText(format.format(record.getDate()));

        /*
        String amount;
        if (record.getCurrency() != null) {
            amount = record.getCurrency().format(record.getAmount().longValue());
        } else {
            amount = String.valueOf(record.getAmount());
        }*/

        float amount = (float)record.getAmount().longValue() / 100;
        vh.tvAmount.setText(String.format("$%.2f",amount));

        return convertView;
    }

    public void setTransactions(List<EmvTransactionRecord> transactions) {
        this.mTransactionRecord = transactions;
    }

    public class ViewHolder{
        public TextView tvAmount;
        public TextView tvDate;
    }
}
