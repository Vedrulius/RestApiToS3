package com.mihey.restapitos3.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class Notification {

    private String message;
    private String createdBy= "RestApiToS3";
    private String modifiedBy="RestApiToS3";
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    private Timestamp modified = new Timestamp(System.currentTimeMillis());
    private Status status;
    private Type type;

    public Notification() {
    }

    public Notification(String message, Status status, Type type) {
        this.message = message;
        this.status = status;
        this.type = type;
    }
}


