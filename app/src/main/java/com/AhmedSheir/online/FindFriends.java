package com.AhmedSheir.online;

public class FindFriends
{
    private String profileimage,FullName, Status;

    public FindFriends(String profileimage, String fullName, String status) {
        this.profileimage = profileimage;
        this.FullName = fullName;
        this.Status = status;
    }

    public FindFriends() {
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        this.FullName = fullName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }
}