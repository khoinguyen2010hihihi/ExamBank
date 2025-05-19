package com.example.demo.model;

public class ExamQuestion {
    private int examId;
    private int questionId;

    public ExamQuestion() {}

    public ExamQuestion(int examId, int questionId) {
        this.examId = examId;
        this.questionId = questionId;
    }

    // Getter và Setter
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
}
