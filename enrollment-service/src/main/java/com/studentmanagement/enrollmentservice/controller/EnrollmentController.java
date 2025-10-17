package com.studentmanagement.enrollmentservice.controller;

import com.studentmanagement.enrollmentservice.model.Enrollment;
import com.studentmanagement.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @PostMapping
    public ResponseEntity<Enrollment> createEnrollment(@RequestBody Enrollment enrollment) {

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return new ResponseEntity<>(savedEnrollment, HttpStatus.CREATED);
    }


    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
}