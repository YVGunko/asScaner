package com.example.yg.as.service;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class RetrofitClass {
    private static String baseUrl;

    public void setUrl(String baseUrl){
        this.baseUrl =baseUrl;
    }

    public static RetrofitInterface getApi() throws Exception {
        if (baseUrl.isEmpty() || baseUrl==null){
            throw new Exception("baseUrl for api call in retrofit class is not set");
        }
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();

        RetrofitInterface RetInt = retrofit.create(RetrofitInterface.class);
        return RetInt;
    }
}
