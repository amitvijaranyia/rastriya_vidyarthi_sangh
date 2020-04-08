package com.example.rastriyavidyarthisangh.POJO;

public class SinglePhotoPojo {

    String photo_url;
    long time_uploaded;
    String id_of_who_uploaded;

    public SinglePhotoPojo() {
    }

    public SinglePhotoPojo(String photo_url, long time_uploaded, String id_of_who_uploaded) {
        this.photo_url = photo_url;
        this.time_uploaded = time_uploaded;
        this.id_of_who_uploaded = id_of_who_uploaded;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public long getTime_uploaded() {
        return time_uploaded;
    }

    public void setTime_uploaded(long time_uploaded) {
        this.time_uploaded = time_uploaded;
    }

    public String getWho_uploaded() {
        return id_of_who_uploaded;
    }

    public void setWho_uploaded(String id_of_who_uploaded) {
        this.id_of_who_uploaded = id_of_who_uploaded;
    }
}
