package com.AhmedSheir.online;

public class Commnents
{
    public String comment, date, time, username, profileimage;

    public Commnents()
    {

    }

    public Commnents(String comment, String time, String username, String profileimage ) {
        this.comment = comment;
        this.time = time;
        this.username = username;
        this.profileimage = profileimage;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
