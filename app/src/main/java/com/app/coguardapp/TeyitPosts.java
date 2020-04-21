package com.app.coguardapp;

public class TeyitPosts {

    String title;
    String url;
    String image_url;

    public TeyitPosts(String title, String url, String image_url) {
        this.title = title;
        this.url = url;
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
