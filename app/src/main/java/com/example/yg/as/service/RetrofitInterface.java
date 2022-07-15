package com.example.yg.as.service;

import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface RetrofitInterface {
    @GET
    Call<ResponseBody> getData(@Url String url, @Query("barcode") String barcode);
}
