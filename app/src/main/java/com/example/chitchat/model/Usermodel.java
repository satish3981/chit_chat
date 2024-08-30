package com.example.chitchat.model;

import java.sql.Timestamp;
import java.util.Date;

public class Usermodel {
  private String phone;
  public String username;
  private Date createdTimestamp;

  private String userId;
  private String fcmToken;


  public Usermodel(){}
    public Usermodel(String phone, String username, Timestamp createdTimestamp, String userId) {
        this.phone = phone;
        this.username =  username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedTimestamp() {

      return  createdTimestamp;
    }

    public void setCreatedTimestamp(Date  createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
