package dbs.smileytown.poc.utils;

/**
 * Created by razelsoco on 26/1/16.
 */
public class BalanceData {
    public String cardNumber;
    public String balance;
    public String date;

    @Override
    public String toString() {
        return "BalanceData{" +
                "balance='" + balance + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
