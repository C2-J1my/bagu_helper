package com.bagu.db;

import com.bagu.model.Question;
import com.bagu.model.ReviewLog;
import com.bagu.model.SpacedRepetition;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionDao {

    public void insert(Question q) {
        String sql = "INSERT INTO questions (question, answer, tags, difficulty, source) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getQuestion());
            ps.setString(2, q.getAnswer());
            ps.setString(3, q.getTags());
            ps.setInt(4, q.getDifficulty());
            ps.setString(5, q.getSource());
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) {
                q.setId(rs.getInt(1));
            }
            // Init spaced repetition
            try (PreparedStatement sr = conn.prepareStatement(
                    "INSERT OR IGNORE INTO spaced_repetition (question_id) VALUES (?)")) {
                sr.setInt(1, q.getId());
                sr.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert question", e);
        }
    }

    public Optional<Question> findById(int id) {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find question", e);
        }
        return Optional.empty();
    }

    public List<Question> findAll() {
        return findWithSql("SELECT * FROM questions ORDER BY created_at DESC", new Object[0]);
    }

    public List<Question> findByTag(String tag) {
        return findWithSql("SELECT * FROM questions WHERE tags LIKE ? ORDER BY created_at DESC",
                new Object[]{"%" + tag + "%"});
    }

    public Optional<Question> random() {
        String sql = "SELECT * FROM questions ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get random question", e);
        }
        return Optional.empty();
    }

    public Optional<Question> nextDue() {
        String sql = """
            SELECT q.* FROM questions q
            JOIN spaced_repetition sr ON q.id = sr.question_id
            WHERE sr.next_review_at <= datetime('now','localtime')
            ORDER BY sr.next_review_at ASC, RANDOM()
            LIMIT 1
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get due question", e);
        }
        return Optional.empty();
    }

    public int countDue() {
        String sql = """
            SELECT COUNT(*) FROM questions q
            JOIN spaced_repetition sr ON q.id = sr.question_id
            WHERE sr.next_review_at <= datetime('now','localtime')
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count due questions", e);
        }
    }

    public int count() {
        return count("SELECT COUNT(*) FROM questions");
    }

    public int countReviewedToday() {
        String sql = "SELECT COUNT(*) FROM review_logs WHERE date(reviewed_at) = date('now','localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count today's reviews", e);
        }
    }

    public int countCorrectToday() {
        String sql = "SELECT COUNT(*) FROM review_logs WHERE correct = 1 AND date(reviewed_at) = date('now','localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count correct today", e);
        }
    }

    public double overallAccuracy() {
        String sql = "SELECT AVG(correct) * 100.0 FROM review_logs";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get accuracy", e);
        }
    }

    public List<Question> findByDueStatus(boolean due) {
        if (due) {
            return findWithSql("""
                SELECT q.* FROM questions q
                JOIN spaced_repetition sr ON q.id = sr.question_id
                WHERE sr.next_review_at <= datetime('now','localtime')
                ORDER BY sr.next_review_at ASC
            """, new Object[0]);
        }
        return findWithSql("""
            SELECT q.* FROM questions q
            JOIN spaced_repetition sr ON q.id = sr.question_id
            WHERE sr.next_review_at > datetime('now','localtime')
            ORDER BY sr.next_review_at ASC
        """, new Object[0]);
    }

    public void logReview(int questionId, boolean correct, int responseSeconds) {
        String sql = "INSERT INTO review_logs (question_id, correct, response_seconds) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setInt(2, correct ? 1 : 0);
            ps.setInt(3, responseSeconds);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to log review", e);
        }
    }

    public SpacedRepetition getSpacedRepetition(int questionId) {
        String sql = "SELECT * FROM spaced_repetition WHERE question_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                var sr = new SpacedRepetition();
                sr.setQuestionId(rs.getInt("question_id"));
                sr.setEaseFactor(rs.getDouble("ease_factor"));
                sr.setIntervalDays(rs.getInt("interval_days"));
                sr.setRepetitions(rs.getInt("repetitions"));
                String nextReview = rs.getString("next_review_at");
                if (nextReview != null) {
                    sr.setNextReviewAt(LocalDateTime.parse(nextReview, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                return sr;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get spaced repetition", e);
        }
        var sr = new SpacedRepetition();
        sr.setQuestionId(questionId);
        return sr;
    }

    public void updateSpacedRepetition(SpacedRepetition sr) {
        String sql = """
            INSERT INTO spaced_repetition (question_id, ease_factor, interval_days, repetitions, next_review_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(question_id) DO UPDATE SET
                ease_factor = excluded.ease_factor,
                interval_days = excluded.interval_days,
                repetitions = excluded.repetitions,
                next_review_at = excluded.next_review_at
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sr.getQuestionId());
            ps.setDouble(2, sr.getEaseFactor());
            ps.setInt(3, sr.getIntervalDays());
            ps.setInt(4, sr.getRepetitions());
            ps.setString(5, sr.getNextReviewAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update spaced repetition", e);
        }
    }

    public int countByTag() {
        return count("SELECT COUNT(DISTINCT tags) FROM questions WHERE tags != ''");
    }

    public List<String> allTags() {
        var tags = new ArrayList<String>();
        String sql = "SELECT DISTINCT tags FROM questions WHERE tags != ''";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String val = rs.getString("tags");
                if (val != null) {
                    for (String t : val.split(",")) {
                        String trimmed = t.trim();
                        if (!trimmed.isEmpty() && !tags.contains(trimmed)) {
                            tags.add(trimmed);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tags", e);
        }
        return tags;
    }

    public int delete(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete question", e);
        }
    }

    // -- helpers --

    private List<Question> findWithSql(String sql, Object[] params) {
        var list = new ArrayList<Question>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            var rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Query failed", e);
        }
        return list;
    }

    private int count(String sql) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException("Count failed", e);
        }
    }

    private Question mapRow(ResultSet rs) throws Exception {
        var q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuestion(rs.getString("question"));
        q.setAnswer(rs.getString("answer"));
        q.setTags(rs.getString("tags"));
        q.setDifficulty(rs.getInt("difficulty"));
        q.setSource(rs.getString("source"));
        String ca = rs.getString("created_at");
        if (ca != null) q.setCreatedAt(LocalDateTime.parse(ca, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String ua = rs.getString("updated_at");
        if (ua != null) q.setUpdatedAt(LocalDateTime.parse(ua, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return q;
    }
}
