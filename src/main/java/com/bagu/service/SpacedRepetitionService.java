package com.bagu.service;

import com.bagu.db.QuestionDao;
import com.bagu.model.SpacedRepetition;

import java.time.LocalDateTime;

/**
 * SM-2 spaced repetition algorithm:
 * - Correct answer: interval grows (1, 6, then interval * easeFactor)
 * - Wrong answer: reset to 1 day, ease factor decreases
 */
public class SpacedRepetitionService {
    private final QuestionDao dao;

    public SpacedRepetitionService(QuestionDao dao) {
        this.dao = dao;
    }

    public void recordResult(int questionId, boolean correct) {
        SpacedRepetition sr = dao.getSpacedRepetition(questionId);

        if (correct) {
            int reps = sr.getRepetitions() + 1;
            int interval;
            if (reps == 1) {
                interval = 1;
            } else if (reps == 2) {
                interval = 6;
            } else {
                interval = (int) Math.ceil(sr.getIntervalDays() * sr.getEaseFactor());
            }
            double ef = Math.min(sr.getEaseFactor() + 0.1, 2.5);

            sr.setRepetitions(reps);
            sr.setIntervalDays(interval);
            sr.setEaseFactor(ef);
        } else {
            sr.setRepetitions(0);
            sr.setIntervalDays(1);
            sr.setEaseFactor(Math.max(sr.getEaseFactor() - 0.2, 1.3));
        }

        sr.setNextReviewAt(LocalDateTime.now().plusDays(sr.getIntervalDays()));
        dao.updateSpacedRepetition(sr);
    }
}
