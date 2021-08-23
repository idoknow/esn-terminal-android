package com.soulter.esndroid;

import java.io.Serializable;

/*
Author : Soulter
2021© Copyright reserved
 */


public class MsgBean implements Serializable {
    String username;
    String fromUser;
    String time;
    String title;
    String content;
    int msgId;

    public MsgBean(String username,String title,String content,String time,String fromUser,int msgId){
        this.username = username;
        this.content = content;
        this.title = title;
        this.time = time;
        this.fromUser = fromUser;
        this.msgId = msgId;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public int getMsgId() {
        return msgId;
    }
}
