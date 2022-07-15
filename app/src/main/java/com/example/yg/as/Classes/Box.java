package com.example.yg.as.Classes;

public class Box {
    @SuppressWarnings("unused")
    private Box() {
        super();
    }

    /*
r.name as receiver, m.name as model,  b.name as brand, g.name as sizing, g.quantity as quantity, c.name as client, s.name as season, d.numberOfOrder, " +
" strftime('%d-%m-%Y %H:%M:%S', o.dateOfTrace/1000, 'unixepoch', 'localtime') as dateOfTrace
" FROM OrderTrace o, OrderTraceDetail d, Contragent r, Client c, Brand b, Model m, Season s, Sizing g" +
" Where o._id=d.orderTrace and d.ROWID=" + valueOf(rowId) +
" and o.receiver=r._id and o.client=c._id and o.brand=b._id and o.season=s._id and d.sizing=g._id and d.model=m._id
        */

    private int quantity;
    private String model ; //№Модели
    private Long model_id ; //№Модели
    private String sizing; //Разм/ряд
    private Long sizing_id; //Разм/ряд
    private String numberOfOrder; //№Партии
    private String dateOfTrace; //Время приемки:
    private String sender ; //Участок от
    private Long sender_id ; //Участок от
    private String receiver ; //Участок от
    private Long receiver_id ; //Участок от
    private String client ; //Клиент
    private Long client_id ; //Клиент
    private String brand ; //Бренд
    private Long brand_id ; //Бренд
    private String season; //Сезон
    private Long season_id; //Сезон
    private String boxDesc; //Сезон
    private Long parent_id ; //OrderTrace
    private Long sentToMasterDate;
    private Long sticker;
    private Long id ; //
    
    public static final String TABLE = "Box";

    public static final String Column_id = "ROWID";
    public static final String Column_quantity = "quantity";
    public static final String Column_model = "model";
    public static final String Column_sizing = "sizing";
    public static final String Column_number = "numberOfOrder";
    public static final String Column_dateOfTrace = "dateOfTrace";
    public static final String Column_receiver = "receiver";
    public static final String Column_client = "client";
    public static final String Column_brand = "brand";
    public static final String Column_season = "season";
    public static final String Column_sender = "sender";
    public static final String Column_orderTrace = "orderTrace";
    public static final String COLUMN_sentToMasterDate = "sentToMasterDate";
    public static final String COLUMN_sticker = "sticker";


    public Box(int quantity, String model, Long model_id, String sizing, Long sizing_id, String numberOfOrder,
               String dateOfTrace, String receiver, Long receiver_id, String client, Long client_id,
               String brand, Long brand_id, String season, Long season_id, String sender, Long sender_id,
               Long parent_id, Long sentToMasterDate, Long sticker, Long id) {
        this.id = id;
        this.sticker = sticker;
        this.quantity = quantity;
        this.model = model;
        this.model_id = model_id;
        this.sizing = sizing;
        this.sizing_id = sizing_id;
        this.numberOfOrder = numberOfOrder;
        this.dateOfTrace = dateOfTrace;
        this.receiver = receiver;
        this.receiver_id = receiver_id;
        this.sender = sender;
        this.sender_id = sender_id;
        this.client = client;
        this.client_id = client_id;
        this.brand = brand;
        this.brand_id = brand_id;
        this.season = season;
        this.season_id = season_id;
        this.parent_id = parent_id;
        this.sentToMasterDate = sentToMasterDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSticker() {
        return sticker;
    }

    public void setSticker(Long sticker) {
        this.sticker = sticker;
    }

    public Long getSentToMasterDate() {
        return sentToMasterDate;
    }

    public void setSentToMasterDate(Long sentToMasterDate) {
        this.sentToMasterDate = sentToMasterDate;
    }

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getSender_id() {
        return sender_id;
    }

    public void setSender_id(Long sender_id) {
        this.sender_id = sender_id;
    }

    public Long getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(Long receiver_id) {
        this.receiver_id = receiver_id;
    }

    public Long getModel_id() {
        return model_id;
    }

    public void setModel_id(Long model_id) {
        this.model_id = model_id;
    }

    public Long getSizing_id() {
        return sizing_id;
    }

    public void setSizing_id(Long sizing_id) {
        this.sizing_id = sizing_id;
    }

    public Long getClient_id() {
        return client_id;
    }

    public void setClient_id(Long client_id) {
        this.client_id = client_id;
    }

    public Long getBrand_id() {
        return brand_id;
    }

    public void setBrand_id(Long brand_id) {
        this.brand_id = brand_id;
    }

    public Long getSeason_id() {
        return season_id;
    }

    public void setSeason_id(Long season_id) {
        this.season_id = season_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSizing() {
        return sizing;
    }

    public void setSizing(String sizing) {
        this.sizing = sizing;
    }

    public String getNumberOfOrder() {
        return numberOfOrder;
    }

    public void setNumberOfOrder(String numberOfOrder) {
        this.numberOfOrder = numberOfOrder;
    }

    public String getDateOfTrace() {
        return dateOfTrace;
    }

    public void setDateOfTrace(String dateOfTrace) {
        this.dateOfTrace = dateOfTrace;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setBoxDesc(String boxDesc) {
        this.boxDesc = boxDesc;
    }

    public String getBoxDesc (){

        boxDesc = "Участок: " + receiver +"\n";
        boxDesc += "Время приемки: " + dateOfTrace +"\n";
        boxDesc += "№Модели: " + model;
        boxDesc += ". Бренд: " + brand + "\n";
        boxDesc += "Разм/ряд: " + sizing;
        boxDesc += ". Пар/кор: " + quantity + "\n";
        boxDesc += "Клиент: " + client + "\n";
        boxDesc += "Сезон: " + season + "\n";
        boxDesc += "№Партии: " + numberOfOrder;

    return boxDesc;
    }
}
