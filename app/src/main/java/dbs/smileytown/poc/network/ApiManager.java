package dbs.smileytown.poc.network;

import retrofit.Retrofit;

/**
 * Created by razelsoco on 26/1/16.
 */
public class ApiManager {
    static final Retrofit RETROFIT = new Retrofit.Builder().baseUrl("https://www.dropbox.com").build();
    static final ApiService API_SERVICE=RETROFIT.create(ApiService.class);

    public static ApiService getApiService(){
        return API_SERVICE;
    }
}
