package com.example.dochat.Model;

public class PrivateMessage {

    private String message,sender,Date,Time,receiver,msg_type;
    private boolean isseen;


    public PrivateMessage(String message, String sender, String Date, String Time, String receiver,String msg_type,boolean isseen) {
        this.message = message;
        this.sender = sender;
        this.receiver=receiver;
        this.Date = Date;
        this.Time = Time;
        this.msg_type=msg_type;
        this.isseen=isseen;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public PrivateMessage(){

    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
