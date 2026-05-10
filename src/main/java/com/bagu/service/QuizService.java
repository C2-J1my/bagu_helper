package com.bagu.service;

import com.bagu.db.QuestionDao;
import com.bagu.model.Question;
import com.bagu.util.Ansi;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class QuizService {
    private final QuestionDao dao;
    private final SpacedRepetitionService srService;
    private final Scanner scanner;
    private int correctCount;
    private int totalCount;

    public QuizService(QuestionDao dao) {
        this.dao = dao;
        this.srService = new SpacedRepetitionService(dao);
        this.scanner = new Scanner(System.in);
    }

    /** Run a quiz session with N random questions */
    public void runQuiz(int count) {
        List<Question> all = dao.findAll();
        if (all.isEmpty()) {
            System.out.println(Ansi.yellow("\n📭 题库为空，先用 bagu add 添加题目吧！\n"));
            return;
        }

        System.out.println(Ansi.bold("\n╔══════════════════════════════════════╗"));
        System.out.println(Ansi.bold("║          Q u i z  模 式            ║"));
        System.out.println(Ansi.bold("╚══════════════════════════════════════╝\n"));

        java.util.Collections.shuffle(all);
        int showCount = Math.min(count, all.size());

        for (int i = 0; i < showCount; i++) {
            presentQuestion(all.get(i), i + 1, showCount);
        }

        printSummary(showCount);
    }

    /** Review due questions */
    public int reviewDue() {
        List<Question> due = dao.findByDueStatus(true);
        if (due.isEmpty()) {
            System.out.println(Ansi.green("\n✅ 所有题目都已复习，太棒了！\n"));
            return 0;
        }

        System.out.println(Ansi.cyan("\n📚 今日待复习: " + due.size() + " 题\n"));
        int reviewed = 0;
        for (int i = 0; i < due.size(); i++) {
            boolean answered = presentQuestion(due.get(i), i + 1, due.size());
            if (answered) reviewed++;
        }
        printSummary(reviewed);
        return reviewed;
    }

    /** Show a single random question (used by default `bagu` command) */
    public boolean showRandom() {
        Optional<Question> due = dao.nextDue();
        Question q;
        if (due.isPresent()) {
            q = due.get();
            System.out.println(Ansi.cyan("\n📚 你有待复习的题目！\n"));
        } else {
            var opt = dao.random();
            if (opt.isEmpty()) {
                System.out.println(Ansi.yellow("\n📭 题库为空，先用 bagu add 添加题目吧！\n"));
                return false;
            }
            q = opt.get();
        }
        return presentQuestion(q, 1, 1);
    }

    private boolean presentQuestion(Question q, int index, int total) {
        System.out.println(Ansi.bold("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        if (total > 1) {
            System.out.println(Ansi.dim("  " + index + "/" + total));
        }

        System.out.println(Ansi.bold("📌 题目: ") + q.getQuestion());
        System.out.println(Ansi.dim("   难度: ") + Ansi.formatDifficulty(q.getDifficulty())
                + Ansi.dim("  标签: ") + Ansi.cyan(formatTags(q)));

        System.out.print(Ansi.dim("\n⏎ 按 Enter 查看答案..."));
        String response = scanner.nextLine();
        boolean skipped = !response.trim().isEmpty() && response.trim().equalsIgnoreCase("skip");

        Instant start = Instant.now();

        System.out.println();
        System.out.println(Ansi.bold("💡 答案:"));
        for (String line : q.getAnswer().split("\n")) {
            System.out.println("  " + line);
        }
        System.out.println();

        if (q.getSource() != null && !q.getSource().isBlank()) {
            System.out.println(Ansi.dim("📖 来源: " + q.getSource()));
        }

        Instant end = Instant.now();
        int elapsed = (int) Duration.between(start, end).getSeconds();

        return askCorrect(q, elapsed);
    }

    private boolean askCorrect(Question q, int elapsed) {
        while (true) {
            System.out.print(Ansi.yellow("答对了吗？(y/n/skip): "));
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "y", "yes", "对", "对了" -> {
                    dao.logReview(q.getId(), true, elapsed);
                    srService.recordResult(q.getId(), true);
                    correctCount++;
                    totalCount++;
                    System.out.println(Ansi.green("✅ 记对了！继续保持！\n"));
                    return true;
                }
                case "n", "no", "错", "错了" -> {
                    dao.logReview(q.getId(), false, elapsed);
                    srService.recordResult(q.getId(), false);
                    totalCount++;
                    System.out.println(Ansi.red("❌ 记下来，明天会再出现！\n"));
                    return true;
                }
                case "skip", "s" -> {
                    System.out.println(Ansi.dim("已跳过\n"));
                    return false;
                }
                default -> System.out.println("请输入 y (对) / n (错) / skip (跳过)");
            }
        }
    }

    private void printSummary(int total) {
        if (total == 0) return;
        System.out.println(Ansi.bold("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        int wrong = total - correctCount;
        double pct = total > 0 ? (correctCount * 100.0 / total) : 0;
        String color = pct >= 80 ? Ansi.GREEN : (pct >= 50 ? Ansi.YELLOW : Ansi.RED);
        System.out.println("📊 " + Ansi.bold("本次结果: ")
                + Ansi.green(correctCount + " 正确") + " / "
                + Ansi.red(wrong + " 错误") + " / "
                + color + String.format("%.0f%% 正确率", pct));
        System.out.println(Ansi.dim("  总复习次数: " + dao.count()));
        System.out.println(Ansi.dim("  今日复习: " + dao.countReviewedToday() + " 题"));
        System.out.println();
    }

    private String formatTags(Question q) {
        String[] tags = q.getTagArray();
        if (tags.length == 0) return "无";
        return String.join(", ", tags);
    }
}
