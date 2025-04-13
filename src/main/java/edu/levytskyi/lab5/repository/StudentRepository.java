package edu.levytskyi.lab5.repository;
/* @author Sandoplay
 * @project lab5
 * @class a
 * @version 1.0.0
 * @since 13.04.2025 - 19.40
 */

import edu.levytskyi.lab5.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
}