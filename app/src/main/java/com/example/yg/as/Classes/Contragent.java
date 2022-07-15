package com.example.yg.as.Classes;

public class Contragent {
    @SuppressWarnings("unused")
    private Contragent() {
        super();
    }

    private Long id;
    private String name;
    private int sequence;

    public static final String TABLE = "Contragent";
    public static final String Column_id = "_id";
    public static final String Column_name = "name";
    public static final String Column_sequence = "sequence";

    public Contragent(Long id, String name, int sequence) {
        this.id = id;
        this.name = name;
        this.sequence = sequence;
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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
