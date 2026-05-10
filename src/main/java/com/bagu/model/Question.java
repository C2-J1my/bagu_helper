package com.bagu.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Question {
    private int id;
    private String question;
    private String answer;
    private String tags;
    private int difficulty; // 1-5
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Question() {}

    public Question(String question, String answer, String tags, int difficulty) {
        this.question = question;
        this.answer = answer;
        this.tags = tags;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String[] getTagArray() {
        return tags == null || tags.isBlank() ? new String[0] : tags.split(",");
    }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String formatDateTime() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }
}
