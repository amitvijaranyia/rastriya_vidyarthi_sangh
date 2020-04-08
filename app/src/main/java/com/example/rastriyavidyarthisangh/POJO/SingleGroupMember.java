package com.example.rastriyavidyarthisangh.POJO;

public class SingleGroupMember {

    String name;
    String college;
    String profile_picture_url;
    String phone_number;
    String date_joined;

    public SingleGroupMember() {
    }

    public SingleGroupMember(String name, String college, String profile_picture_url, String phone_number, String date_joined) {
        this.name = name;
        this.college = college;
        this.profile_picture_url = profile_picture_url;
        this.phone_number = phone_number;
        this.date_joined = date_joined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getDate_joined() {
        return date_joined;
    }

    public void setDate_joined(String date_joined) {
        this.date_joined = date_joined;
    }
}
