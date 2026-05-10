package com.bagu.model;

import java.time.LocalDateTime;

public class SpacedRepetition {
    private int questionId;
    private double easeFactor;
    private int intervalDays;
    private int repetitions;
    private LocalDateTime nextReviewAt;

    public SpacedRepetition() {
        this.easeFactor = 2.5;
        this.intervalDays = 0;
        this.repetitions = 0;
    }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(LocalDateTime nextReviewAt) { this.nextReviewAt = nextReviewAt; }

    public boolean isDue() {
        return nextReviewAt == null || LocalDateTime.now().isAfter(nextReviewAt);
    }
}
