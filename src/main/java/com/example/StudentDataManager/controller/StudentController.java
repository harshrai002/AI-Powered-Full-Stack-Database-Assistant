package com.example.StudentDataManager.controller;

import com.example.StudentDataManager.entity.Student;
import com.example.StudentDataManager.service.StudentService;
import com.example.StudentDataManager.service.StudentService.StudentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/v1/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<String> storeStudent(@RequestBody Student student) {
        String result = studentService.storeStudent(student.getId(), student.getEmail(), student.getName(), student.getMarks());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<StudentInfo>> getStudentInfoById(@PathVariable Long id) {
        List<StudentInfo> studentInfo = studentService.getStudentInfoById(id);

        if (studentInfo == null || studentInfo.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        }

        return ResponseEntity.ok(studentInfo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudentById(@PathVariable Long id) {
        String result = studentService.deleteStudentById(id);

        if(result.equals("Student not found.")) {
            return ResponseEntity.status(404).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateStudentById(@PathVariable Long id, @RequestBody Student updatedStudent) {
        String result = studentService.updateStudentById(id, updatedStudent);
        if(result.equals("Student not found.")) {
            return ResponseEntity.status(404).body(result);
        }

        return ResponseEntity.ok(result);
    }
}
