package com.example.yg.as.Classes;

public class Defs {
    private String _Host_IP;
    private String _Port;
    private String Device_id;
    private Contragent contragent;

    public static final String table_Defs = "Defs";
    public static final String COLUMN_Device_id = "Device_id";
    public static final String COLUMN_contragent = "contragent";
    public static final String COLUMN_Host_IP = "Host_IP";
    public static final String COLUMN_Port = "Port";

    public Defs(String _Host_IP, String _Port, String device_id, Contragent contragent) {
        this._Host_IP = _Host_IP;
        this._Port = _Port;
        this.Device_id = device_id;
        this.contragent = contragent;
    }

    public Defs(String _Host_IP, String _Port, Contragent contragent) {
        this._Host_IP = _Host_IP;
        this._Port = _Port;
        this.contragent = contragent;
    }
    public String get_Host_IP() {
        return _Host_IP;
    }

    public void set_Host_IP(String _Host_IP) {
        this._Host_IP = _Host_IP;
    }

    public String get_Port() {
        return _Port;
    }

    public void set_Port(String _Port) {
        this._Port = _Port;
    }

    public String getDevice_id() {
        return Device_id;
    }

    public void setDevice_id(String device_id) {
        Device_id = device_id;
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }

    public String getUrl() {return "http://" + this._Host_IP+":"+this._Port;}
}
