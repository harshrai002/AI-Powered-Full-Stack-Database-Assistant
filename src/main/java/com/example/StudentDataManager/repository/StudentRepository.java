package com.example.StudentDataManager.repository;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.example.StudentDataManager.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void save(Student student) {
        dynamoDBMapper.save(student);
    }

    public Student findById(Long id) {
        return dynamoDBMapper.load(Student.class, id);
    }

    public void deleteById(Long id) {
        Student student = dynamoDBMapper.load(Student.class, id);
        if(student != null) dynamoDBMapper.delete(student);
    }

    public List<Student> findAll() {
        return dynamoDBMapper.scan(Student.class, new DynamoDBScanExpression());
    }
}
