package com.onlineScheduler.Scheduler.dto;


import jdk.jfr.DataAmount;
import lombok.Data;

@Data
public class AppointmentDTO {
    private int startTime;
    private int endTime;
}
