package dbs.smileytown.poc.network;


import com.squareup.okhttp.Callback;
import com.squareup.okhttp.ResponseBody;


import retrofit.Call;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Streaming;

/**
 * Created by razelsoco on 26/1/16.
 */
public interface ApiService {
    //https://www.dropbox.com/s/qefwfl7wjisbbg4/txn-excel-sample1.xlsx?dl=0
    //https://www.dropbox.com/s/g01ne347ts2nur6/txn-excel-sample.xlsx?dl=0
    String url = "/s/g01ne347ts2nur6/txn-excel-sample.xlsx?dl=1";

    @GET(url)
    @Streaming
    Call<ResponseBody> getFile();

}
