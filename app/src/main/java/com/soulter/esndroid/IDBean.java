package com.soulter.esndroid;

import java.util.List;

/*
Author : Soulter
2021© Copyright reserved
 */


public class IDBean {
    public String userName;
    public String pass;
    public List<String> types;

    public IDBean(String userName,String pass,List<String> types){
        this.userName=userName;
        this.pass=pass;
        this.types = types;
    }

    public String getUserName(){
        return userName;
    }
    public String getPass(){
        return pass;
    }

    public List<String> getTypes() {
        return types;
    }
}
