package com.example.rastriyavidyarthisangh.POJO;

public class SingleEventPojo {

    String who_posted;
    long time_posted;
    String id_of_who_posted;
    String description_of_post;
    String event_photo_url;

    public SingleEventPojo() {
    }

    public SingleEventPojo(String who_posted, long time_posted, String id_of_who_posted, String description_of_post, String event_photo_url) {
        this.who_posted = who_posted;
        this.time_posted = time_posted;
        this.id_of_who_posted = id_of_who_posted;
        this.description_of_post = description_of_post;
        this.event_photo_url = event_photo_url;
    }

    public String getWho_posted() {
        return who_posted;
    }

    public void setWho_posted(String who_posted) {
        this.who_posted = who_posted;
    }

    public long getTime_posted() {
        return time_posted;
    }

    public void setTime_posted(long time_posted) {
        this.time_posted = time_posted;
    }

    public String getId_of_who_posted() {
        return id_of_who_posted;
    }

    public void setId_of_who_posted(String id_of_who_posted) {
        this.id_of_who_posted = id_of_who_posted;
    }

    public String getDescription_of_post() {
        return description_of_post;
    }

    public void setDescription_of_post(String description_of_post) {
        this.description_of_post = description_of_post;
    }

    public String getEvent_photo_url() {
        return event_photo_url;
    }

    public void setEvent_photo_url(String event_photo_url) {
        this.event_photo_url = event_photo_url;
    }
}
