package com.onlineScheduler.Scheduler.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "appointments")//using MongoDB database
public class Appointment {
    @Id
    private String id;
    private int startTime;
    private int endTime;
    private String operatorName;

    public Appointment() {
        // Default constructor
    }

    public Appointment(int startTime, int endTime, String operatorName) {
        this.id = UUID.randomUUID().toString();//taking random ID as asked in the requirement
        this.startTime = startTime;
        this.endTime = endTime;
        this.operatorName = operatorName;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
