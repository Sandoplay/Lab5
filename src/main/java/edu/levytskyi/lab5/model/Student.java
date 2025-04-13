package edu.levytskyi.lab5.model;
/* @author Sandoplay
 * @project lab5
 * @class a
 * @version 1.0.0
 * @since 13.04.2025 - 19.40
 */

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document // MongoDB документ
@Data // Lombok: гетери, сетери, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    private String id;

    private String name;
    private String groupNumber; // Номер групи
    private Integer course;     // Курс навчання

    // Конструктор без ID
    public Student(String name, String groupNumber, Integer course) {
        this.name = name;
        this.groupNumber = groupNumber;
        this.course = course;
    }

    // Явне перевизначення equals/hashCode лише за ID, якщо НЕ використовується @Data

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}