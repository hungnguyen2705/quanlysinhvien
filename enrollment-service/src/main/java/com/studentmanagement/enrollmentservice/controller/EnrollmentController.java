package com.studentmanagement.enrollmentservice.controller;

import com.studentmanagement.enrollmentservice.model.Enrollment;
import com.studentmanagement.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping
    public Mono<ResponseEntity<?>> createEnrollment(@RequestBody Enrollment enrollment) {
        
        Mono<Boolean> studentExistsMono = webClientBuilder.build().get()
                .uri("http://student-service/api/students/" + enrollment.getStudentId()) 
                .retrieve() 
                .bodyToMono(String.class) 
                .map(response -> true)
                .onErrorResume(e -> Mono.just(false)) 
                .defaultIfEmpty(false);

        Mono<Boolean> courseExistsMono = webClientBuilder.build().get()
                .uri("http://course-service/api/courses/" + enrollment.getCourseId()) 
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorResume(e -> Mono.just(false))
                .defaultIfEmpty(false);

        return Mono.zip(studentExistsMono, courseExistsMono)
            .flatMap(tuple -> {
                
                boolean studentExists = tuple.getT1();
                boolean courseExists = tuple.getT2();

                if (studentExists && courseExists) {
                    Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
                    return Mono.just(new ResponseEntity<>(savedEnrollment, HttpStatus.CREATED));
                } else {
                    String errorMessage = "Invalid request: ";
                    if (!studentExists) errorMessage += "Student ID not found. ";
                    if (!courseExists) errorMessage += "Course ID not found.";
                    
                    return Mono.just(new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST));
                }
            });
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
}