package edu.levytskyi.lab5;
/* @author Sandoplay
 * @project lab5
 * @class RepositoryTest
 * @version 1.0.0
 * @since 24.04.2025 - 22.52
 */


import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class RepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student student1, student2, student3;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();

        student1 = new Student("Іван Петренко", "ІП-21", 2);
        student2 = new Student("Марія Сидоренко", "КН-22", 2);
        student3 = new Student("Петро Іваненко", "ІП-21", 2);

        student1 = studentRepository.save(student1);
        student2 = studentRepository.save(student2);
        student3 = studentRepository.save(student3);
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Повинен зберігати нового студента і генерувати ID")
    void shouldSaveNewStudentAndGenerateId() {
        Student newStudent = new Student("Олена Василенко", "ПМ-23", 1);
        long countBefore = studentRepository.count();

        Student savedStudent = studentRepository.save(newStudent);
        long countAfter = studentRepository.count();

        assertNotNull(savedStudent);
        assertNotNull(savedStudent.getId());
        assertFalse(savedStudent.getId().isEmpty());
        assertEquals("Олена Василенко", savedStudent.getName());
        assertEquals("ПМ-23", savedStudent.getGroupNumber());
        assertEquals(1, savedStudent.getCourse());
        assertEquals(countBefore + 1, countAfter);

        Optional<Student> found = studentRepository.findById(savedStudent.getId());
        assertTrue(found.isPresent());
        assertEquals("Олена Василенко", found.get().getName());
    }

    @Test
    @DisplayName("2. Повинен знаходити студента за існуючим ID")
    void shouldFindStudentByExistingId() {
        assertNotNull(student1.getId());

        Optional<Student> found = studentRepository.findById(student1.getId());

        assertTrue(found.isPresent());
        assertEquals(student1.getId(), found.get().getId());
        assertEquals(student1.getName(), found.get().getName());
        assertEquals(student1.getGroupNumber(), found.get().getGroupNumber());
        assertEquals(student1.getCourse(), found.get().getCourse());
    }

    @Test
    @DisplayName("3. Повинен повертати порожній Optional для неіснуючого ID")
    void shouldReturnEmptyOptionalForNonExistentId() {
        String nonExistentId = "non-existent-student-id";

        Optional<Student> found = studentRepository.findById(nonExistentId);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("4. Повинен знаходити всіх студентів")
    void shouldFindAllStudents() {
        List<Student> allStudents = studentRepository.findAll();

        assertNotNull(allStudents);
        assertEquals(3, allStudents.size());
        assertThat(allStudents).extracting(Student::getId).containsExactlyInAnyOrder(student1.getId(), student2.getId(), student3.getId());
    }

    @Test
    @DisplayName("5. Повинен оновлювати існуючого студента")
    void shouldUpdateExistingStudent() {
        String existingId = student2.getId();
        assertNotNull(existingId);
        String updatedName = "Марія Оновлена";
        Integer updatedCourse = 3;

        Student studentToUpdate = studentRepository.findById(existingId)
                .orElseThrow(() -> new AssertionError("Студент для оновлення не знайдений в БД"));

        studentToUpdate.setName(updatedName);
        studentToUpdate.setCourse(updatedCourse);
        Student savedStudent = studentRepository.save(studentToUpdate);
        long count = studentRepository.count();

        assertNotNull(savedStudent);
        assertEquals(existingId, savedStudent.getId());
        assertEquals(updatedName, savedStudent.getName());
        assertEquals(updatedCourse, savedStudent.getCourse());
        assertEquals(student2.getGroupNumber(), savedStudent.getGroupNumber());
        assertEquals(3, count);

        Optional<Student> foundAfterUpdate = studentRepository.findById(existingId);
        assertTrue(foundAfterUpdate.isPresent());
        assertEquals(updatedName, foundAfterUpdate.get().getName());
        assertEquals(updatedCourse, foundAfterUpdate.get().getCourse());
    }

    @Test
    @DisplayName("6. Повинен видаляти студента за ID")
    void shouldDeleteStudentById() {
        String idToDelete = student3.getId();
        assertNotNull(idToDelete);
        long countBefore = studentRepository.count();
        assertTrue(studentRepository.existsById(idToDelete));

        studentRepository.deleteById(idToDelete);

        long countAfter = studentRepository.count();
        assertEquals(countBefore - 1, countAfter);
        assertFalse(studentRepository.existsById(idToDelete));
    }

    @Test
    @DisplayName("7. Повинен повертати правильну кількість студентів")
    void shouldReturnCorrectStudentCount() {
        long count = studentRepository.count();

        assertEquals(3, count);

        studentRepository.save(new Student("Тест Кількість", "ТК-01", 4));
        assertEquals(4, studentRepository.count());
    }

    @Test
    @DisplayName("8. Повинен повертати true, якщо студент існує за ID")
    void shouldReturnTrueWhenStudentExistsById() {
        assertNotNull(student1.getId());

        boolean exists = studentRepository.existsById(student1.getId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("9. Повинен повертати false, якщо студент не існує за ID")
    void shouldReturnFalseWhenStudentDoesNotExistById() {
        String nonExistentId = "non-existent-id-student";

        boolean exists = studentRepository.existsById(nonExistentId);

        assertFalse(exists);
    }

    @Test
    @DisplayName("10. Повинен знаходити студентів за номером групи (користувацький запит)")
    void shouldFindByGroupNumber() {
        String targetGroup = "ІП-21";
        String otherGroup = "КН-22";

        List<Student> foundStudents = studentRepository.findByGroupNumber(targetGroup);

        assertNotNull(foundStudents);
        assertEquals(2, foundStudents.size());
        assertTrue(foundStudents.stream().allMatch(s -> s.getGroupNumber().equals(targetGroup)));
        List<String> foundIds = foundStudents.stream().map(Student::getId).toList();
        assertThat(foundIds).containsExactlyInAnyOrder(student1.getId(), student3.getId());

        List<Student> foundOtherStudents = studentRepository.findByGroupNumber(otherGroup);
        assertEquals(1, foundOtherStudents.size());
        assertEquals(student2.getId(), foundOtherStudents.get(0).getId());

        List<Student> foundNonExistent = studentRepository.findByGroupNumber("НЕІСНУЮЧА-ГРУПА");
        assertTrue(foundNonExistent.isEmpty());
    }
}