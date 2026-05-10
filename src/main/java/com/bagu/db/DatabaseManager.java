package com.bagu.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_PATH;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite driver not found", e);
        }
        String home = System.getProperty("user.home");
        String baguDir = home + "/.bagu";
        new java.io.File(baguDir).mkdirs();
        DB_PATH = "jdbc:sqlite:" + baguDir + "/bagu.db";
    }

    public static Connection getConnection() throws Exception {
        Connection conn = DriverManager.getConnection(DB_PATH);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
        }
        return conn;
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question TEXT NOT NULL,
                    answer TEXT NOT NULL,
                    tags TEXT DEFAULT '',
                    difficulty INTEGER DEFAULT 1 CHECK(difficulty BETWEEN 1 AND 5),
                    source TEXT DEFAULT '',
                    created_at TEXT DEFAULT (datetime('now','localtime')),
                    updated_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS review_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question_id INTEGER NOT NULL,
                    correct INTEGER NOT NULL CHECK(correct IN (0,1)),
                    response_seconds INTEGER DEFAULT 0,
                    reviewed_at TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS spaced_repetition (
                    question_id INTEGER PRIMARY KEY,
                    ease_factor REAL DEFAULT 2.5,
                    interval_days INTEGER DEFAULT 0,
                    repetitions INTEGER DEFAULT 0,
                    next_review_at TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
                )
            """);
            // Ensure each question has a spaced_repetition row
            stmt.execute("""
                INSERT OR IGNORE INTO spaced_repetition (question_id)
                SELECT id FROM questions
            """);
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            System.exit(1);
        }
    }
}
