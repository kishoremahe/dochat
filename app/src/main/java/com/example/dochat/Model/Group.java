package com.example.dochat.Model;

public class Group {

    private String created_by;
    private String group_name;
    private String imageurl;

    public Group(){

    }

    public Group(String created_by, String group_name, String imageurl) {
        this.created_by = created_by;
        this.group_name = group_name;
        this.imageurl= imageurl;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
