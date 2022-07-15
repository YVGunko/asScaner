package com.example.yg.as.Classes;

public class Sticker {
    private Sticker() {
        super();
    }
    private Long idSticker ; //
    private int quantity;
    private Long idDoc;
    private Long idQRCode;

    public static final String TABLE = "Sticker";
    public static final String Column_id = "_id";
    public static final String Column_orderTrace = "orderTrace";
    public static final String Column_quantity = "quantity";
    public static final String Column_orderTraceDetail = "orderTraceDetail";


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getIdSticker() {
        return idSticker;
    }

    public void setIdSticker(Long idSticker) {
        this.idSticker = idSticker;
    }

    public Long getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(Long idDoc) {
        this.idDoc = idDoc;
    }

    public Long getIdQRCode() {
        return idQRCode;
    }

    public void setIdQRCode(Long idQRCode) {
        this.idQRCode = idQRCode;
    }

    public Sticker(Long idSticker, int quantity, Long idDoc, Long idQRCode) {
        this.idSticker = idSticker;
        this.quantity = quantity;
        this.idDoc = idDoc;
        this.idQRCode = idQRCode;
    }
}
