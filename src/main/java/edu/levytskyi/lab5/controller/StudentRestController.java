package edu.levytskyi.lab5.controller;
/* @author Sandoplay
 * @project lab5
 * @class ф
 * @version 1.0.0
 * @since 13.04.2025 - 19.40
 */

import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students") // Використовуємо множину для назви ресурсу
@RequiredArgsConstructor
public class StudentRestController {

    private final StudentService studentService;

    @GetMapping("/")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable String id) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        return studentOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        // Повертаємо 201 Created і створений об'єкт
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student studentDetails) {
        studentDetails.setId(id);
        Optional<Student> updatedStudentOpt = studentService.updateStudent(studentDetails);
        return updatedStudentOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        boolean deleted = studentService.deleteStudentById(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}