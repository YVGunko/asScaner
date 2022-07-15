package com.example.yg.as.Classes;

import java.util.Date;

public class OrderTraceDetail {
    @SuppressWarnings("unused")
    private OrderTraceDetail() {
        super();
    }

    private Long id;
    private OrderTrace orderTrace;
    private int quantity;
    private Model model ;
    private Sizing sizing;
    private Long numberOfOrder;

    public static final String TABLE = "OrderTraceDetail";
    public static final String Column_id = "_id";
    public static final String Column_quantity = "quantity";
    public static final String Column_number = "numberOfOrder";
    public static final String Column_sizing = "sizing";
    public static final String Column_model = "model";
    public static final String Column_orderTrace = "orderTrace";

    public OrderTraceDetail (Long id, Long parent_id, Long numberOfOrder,
                             Long model_id, String model_name,
                             Long sizing_id, String sizing_name, int quantity)	{
        this.id = id;
        this.orderTrace = new OrderTrace (parent_id, numberOfOrder, new Long(0), "",
                new Long(0), new Long(0), new Long(0), new Long(0), new Long(0));
        this.numberOfOrder = numberOfOrder;
        this.model = new Model(model_id, model_name);
        this.sizing = new Sizing(sizing_id, sizing_name, quantity);
        this.quantity = quantity;
    }

    public OrderTraceDetail (Long id, Long parent_id, Long numberOfOrder,
                             Long model_id,
                             Long sizing_id, int quantity)	{
        this.id = id;
        this.orderTrace = new OrderTrace (parent_id, numberOfOrder, new Long(0), "",
                new Long(0), new Long(0), new Long(0), new Long(0), new Long(0));
        this.numberOfOrder = numberOfOrder;
        this.model = new Model(model_id, "");
        this.sizing = new Sizing(sizing_id, "", quantity);
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderTrace getOrderTrace() {
        return orderTrace;
    }

    public void setOrderTrace(OrderTrace orderTrace) {
        this.orderTrace = orderTrace;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Sizing getSizing() {
        return sizing;
    }

    public void setSizing(Sizing sizing) {
        this.sizing = sizing;
    }

    public Long getNumberOfOrder() {
        return numberOfOrder;
    }

    public void setNumberOfOrder(Long numberOfOrder) {
        this.numberOfOrder = numberOfOrder;
    }
}
