package edu.levytskyi.lab5.service;
/* @author Sandoplay
 * @project lab5
 * @class s
 * @version 1.0.0
 * @since 13.04.2025 - 19.40
 */

import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.repository.StudentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    private final List<Student> initialStudents = Arrays.asList(
            new Student("Іван Петренко", "КН-31", 3),
            new Student("Марія Сидоренко", "ПМ-21", 2),
            new Student("Петро Іваненко", "КН-31", 3)
    );

    @PostConstruct
    public void init() {
        studentRepository.deleteAll();
        studentRepository.saveAll(initialStudents);
        System.out.println("Initialized database with sample students.");
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student) {
        student.setId(null);
        return studentRepository.save(student);
    }

    public Optional<Student> updateStudent(Student studentDetails) {
        return studentRepository.findById(studentDetails.getId())
                .map(existingStudent -> {
                    existingStudent.setName(studentDetails.getName());
                    existingStudent.setGroupNumber(studentDetails.getGroupNumber());
                    existingStudent.setCourse(studentDetails.getCourse());
                    return studentRepository.save(existingStudent);
                });
    }

    public boolean deleteStudentById(String id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}