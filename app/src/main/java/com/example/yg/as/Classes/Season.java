package com.example.yg.as.Classes;

public class Season {
    @SuppressWarnings("unused")
    private Season() {
        super();
    }

    private Long id;
    private String name;

    public static final String TABLE = "Season";
    public static final String Column_id = "_id";
    public static final String Column_name = "name";

    public Season (Long id, String name)	{
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
