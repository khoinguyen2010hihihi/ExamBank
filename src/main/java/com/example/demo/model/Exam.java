package com.example.demo.model;

import java.sql.Timestamp;

public class Exam {
    private int id;
    private String name;
    private Timestamp createdAt;

    public Exam() {}

    public Exam(int id, String name, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
