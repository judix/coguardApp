package com.app.coguardapp;


import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import java.io.Serializable;

public class Message implements Serializable {
    String id, message, url, title, description,image_url,source_url;
    boolean sourced;
    Type type;
    public Message() {
        this.type = Type.TEXT;
    }

    public boolean isSourced() {
        return sourced;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getSource_url() {
        return source_url;
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    public void setSourced(boolean sourced) {
        this.sourced = sourced;
    }

    public Message(RuntimeResponseGeneric r) {
        this.message = "";
        this.title = r.title();
        this.description = r.description();
        this.url = r.source();
        this.id = "2";
        this.type = Type.IMAGE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public enum Type {
        TEXT,
        IMAGE
    }
}

