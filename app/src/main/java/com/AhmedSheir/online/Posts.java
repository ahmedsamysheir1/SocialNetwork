package com.AhmedSheir.online;

public class Posts {

    public String uid , time , date,  postimage, description, userprofileimage, fullname ;

    public Posts ()
    {

    }

    public Posts(String uid, String time, String date, String postimage, String description, String userprofileimage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.userprofileimage = userprofileimage;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserprofileimage() {
        return userprofileimage;
    }

    public void setUserprofileimage(String userprofileimage) {
        this.userprofileimage = userprofileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
