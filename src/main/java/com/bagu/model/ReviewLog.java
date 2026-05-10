package com.bagu.model;

import java.time.LocalDateTime;

public class ReviewLog {
    private int id;
    private int questionId;
    private boolean correct;
    private int responseSeconds;
    private LocalDateTime reviewedAt;

    public ReviewLog() {}

    public ReviewLog(int questionId, boolean correct) {
        this.questionId = questionId;
        this.correct = correct;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public int getResponseSeconds() { return responseSeconds; }
    public void setResponseSeconds(int responseSeconds) { this.responseSeconds = responseSeconds; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
