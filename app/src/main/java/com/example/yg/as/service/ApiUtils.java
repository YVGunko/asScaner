package com.example.yg.as.service;

public class ApiUtils {
    public static OrderTraceService getOrderService(String baseUrl) {
        return RetrofitClient.getClient(baseUrl).create(OrderTraceService.class);
    }
}
