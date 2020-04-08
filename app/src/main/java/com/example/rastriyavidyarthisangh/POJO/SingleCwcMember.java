package com.example.rastriyavidyarthisangh.POJO;

public class SingleCwcMember {

    String name;
    String designation;
    String phone_number;
    String email_id;
    String facebook_id;
    String profile_picture_url;
    int priority;

    public SingleCwcMember() {
    }

    public SingleCwcMember(String name, String designation, String phone_number, String email_id,
                           String facebook_id, String profile_picture_url, int priority) {
        this.name = name;
        this.designation = designation;
        this.phone_number = phone_number;
        this.email_id = email_id;
        this.facebook_id = facebook_id;
        this.profile_picture_url = profile_picture_url;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
