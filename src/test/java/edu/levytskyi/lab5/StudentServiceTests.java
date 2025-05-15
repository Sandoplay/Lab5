package edu.levytskyi.lab5;
/* @author Sandoplay
 * @project lab5
 * @class StudentServiceTests
 * @version 1.0.0
 * @since 15.05.2025 - 18.42
 */

import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.repository.StudentRepository;
import edu.levytskyi.lab5.service.StudentService; // Імпорт сервісу залишається, якщо він в іншому пакеті
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StudentServiceTests {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    private List<Student> initialStudentsFromService;

    @BeforeEach
    void setUp() {
        studentService.init();
        initialStudentsFromService = studentService.getAllStudents();
    }

    @AfterEach
    void tearDown() {
        // Закоментовано, оскільки init() в setUp() вже очищає та ініціалізує дані
        // studentRepository.deleteAll();
    }

    // --- Tests for init() and getAllStudents() ---
    @Test
    @DisplayName("1. Service Initialization: Should load exactly 3 initial students")
    void testInit_LoadsInitialStudents_CorrectCount() {
        List<Student> students = studentService.getAllStudents();
        assertNotNull(students);
        assertEquals(3, students.size(), "Database should be initialized with 3 students.");
    }

    @Test
    @DisplayName("2. Service Initialization: First initial student details should match")
    void testInit_FirstStudentDetailsCorrect() {
        List<Student> students = studentService.getAllStudents();
        Student firstInitialSpec = new Student("Іван Петренко", "КН-31", 3);
        Student firstDbStudent = students.stream()
                .filter(s -> s.getName().equals(firstInitialSpec.getName()) &&
                        s.getGroupNumber().equals(firstInitialSpec.getGroupNumber()) &&
                        s.getCourse().equals(firstInitialSpec.getCourse()))
                .findFirst().orElse(null);

        assertNotNull(firstDbStudent, "First initial student not found or details mismatch.");
        assertEquals(firstInitialSpec.getName(), firstDbStudent.getName());
        assertEquals(firstInitialSpec.getGroupNumber(), firstDbStudent.getGroupNumber());
        assertEquals(firstInitialSpec.getCourse(), firstDbStudent.getCourse());
        assertNotNull(firstDbStudent.getId(), "Initialized student should have an ID.");
    }

    @Test
    @DisplayName("3. getAllStudents: Returns all students from repository")
    void testGetAllStudents_ReturnsAll() {
        List<Student> students = studentService.getAllStudents();
        assertEquals(initialStudentsFromService.size(), students.size());
    }

    // --- Tests for getStudentById() ---
    @Test
    @DisplayName("4. getStudentById: Should return student for existing ID")
    void testGetStudentById_ExistingId_ReturnsStudent() {
        assertFalse(initialStudentsFromService.isEmpty(), "Initial students list is empty, cannot proceed.");
        String existingId = initialStudentsFromService.get(0).getId();

        Optional<Student> foundStudentOpt = studentService.getStudentById(existingId);
        assertTrue(foundStudentOpt.isPresent(), "Student should be found for existing ID.");
        assertEquals(existingId, foundStudentOpt.get().getId());
        assertEquals(initialStudentsFromService.get(0).getName(), foundStudentOpt.get().getName());
    }

    @Test
    @DisplayName("5. getStudentById: Should return empty Optional for non-existent ID")
    void testGetStudentById_NonExistentId_ReturnsEmpty() {
        Optional<Student> foundStudentOpt = studentService.getStudentById("nonExistentId123");
        assertFalse(foundStudentOpt.isPresent(), "Student should not be found for non-existent ID.");
    }

    @Test
    @DisplayName("6. getStudentById: Should throw IllegalArgumentException for null ID")
    void testGetStudentById_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentById(null);
        }, "Calling getStudentById with null ID should throw IllegalArgumentException (from MongoRepository).");
    }

    @Test
    @DisplayName("7. getStudentById: Should return empty Optional for empty string ID")
    void testGetStudentById_EmptyStringId_ReturnsEmpty() {
        Optional<Student> foundStudentOpt = studentService.getStudentById("");
        assertFalse(foundStudentOpt.isPresent(), "Student should not be found for empty string ID.");
    }


    // --- Tests for createStudent() ---
    @Test
    @DisplayName("8. createStudent: Successfully creates a new student")
    void testCreateStudent_ValidData_CreatesSuccessfully() {
        Student newStudent = new Student("Олена Василенко", "ІП-11", 1);
        Student createdStudent = studentService.createStudent(newStudent);

        assertNotNull(createdStudent.getId(), "Created student ID should not be null.");
        assertEquals("Олена Василенко", createdStudent.getName());
        assertEquals("ІП-11", createdStudent.getGroupNumber());
        assertEquals(1, createdStudent.getCourse());

        Optional<Student> fetchedStudent = studentRepository.findById(createdStudent.getId());
        assertTrue(fetchedStudent.isPresent());
        assertEquals("Олена Василенко", fetchedStudent.get().getName());
    }

    @Test
    @DisplayName("9. createStudent: ID is set to null before saving (ensuring new document)")
    void testCreateStudent_IdIsSetToNullBeforeSave() {
        Student studentWithId = new Student("existingIdSimulated", "Майк Тайсон", "БОКС-01", 4);
        Student createdStudent = studentService.createStudent(studentWithId);

        assertNotNull(createdStudent.getId());
        assertNotEquals("existingIdSimulated", createdStudent.getId(), "Service should have nulled the ID, so Mongo generates a new one.");
        assertEquals(initialStudentsFromService.size() + 1, studentService.getAllStudents().size());
    }

    @Test
    @DisplayName("10. createStudent: Increases total student count by one")
    void testCreateStudent_IncreasesCount() {
        long initialCount = studentRepository.count();
        Student newStudent = new Student("Анна Коваленко", "ФЛ-22", 2);
        studentService.createStudent(newStudent);
        assertEquals(initialCount + 1, studentRepository.count());
    }


    // --- Tests for updateStudent() ---
    @Test
    @DisplayName("11. updateStudent: Successfully updates an existing student's name")
    void testUpdateStudent_ExistingStudentName_UpdatesSuccessfully() {
        Student studentToUpdate = initialStudentsFromService.get(1);
        String originalId = studentToUpdate.getId();

        Student updateDetails = new Student();
        updateDetails.setId(originalId);
        updateDetails.setName("Updated Name For " + studentToUpdate.getName());
        updateDetails.setGroupNumber(studentToUpdate.getGroupNumber());
        updateDetails.setCourse(studentToUpdate.getCourse());

        Optional<Student> updatedStudentOpt = studentService.updateStudent(updateDetails);
        assertTrue(updatedStudentOpt.isPresent(), "Student should be updated.");
        assertEquals(updateDetails.getName(), updatedStudentOpt.get().getName());
        assertEquals(originalId, updatedStudentOpt.get().getId());
    }

    @Test
    @DisplayName("12. updateStudent: Successfully updates group and course")
    void testUpdateStudent_ExistingStudentGroupAndCourse_UpdatesSuccessfully() {
        Student studentToUpdate = initialStudentsFromService.get(2);
        String originalId = studentToUpdate.getId();

        Student updateDetails = new Student();
        updateDetails.setId(originalId);
        updateDetails.setName(studentToUpdate.getName());
        updateDetails.setGroupNumber("ПМ-22-оновлена");
        updateDetails.setCourse(studentToUpdate.getCourse() + 1);


        Optional<Student> updatedStudentOpt = studentService.updateStudent(updateDetails);
        assertTrue(updatedStudentOpt.isPresent());
        assertEquals("ПМ-22-оновлена", updatedStudentOpt.get().getGroupNumber());
        assertEquals(studentToUpdate.getCourse() + 1, updatedStudentOpt.get().getCourse());
    }

    @Test
    @DisplayName("13. updateStudent: Attempting to update non-existent student returns empty Optional")
    void testUpdateStudent_NonExistentStudent_ReturnsEmpty() {
        Student nonExistentStudent = new Student("nonExistentId456", "Ghost Student", "XX-00", 0);
        Optional<Student> updatedStudentOpt = studentService.updateStudent(nonExistentStudent);
        assertFalse(updatedStudentOpt.isPresent(), "Update should fail for non-existent student.");
    }

    @Test
    @DisplayName("14. updateStudent: Attempting to update with null ID in details throws Exception")
    void testUpdateStudent_NullIdInDetails_ThrowsException() {
        Student studentDetailsWithNullId = new Student(null, "No Id Student", "ZZ-99", 5);
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.updateStudent(studentDetailsWithNullId);
        });
    }

    // --- Tests for deleteStudentById() ---
    @Test
    @DisplayName("15. deleteStudentById: Successfully deletes an existing student")
    void testDeleteStudentById_ExistingId_DeletesSuccessfully() {
        Student studentToDelete = initialStudentsFromService.get(0);
        String idToDelete = studentToDelete.getId();

        assertTrue(studentService.deleteStudentById(idToDelete), "Deletion should be successful.");
        assertFalse(studentRepository.existsById(idToDelete), "Student should no longer exist in repository.");
        assertEquals(initialStudentsFromService.size() - 1, studentService.getAllStudents().size());
    }

    @Test
    @DisplayName("16. deleteStudentById: Attempting to delete non-existent student returns false")
    void testDeleteStudentById_NonExistentId_ReturnsFalse() {
        assertFalse(studentService.deleteStudentById("nonExistentId789"), "Deletion should fail for non-existent ID.");
        assertEquals(initialStudentsFromService.size(), studentService.getAllStudents().size());
    }

    @Test
    @DisplayName("17. deleteStudentById: Attempting to delete with null ID throws Exception")
    void testDeleteStudentById_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.deleteStudentById(null);
        });
    }

    @Test
    @DisplayName("18. deleteStudentById: Attempting to delete with empty string ID returns false")
    void testDeleteStudentById_EmptyStringId_ReturnsFalse() {
        assertFalse(studentService.deleteStudentById(""));
        assertEquals(initialStudentsFromService.size(), studentService.getAllStudents().size());
    }

    // --- Combined/Edge Case Tests ---
    @Test
    @DisplayName("19. Create, then Update, then Get, then Delete")
    void testFullCrudCycle() {
        // Create
        Student student = new Student("Full Cycle", "FC-01", 1);
        Student created = studentService.createStudent(student);
        assertNotNull(created.getId());
        String id = created.getId();

        // Update
        Student updateDetails = new Student(id, "Full Cycle Updated", "FC-02", 2);
        Optional<Student> updatedOpt = studentService.updateStudent(updateDetails);
        assertTrue(updatedOpt.isPresent());
        assertEquals("Full Cycle Updated", updatedOpt.get().getName());

        // Get
        Optional<Student> fetchedOpt = studentService.getStudentById(id);
        assertTrue(fetchedOpt.isPresent());
        assertEquals("Full Cycle Updated", fetchedOpt.get().getName());

        // Delete
        assertTrue(studentService.deleteStudentById(id));
        assertFalse(studentService.getStudentById(id).isPresent());
    }

    @Test
    @DisplayName("20. Multiple creations and getAll")
    void testMultipleCreationsAndGetAll() {
        studentService.createStudent(new Student("Student A", "GRP-A", 1));
        studentService.createStudent(new Student("Student B", "GRP-B", 2));
        assertEquals(initialStudentsFromService.size() + 2, studentService.getAllStudents().size());
    }
}