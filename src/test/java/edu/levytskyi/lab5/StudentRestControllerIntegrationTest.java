package edu.levytskyi.lab5;
/* @author Sandoplay
 * @project lab5
 * @class StudentRestControllerIntegrationTest
 * @version 1.0.0
 * @since 18.05.2025 - 17.31
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.levytskyi.lab5.model.Student;
import edu.levytskyi.lab5.repository.StudentRepository;
import edu.levytskyi.lab5.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    //ObjectMapper для десеріалізації в тестах
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Student student1, student2, student3;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();

        student1 = studentRepository.save(Student.builder().name("Alice Smith").groupNumber("CS101").course(2).build());
        student2 = studentRepository.save(Student.builder().name("Bob Johnson").groupNumber("EE202").course(3).build());
        student3 = studentRepository.save(Student.builder().name("Charlie Brown").groupNumber("CS101").course(2).build());
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Create Student - Success (Happy Path)")
    void testCreateStudent_Success() throws Exception {
        Student newStudentRequest = Student.builder().name("David Lee").groupNumber("ME303").course(4).build();

        MvcResult result = mockMvc.perform(post("/api/v1/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Utils.toJson(newStudentRequest))) // Використовуємо Utils.toJson
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("David Lee"))
                .andExpect(jsonPath("$.groupNumber").value("ME303"))
                .andExpect(jsonPath("$.course").value(4))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Student createdStudentResponse = objectMapper.readValue(responseString, Student.class);
        Optional<Student> studentFromDb = studentRepository.findById(createdStudentResponse.getId());

        assertThat(studentFromDb).isPresent();
        assertThat(studentFromDb.get().getName()).isEqualTo(newStudentRequest.getName());
        assertThat(studentFromDb.get().getGroupNumber()).isEqualTo(newStudentRequest.getGroupNumber());
        assertThat(studentFromDb.get().getCourse()).isEqualTo(newStudentRequest.getCourse());
    }

    @Test
    @DisplayName("2. Get All Students - Returns Initial Students")
    void testGetAllStudents_ReturnsInitialStudents() throws Exception {
        mockMvc.perform(get("/api/v1/students/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value(student1.getName()))
                .andExpect(jsonPath("$[1].name").value(student2.getName()))
                .andExpect(jsonPath("$[2].name").value(student3.getName()));
    }

    @Test
    @DisplayName("3. Get Student by ID - Exists (Happy Path)")
    void testGetStudentById_Exists_Success() throws Exception {
        mockMvc.perform(get("/api/v1/students/" + student1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(student1.getId()))
                .andExpect(jsonPath("$.name").value(student1.getName()))
                .andExpect(jsonPath("$.groupNumber").value(student1.getGroupNumber()))
                .andExpect(jsonPath("$.course").value(student1.getCourse()));
    }

    @Test
    @DisplayName("4. Get Student by ID - Not Exists (Returns 404 Not Found)")
    void testGetStudentById_NotExists_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/students/nonExistentId123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5. Update Student - Exists (Happy Path)")
    void testUpdateStudent_Exists_Success() throws Exception {
        Student updatedDetails = Student.builder()
                .name("Alice Wonderland")
                .groupNumber("CS102")
                .course(3)
                .build();

        mockMvc.perform(put("/api/v1/students/" + student1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Utils.toJson(updatedDetails))) // Використовуємо Utils.toJson
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student1.getId()))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"))
                .andExpect(jsonPath("$.groupNumber").value("CS102"))
                .andExpect(jsonPath("$.course").value(3));

        Optional<Student> studentFromDb = studentRepository.findById(student1.getId());
        assertThat(studentFromDb).isPresent();
        assertThat(studentFromDb.get().getName()).isEqualTo("Alice Wonderland");
        assertThat(studentFromDb.get().getGroupNumber()).isEqualTo("CS102");
    }

    @Test
    @DisplayName("6. Update Student - Not Exists (Returns 404 Not Found)")
    void testUpdateStudent_NotExists_ReturnsNotFound() throws Exception {
        Student updatedDetails = Student.builder().name("Ghost Student").groupNumber("XX000").course(0).build();
        mockMvc.perform(put("/api/v1/students/nonExistentId123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Utils.toJson(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("7. Delete Student - Exists (Happy Path)")
    void testDeleteStudent_Exists_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/students/" + student1.getId()))
                .andExpect(status().isNoContent());

        assertThat(studentRepository.findById(student1.getId())).isEmpty();
        assertThat(studentRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("8. Delete Student - Not Exists (Returns 404 Not Found)")
    void testDeleteStudent_NotExists_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/students/nonExistentId123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("9. Get All Students - When Repository Is Empty (Returns Empty List)")
    void testGetAllStudents_WhenRepositoryIsEmpty_ReturnsEmptyList() throws Exception {
        studentRepository.deleteAll();

        mockMvc.perform(get("/api/v1/students/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("10. Create Student - Attempt to Create with Existing ID (Should ignore provided ID and generate new)")
    void testCreateStudent_WithExistingId_ShouldGenerateNewId() throws Exception {
        Student studentWithIdRequest = Student.builder()
                .id(student1.getId())
                .name("Duplicate Test")
                .groupNumber("DT100")
                .course(1)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Utils.toJson(studentWithIdRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(is(org.hamcrest.Matchers.not(student1.getId()))))
                .andExpect(jsonPath("$.name").value("Duplicate Test"))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Student createdStudentResponse = objectMapper.readValue(responseString, Student.class);
        Optional<Student> studentFromDb = studentRepository.findById(createdStudentResponse.getId());

        assertThat(studentFromDb).isPresent();
        assertThat(studentFromDb.get().getId()).isNotEqualTo(student1.getId());
        assertThat(studentRepository.count()).isEqualTo(4);
    }
}