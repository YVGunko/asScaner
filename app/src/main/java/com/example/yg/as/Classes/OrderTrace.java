package com.example.yg.as.Classes;

import java.util.Date;

public class OrderTrace {
    @SuppressWarnings("unused")
    private OrderTrace() {
        super();
    }
    private Long id;
    private String nameOfTrace;
    private Long dateOfTrace;
    private Contragent sender ;
    private Contragent receiver ;
    private Client client ;
    private Brand brand ;
    private Season season;
    private Long numberOfOrder;

    public static final String TABLE = "OrderTrace";
    public static final String Column_id = "_id";
    public static final String Column_name = "nameOfTrace";
    public static final String Column_date = "dateOfTrace";
    public static final String Column_number = "numberOfOrder";
    public static final String Column_sender = "sender";
    public static final String Column_receiver = "receiver";
    public static final String Column_client = "client";
    public static final String Column_brand = "brand";
    public static final String Column_season = "season";


    public OrderTrace (Long id, Long numberOfOrder, Long dateOfTrace, String nameOfTrace,
                       Long sender_id, String sender_name, int sender_sequence,
                       Long receiver_id, String receiver_name, int receiver_sequence,
                       Long client_id, String client_name,
                       Long brand_id, String brand_name,
                       Long season_id, String season_name)	{
        this.id = id;
        this.numberOfOrder = numberOfOrder;
        this.dateOfTrace = dateOfTrace;
        this.nameOfTrace = nameOfTrace;
        this.sender = new Contragent(sender_id, sender_name, sender_sequence);
        this.receiver = new Contragent(receiver_id, receiver_name, receiver_sequence);
        this.client = new Client(client_id, client_name);
        this.brand = new Brand(brand_id, brand_name);
        this.season = new Season(season_id, season_name);
    }

    public OrderTrace (Long id, Long numberOfOrder, Long dateOfTrace, String nameOfTrace,
                       Long sender_id,
                       Long receiver_id,
                       Long client_id,
                       Long brand_id,
                       Long season_id)	{
        this.id = id;
        this.numberOfOrder = numberOfOrder;
        this.dateOfTrace = dateOfTrace;
        this.nameOfTrace = nameOfTrace;
        this.sender = new Contragent(sender_id, "", 0);
        this.receiver = new Contragent(receiver_id, "", 0);
        this.client = new Client(client_id, "");
        this.brand = new Brand(brand_id, "");
        this.season = new Season(season_id, "");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameOfTrace() {
        return nameOfTrace;
    }

    public void setNameOfTrace(String nameOfTrace) {
        this.nameOfTrace = nameOfTrace;
    }

    public Long getDateOfTrace() {
        return dateOfTrace;
    }

    public void setDateOfTrace(Long dateOfTrace) {
        this.dateOfTrace = dateOfTrace;
    }

    public Contragent getSender() {
        return sender;
    }

    public void setSender(Contragent sender) {
        this.sender = sender;
    }

    public Contragent getReceiver() {
        return receiver;
    }

    public void setReceiver(Contragent receiver) {
        this.receiver = receiver;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Long getNumberOfOrder() {
        return numberOfOrder;
    }

    public void setNumberOfOrder(Long numberOfOrder) {
        this.numberOfOrder = numberOfOrder;
    }
}
