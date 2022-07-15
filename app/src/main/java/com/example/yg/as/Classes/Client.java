package com.example.yg.as.Classes;

public class Client {
    @SuppressWarnings("unused")
    private Client() {
        super();
    }

    private Long id;
    private String name;

    public static final String TABLE = "Client";
    public static final String Column_id = "_id";
    public static final String Column_name = "name";

    public Client (Long id, String name)	{
        this.id = id;
        this.name = name;
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
}