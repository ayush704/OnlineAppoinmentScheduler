package com.onlineScheduler.Scheduler.service;

import com.onlineScheduler.Scheduler.exception.AppointmentException;
import com.onlineScheduler.Scheduler.model.Appointment;
import com.onlineScheduler.Scheduler.model.ServiceOperator;
import com.onlineScheduler.Scheduler.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.onlineScheduler.Scheduler.dto.AppointmentDTO;

@Service
public class SchedulingService {
    private static final List<ServiceOperator> OPERATORS = Arrays.asList(
            new ServiceOperator("ServiceOperator0"),
            new ServiceOperator("ServiceOperator1"),
            new ServiceOperator("ServiceOperator2")
    );



//checking the condition :0<=startTime<=24 and 0<=endTime<=24 same for newStartTime and newEndTime

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment bookAppointment(int startTime, int endTime, String operatorName) {
        // Validating the startTime and endTime
        if (startTime<0 || startTime>24 || endTime<0 || endTime>24 || endTime-startTime<1 || endTime - startTime > 1) {
            throw new IllegalArgumentException("The appointment duration must be at least 1 hour.");
        }

        ServiceOperator operator = getOperatorByName(operatorName);
        if (operator == null) {
            // Booking with any available operator at the given time frame
            operator = getAvailableOperator(startTime, endTime);
        }

        if (operator != null && !isOverlappingAppointment(operator, startTime, endTime)) {
            Appointment appointment = new Appointment(startTime, endTime, operator.getName());
            appointmentRepository.save(appointment);
            return appointment;
        }

        return null;
    }

    //Here checking the overlapping problem
    private boolean isOverlappingAppointment(ServiceOperator operator, int startTime, int endTime) {
        List<Appointment> operatorAppointments = appointmentRepository.findByOperatorName(operator.getName());
        return operatorAppointments.stream()
                .anyMatch(appointment -> appointment.getStartTime() < endTime && appointment.getEndTime() > startTime);
    }

    private ServiceOperator getOperatorByName(String operatorName) {
        return OPERATORS.stream()
                .filter(operator -> operator.getName().equals(operatorName))
                .findFirst()
                .orElse(null);
    }

    private ServiceOperator getAvailableOperator(int startTime, int endTime) {
        return OPERATORS.stream()
                .filter(operator -> !isOverlappingAppointment(operator, startTime, endTime))
                .findFirst()
                .orElse(null);
    }

    public Appointment rescheduleAppointment(String appointmentId, int newStartTime, int newEndTime) {

        if (newStartTime<0 || newStartTime>24 || newEndTime<0 || newEndTime>24 ||newEndTime-newStartTime<1 || newEndTime - newStartTime > 1) {
            throw new IllegalArgumentException("The appointment duration must be at least 1 hour.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Appointment not found"));


        if (!isOverlappingAppointment(getOperatorByName(appointment.getOperatorName()), newStartTime, newEndTime)) {
            appointment.setStartTime(newStartTime);
            appointment.setEndTime(newEndTime);
            appointmentRepository.save(appointment);
            return appointment;
        }

        return null;
    }

    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Appointment not found"));

        appointmentRepository.delete(appointment);
    }

    public List<AppointmentDTO> getBookedAppointments(String operatorName) {
        List<Appointment> appointments = appointmentRepository.findByOperatorName(operatorName);
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();//Here we are filtering the start and end times only

        appointments.forEach(appointment -> {
            AppointmentDTO appointmentDTO = new AppointmentDTO();
            appointmentDTO.setStartTime(appointment.getStartTime());
            appointmentDTO.setEndTime(appointment.getEndTime());
            appointmentDTOs.add(appointmentDTO);
        });

        return appointmentDTOs;
    }
   //Check for open slots for that particular operator and here merging the interval as well
    public List<OpenSlot> getOpenSlots(String operatorName) {
        List<Appointment> bookedAppointments = appointmentRepository.findByOperatorName(operatorName);
        List<OpenSlot> openSlots = new ArrayList<>();

        int prevEndTime = 0;
        for (Appointment appointment : bookedAppointments) {
            if (appointment.getStartTime() > prevEndTime) {
                openSlots.add(new OpenSlot(prevEndTime, appointment.getStartTime()));
            }
            prevEndTime = appointment.getEndTime();
        }

        // Add the remaining open slots after the last appointment
        if (prevEndTime < 24) {
            openSlots.add(new OpenSlot(prevEndTime, 24));
        }

        return openSlots;
    }

    public static class OpenSlot {
        private int startTime;
        private int endTime;

        public OpenSlot(int startTime, int endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
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


    }
}