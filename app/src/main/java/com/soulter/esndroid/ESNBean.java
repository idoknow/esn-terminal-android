package com.soulter.esndroid;

import java.io.Serializable;
import java.util.List;

import conn.ESNSession;

/*
Author : Soulter
2021© Copyright reserved
 */


public class ESNBean implements Serializable {
    String username;
    ESNSession esnSession;
    List<String> types;
    public ESNBean(String username,ESNSession esnSession,List<String> types){
        this.username = username;
        this.esnSession = esnSession;
        this.types = types;
    }

    public ESNSession getEsnSession() {
        return esnSession;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getTypes() {
        return types;
    }
}
