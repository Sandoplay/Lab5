package edu.levytskyi.lab5;
/* @author Sandoplay
 * @project lab5
 * @class Mocking
 * @version 1.0.0
 * @since 17.05.2025 - 22.37
 */

import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.repository.StudentRepository;
import edu.levytskyi.lab5.service.StudentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceMockTests {

    @Mock
    private StudentRepository mockRepository;

    @InjectMocks //має замінювати underTest = new StudentService(mockRepository);

    private StudentService underTest;

    @Captor
    private ArgumentCaptor<Student> studentArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    private Student studentToCreate;
    private Student sampleStudent1;
    private Student sampleStudent2;

    @BeforeEach
    void setUp() {
        studentToCreate = new Student("Новий Студент", "ІПЗ-41", 4);
        sampleStudent1 = new Student("id1", "Іван Петренко", "КН-31", 3);
        sampleStudent2 = new Student("id2", "Марія Сидоренко", "ПМ-21", 2);
    }

    @DisplayName("1. CreateStudent: New Student (Happy Path)")
    @Test
    void createStudent_whenNewStudent_thenSavesAndReturnsStudent() {
        given(mockRepository.save(any(Student.class))).willAnswer(invocation -> {
            Student studentPassedToSave = invocation.getArgument(0);
            return new Student( // Повертаємо новий екземпляр, імітуючи генерацію ID
                    "generatedId123",
                    studentPassedToSave.getName(),
                    studentPassedToSave.getGroupNumber(),
                    studentPassedToSave.getCourse()
            );
        });

        Student studentForService = new Student("Новий Студент", "ІПЗ-41", 4);

        Student createdStudentByService = underTest.createStudent(studentForService);

        then(mockRepository).should().save(studentArgumentCaptor.capture());
        Student studentActuallyPassedToRepoSave = studentArgumentCaptor.getValue();

        assertNull(studentActuallyPassedToRepoSave.getId());
        assertThat(studentActuallyPassedToRepoSave.getName()).isEqualTo(studentForService.getName());
        assertThat(studentActuallyPassedToRepoSave.getGroupNumber()).isEqualTo(studentForService.getGroupNumber());
        assertThat(studentActuallyPassedToRepoSave.getCourse()).isEqualTo(studentForService.getCourse());

        assertNotNull(createdStudentByService);
        assertThat(createdStudentByService.getName()).isEqualTo(studentForService.getName());
        assertThat(createdStudentByService.getId()).isEqualTo("generatedId123");

        verify(mockRepository, times(1)).save(any(Student.class));
    }

    @DisplayName("2. UpdateStudent: Existing Student (Happy Path)")
    @Test
    void updateStudent_whenStudentExists_thenUpdatesAndReturnsUpdatedStudent() {
        Student existingStudent = new Student("idToUpdate", "Старе Ім'я", "ГР-00", 1);
        Student studentDetailsToUpdate = new Student("idToUpdate", "Нове Оновлене Ім'я", "ГР-01", 2);

        given(mockRepository.findById(existingStudent.getId())).willReturn(Optional.of(existingStudent));
        given(mockRepository.save(any(Student.class))).willAnswer(invocation -> invocation.getArgument(0));

        Optional<Student> updatedStudentOpt = underTest.updateStudent(studentDetailsToUpdate);

        assertTrue(updatedStudentOpt.isPresent());
        Student updatedStudent = updatedStudentOpt.get();

        assertThat(updatedStudent.getName()).isEqualTo(studentDetailsToUpdate.getName());
        assertThat(updatedStudent.getGroupNumber()).isEqualTo(studentDetailsToUpdate.getGroupNumber());
        assertThat(updatedStudent.getCourse()).isEqualTo(studentDetailsToUpdate.getCourse());

        then(mockRepository).should(times(1)).findById(existingStudent.getId());
        then(mockRepository).should(times(1)).save(studentArgumentCaptor.capture());
        Student studentActuallySaved = studentArgumentCaptor.getValue();

        assertThat(studentActuallySaved.getId()).isEqualTo(existingStudent.getId());
        assertThat(studentActuallySaved.getName()).isEqualTo(studentDetailsToUpdate.getName());
    }

    @DisplayName("3. UpdateStudent: Student does not exist should return empty Optional")
    @Test
    void updateStudent_whenStudentDoesNotExist_thenReturnsEmptyOptional() {
        Student studentDetailsToUpdate = new Student("nonExistentId", "Ім'я", "ГР-00", 1);
        given(mockRepository.findById(studentDetailsToUpdate.getId())).willReturn(Optional.empty());

        Optional<Student> updatedStudentOpt = underTest.updateStudent(studentDetailsToUpdate);

        assertFalse(updatedStudentOpt.isPresent());
        then(mockRepository).should(times(1)).findById(studentDetailsToUpdate.getId());
        then(mockRepository).should(never()).save(any(Student.class));
    }

    @DisplayName("4. DeleteStudentById: Student exists (Happy Path)")
    @Test
    void deleteStudentById_whenStudentExists_thenDeletesAndReturnsTrue() {
        String idToDelete = "studentToDelete123";
        given(mockRepository.existsById(idToDelete)).willReturn(true);
        doNothing().when(mockRepository).deleteById(anyString());

        boolean deleted = underTest.deleteStudentById(idToDelete);

        assertTrue(deleted);
        then(mockRepository).should(times(1)).existsById(idToDelete);
        then(mockRepository).should(times(1)).deleteById(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(idToDelete);
    }

    @DisplayName("5. DeleteStudentById: Student does not exist should return false")
    @Test
    void deleteStudentById_whenStudentDoesNotExist_thenReturnsFalseAndDoesNotDelete() {
        String idToDelete = "nonExistentId123";
        given(mockRepository.existsById(idToDelete)).willReturn(false);

        boolean deleted = underTest.deleteStudentById(idToDelete);

        assertFalse(deleted);
        then(mockRepository).should(times(1)).existsById(idToDelete);
        then(mockRepository).should(never()).deleteById(anyString());
    }

    @DisplayName("6. GetAllStudents: When students exist should return list of students")
    @Test
    void getAllStudents_whenStudentsExist_thenReturnsListOfStudents() {
        List<Student> expectedStudents = Arrays.asList(sampleStudent1, sampleStudent2);
        given(mockRepository.findAll()).willReturn(expectedStudents);

        List<Student> actualStudents = underTest.getAllStudents();

        assertNotNull(actualStudents);
        assertThat(actualStudents.size()).isEqualTo(2);
        assertThat(actualStudents).containsExactlyInAnyOrder(sampleStudent1, sampleStudent2);
        then(mockRepository).should(times(1)).findAll();
    }

    @DisplayName("7. GetAllStudents: When no students exist should return empty list")
    @Test
    void getAllStudents_whenNoStudentsExist_thenReturnsEmptyList() {
        given(mockRepository.findAll()).willReturn(new ArrayList<>());

        List<Student> actualStudents = underTest.getAllStudents();

        assertNotNull(actualStudents);
        assertTrue(actualStudents.isEmpty());
        then(mockRepository).should(times(1)).findAll();
    }

    @DisplayName("8. GetStudentById: When student exists should return Optional of the student")
    @Test
    void getStudentById_whenStudentExists_thenReturnsOptionalOfStudent() {
        given(mockRepository.findById(sampleStudent1.getId())).willReturn(Optional.of(sampleStudent1));

        Optional<Student> actualStudentOpt = underTest.getStudentById(sampleStudent1.getId());

        assertTrue(actualStudentOpt.isPresent());
        Student actualStudent = actualStudentOpt.get();
        assertThat(actualStudent.getId()).isEqualTo(sampleStudent1.getId());
        assertThat(actualStudent.getName()).isEqualTo(sampleStudent1.getName());
        then(mockRepository).should(times(1)).findById(sampleStudent1.getId());
    }

    @DisplayName("9. GetStudentById: When student does not exist should return empty Optional")
    @Test
    void getStudentById_whenStudentDoesNotExist_thenReturnsEmptyOptional() {
        String nonExistentId = "nonExistent123";
        given(mockRepository.findById(nonExistentId)).willReturn(Optional.empty());

        Optional<Student> actualStudentOpt = underTest.getStudentById(nonExistentId);

        assertFalse(actualStudentOpt.isPresent());
        then(mockRepository).should(times(1)).findById(nonExistentId);
    }

    @DisplayName("10. CreateStudent: Service correctly nullifies ID even if input student has one")
    @Test
    void createStudent_serviceNullifiesPreExistingIdBeforeSaving() {
        Student studentWithPreExistingId = new Student("someId", "Тестовий Студент", "ТК-11", 1);

        given(mockRepository.save(any(Student.class))).willAnswer(invocation -> {
            Student argumentToSave = invocation.getArgument(0);
            assertNull(argumentToSave.getId());
            return new Student("dbGeneratedId", argumentToSave.getName(), argumentToSave.getGroupNumber(), argumentToSave.getCourse());
        });

        Student resultFromService = underTest.createStudent(studentWithPreExistingId);

        then(mockRepository).should().save(studentArgumentCaptor.capture());
        Student capturedStudent = studentArgumentCaptor.getValue();
        assertNull(capturedStudent.getId());


        assertNotNull(resultFromService.getId());
        assertEquals("dbGeneratedId", resultFromService.getId());
    }
}