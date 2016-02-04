package dbs.smileytown.poc.network;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.Retrofit;

/**
 * Created by razelsoco on 26/1/16.
 */
public class ApiManager {
    //static final Retrofit RETROFIT = new Retrofit.Builder().baseUrl("https://www.dropbox.com").client().build();
    static ApiService API_SERVICE;//=RETROFIT.create(ApiService.class);

    public static ApiService getApiService(){
        if(API_SERVICE == null){
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
            okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
            Retrofit RETROFIT = new Retrofit.Builder().baseUrl("https://www.dropbox.com").client(okHttpClient).build();
            //Retrofit RETROFIT = new Retrofit.Builder().baseUrl("https://www.dropbox.com").build();
            API_SERVICE=RETROFIT.create(ApiService.class);
        }

        return API_SERVICE;
    }
}
