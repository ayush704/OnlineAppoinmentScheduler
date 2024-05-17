package com.onlineScheduler.Scheduler.Controller;

import com.onlineScheduler.Scheduler.dto.AppointmentDTO;
import com.onlineScheduler.Scheduler.exception.AppointmentException;
import com.onlineScheduler.Scheduler.model.Appointment;
import com.onlineScheduler.Scheduler.service.SchedulingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired
    private SchedulingService schedulingService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestParam int startTime,
                                                               @RequestParam int endTime,
                                                               @RequestParam(required = false) String operatorName) {
        try {
            Appointment appointment = schedulingService.bookAppointment(startTime, endTime, operatorName);
            if (appointment != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("_id", appointment.getId());
                response.put("startTime", appointment.getStartTime());
                response.put("endTime", appointment.getEndTime());
                response.put("operatorName", appointment.getOperatorName());
                log.info("Saving Appointment : AppointmentId : {}", appointment.getId());
                return ResponseEntity.ok(response);
            } else {
                throw new AppointmentException("Failed to book appointment.");
            }
        } catch (AppointmentException e) {
            log.error("Exception Occured", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(@PathVariable String appointmentId,
                                                                     @RequestParam int newStartTime,
                                                                     @RequestParam int newEndTime) {
        try {

            Appointment rescheduledAppointment = schedulingService.rescheduleAppointment(appointmentId, newStartTime, newEndTime);
            if (rescheduledAppointment != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("_id", rescheduledAppointment.getId());
                response.put("startTime", rescheduledAppointment.getStartTime());
                response.put("endTime", rescheduledAppointment.getEndTime());
                response.put("operatorName", rescheduledAppointment.getOperatorName());
                return ResponseEntity.ok(response);
            } else {
                throw new AppointmentException("Failed to reschedule appointment.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@PathVariable String appointmentId) {
        try {
            schedulingService.cancelAppointment(appointmentId);
            return ResponseEntity.ok("Appointment canceled successfully.");
        } catch (AppointmentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/booked/{operatorName}")
    public ResponseEntity<List<AppointmentDTO>> getBookedAppointments(@PathVariable String operatorName) {
        List<AppointmentDTO> bookedAppointments = schedulingService.getBookedAppointments(operatorName);
        return ResponseEntity.ok(bookedAppointments);
    }

    @GetMapping("/open-slots/{operatorName}")
    public ResponseEntity<List<SchedulingService.OpenSlot>> getOpenSlots(@PathVariable String operatorName) {
        List<SchedulingService.OpenSlot> openSlots = schedulingService.getOpenSlots(operatorName);
        return ResponseEntity.ok(openSlots);
    }

    //just to handle some exception
    @ExceptionHandler(AppointmentException.class)
    public ResponseEntity<String> handleAppointmentException(AppointmentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}