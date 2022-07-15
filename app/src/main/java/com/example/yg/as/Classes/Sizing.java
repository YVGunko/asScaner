package com.example.yg.as.Classes;

public class Sizing {
    @SuppressWarnings("unused")
    private Sizing() {
        super();
    }
    private Long id;
    private String name;
    private int quantity;

    public static final String TABLE = "Sizing";
    public static final String Column_id = "_id";
    public static final String Column_name = "name";
    public static final String Column_quantity = "quantity";

    public Sizing (Long id, String name, int quantity)	{
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
