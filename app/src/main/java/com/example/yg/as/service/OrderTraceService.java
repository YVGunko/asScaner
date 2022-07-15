package com.example.yg.as.service;

import com.example.yg.as.Classes.Box;
import com.example.yg.as.Classes.Brand;
import com.example.yg.as.Classes.Client;
import com.example.yg.as.Classes.Contragent;
import com.example.yg.as.Classes.Model;
import com.example.yg.as.Classes.OrderTraceDetail;
import com.example.yg.as.Classes.Season;
import com.example.yg.as.Classes.Sizing;
import com.example.yg.as.Classes.Sticker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderTraceService {
    @GET("/checkConnection")
    Call<Object> checkConnection();

    @GET("/getStickerGreaterThan")
    Call<List<Sticker>> findStickerGreaterThan(@Query("idStickerMax") Long idStickerMax);

    @GET("/getAllRowsAndDoc")
    Call<List<OrderTraceDetail>> getAllRowsAndDoc(@Query("date") Long date);

    @GET("/getAllContragent")
    Call<List<Contragent>> getAllContragent();

    @GET("/getAllClient")
    Call<List<Client>> getAllClient();

    @GET("/getAllModel")
    Call<List<Model>> getAllModel();

    @GET("/getAllBrand")
    Call<List<Brand>> getAllBrand();

    @GET("/getAllSeason")
            Call<List<Season>> getAllSeason();

    @GET("/getAllSizing")
            Call<List<Sizing>> getAllSizing();

    @POST("/setBox")
    Call<List<Box>> postBox(@Body ArrayList<Box> box);

    @POST("/exportBox")
    Call<List<Box>> exportBox(@Body ArrayList<Box> box);

    @POST("/exportOrderTraceDetail")
    Call<List<OrderTraceDetail>> exportOrderTraceDetail(@Body ArrayList<OrderTraceDetail> orderTraceDetail);

    @GET("/getAllBox")
    Call<List<Box>> importBox();
}
