package com.adnan.photoblog;

import java.util.Date;

public class Notifications {
    private String toUserName;//user who receive like or comment
    private String desc1;
    private String fromUserNameImageUri;
    private String  fromUserName;//user who do like or comment
    private String type;

    public Notifications() {
    }

    public Notifications(String toUserName, String desc1, String fromUserNameImageUri, String fromUserName, String type) {
        this.toUserName = toUserName;
        this.desc1 = desc1;
        this.fromUserNameImageUri = fromUserNameImageUri;
        this.fromUserName = fromUserName;
        this.type = type;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getDesc1() {
        return desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getFromUserNameImageUri() {
        return fromUserNameImageUri;
    }

    public void setFromUserNameImageUri(String fromUserNameImageUri) {
        this.fromUserNameImageUri = fromUserNameImageUri;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}