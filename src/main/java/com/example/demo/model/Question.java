package com.example.demo.model;

public class Question {
    private int id;
    private String content;
    private String type;       // "text", "audio", "image"
    private String mediaPath;  // đường dẫn media (nếu có)
    private String level;      // ví dụ: N5, N4,...

    // Constructor không tham số
    public Question() {
    }

    // Constructor đầy đủ
    public Question(int id, String content, String type, String mediaPath, String level) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.mediaPath = mediaPath;
        this.level = level;
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMediaPath() { return mediaPath; }
    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
