package com.example.chitchat.model;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String message;
    private String messageId;
    private String senderId;
    private String fileUrl;
    private Timestamp timestamp;
    private String massageType;

    public  ChatMessageModel(){}

    public ChatMessageModel(String message, String senderId, Timestamp timestamp, String massageType) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
       // this.fileUrl = fileUrl;
        this.massageType = massageType;

    }




    public  String getMessageId(){
        return messageId;
    }
    public void setMessageId(String messageId)
    {
        this.messageId=messageId;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {

//        if (timestamp instanceof Long) {
//            this.timestamp =(Long) timestamp;
//        } else if (timestamp instanceof Timestamp) {
//
//            this.timestamp=((Timestamp)timestamp).toDate().getTime();
//        }
        this.timestamp=timestamp;
    }

    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl){
        this.fileUrl=fileUrl;
    }

    public String getMessageType() {
        return massageType;
    }
    public void setMassageType(String massageType)
    {
        this.massageType=massageType;
    }


}
