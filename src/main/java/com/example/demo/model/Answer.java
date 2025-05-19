package com.example.demo.model;

public class Answer {
    private int id;
    private int questionId;
    private String content;
    private boolean isCorrect;

    public Answer() {}

    public Answer(int id, int questionId, String content, boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.isCorrect = isCorrect;
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}
