package com.onlineScheduler.Scheduler.repository;

import com.onlineScheduler.Scheduler.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByOperatorName(String operatorName);
}