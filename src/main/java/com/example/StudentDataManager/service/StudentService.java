package com.example.StudentDataManager.service;

import com.example.StudentDataManager.entity.Student;
import com.example.StudentDataManager.repository.StudentRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public String storeStudent(Long id, String email, String name, Map<String, Integer> marks) {
        Student existingStudent = studentRepository.findById(id);
        if(existingStudent != null) {
            return "Student already exists.";
        }

        Student student = new Student();
        student.setId(id);
        student.setEmail(email);
        student.setName(name);
        student.setMarks(marks != null ? marks : new HashMap<>());
        studentRepository.save(student);
        return "Student stored successfully";
    }

    public String deleteStudentById(Long id) {
        Student existingStudent = studentRepository.findById(id);
        if(existingStudent == null) return "Student not found.";
        studentRepository.deleteById(id);
        return "Student deleted successfully.";
    }

    public List<StudentInfo> getStudentInfoById(Long id) {
        Student student = studentRepository.findById(id);
        if (student == null) {
            return Collections.emptyList();
        }
        return List.of(convertToStudentInfo(student));
    }

    public String updateStudentById(Long id, Student updatedStudent) {
        Student existingStudent = studentRepository.findById(id);
        if(existingStudent == null) {
            return "Student not found.";
        }

        // Update name if provided
        if(updatedStudent.getName() != null) {
            existingStudent.setName(updatedStudent.getName());
        }

        // Update email if provided
        if(updatedStudent.getEmail() != null) {
            existingStudent.setEmail(updatedStudent.getEmail());
        }

        // Update marks if provided
        if(updatedStudent.getMarks() != null) {
            Map<String, Integer> existingMarks = existingStudent.getMarks();
            if (existingMarks == null) {
                existingMarks = new HashMap<>();
            }

            // Merge/update marks instead of replacing entirely
            for (Map.Entry<String, Integer> entry : updatedStudent.getMarks().entrySet()) {
                existingMarks.put(entry.getKey(), entry.getValue());
            }
            existingStudent.setMarks(existingMarks);
        }

        studentRepository.save(existingStudent);
        return "Student updated successfully.";
    }

    // Method to update specific subject marks
    public String updateSubjectMarks(Long id, String subject, Integer marks) {
        Student existingStudent = studentRepository.findById(id);
        if(existingStudent == null) {
            return "Student not found.";
        }

        Map<String, Integer> currentMarks = existingStudent.getMarks();
        if (currentMarks == null) {
            currentMarks = new HashMap<>();
        }

        currentMarks.put(subject, marks);
        existingStudent.setMarks(currentMarks);
        studentRepository.save(existingStudent);

        return "Subject marks updated successfully.";
    }

    private StudentInfo convertToStudentInfo(Student student) {
        return new StudentInfo(student.getId(), student.getName(), student.getEmail(), student.getMarks());
    }

    @Setter
    @Getter
    public static class StudentInfo {
        private Long id;
        private String name;
        private String email;
        private Map<String, Integer> marks;

        public StudentInfo(Long id, String name, String email, Map<String, Integer> marks) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.marks = marks != null ? marks : new HashMap<>();
        }
    }
}



