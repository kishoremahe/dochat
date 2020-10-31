package com.example.dochat.Model;

public class User {

    private String uid;
    private String username;
    private String about;
    private String status;
    private String imageurl;
    private String device_token;
    private String last_seen_date;
    private String last_seen_time;
    private String login_status;

    public User(){

    }

    public User(String uid, String username, String about,String imageurl,String status,String device_token,String last_seen_date,String last_seen_time,String login_status) {
        this.uid = uid;
        this.username = username;
        this.about = about;
        this.imageurl=imageurl;
        this.status=status;
        this.device_token=device_token;
        this.last_seen_date=last_seen_date;
        this.last_seen_time=last_seen_time;
        this.login_status=login_status;
    }

    public String getImageurl() {
        return imageurl;
    }

    public String getLogin_status() {
        return login_status;
    }

    public void setLogin_status(String login_status) {
        this.login_status = login_status;
    }

    public String getStatus() {
        return status;
    }

    public String getLast_seen_date() {
        return last_seen_date;
    }

    public void setLast_seen_date(String last_seen_date) {
        this.last_seen_date = last_seen_date;
    }

    public String getLast_seen_time() {
        return last_seen_time;
    }

    public void setLast_seen_time(String last_seen_time) {
        this.last_seen_time = last_seen_time;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
