package com.example.dochat.Model;

public class GroupMessage {

    private String message,sender,Date,Time,msg_type;

    public GroupMessage(String message, String sender, String Date, String Time,String msg_type) {
        this.message = message;
        this.sender = sender;
        this.Date = Date;
        this.Time = Time;
        this.msg_type=msg_type;
    }

    public GroupMessage(){

    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
