package com.bagu.command;

import com.bagu.db.QuestionDao;
import com.bagu.util.Ansi;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "stats", description = "学习统计", aliases = {"st"})
public class StatsCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        QuestionDao dao = new QuestionDao();

        int total = dao.count();
        int due = dao.countDue();
        int reviewedToday = dao.countReviewedToday();
        int correctToday = dao.countCorrectToday();
        double accuracy = dao.overallAccuracy();
        List<String> tags = dao.allTags();

        System.out.println(Ansi.bold("\n📊 学习统计\n"));

        // Overview
        System.out.println("  " + Ansi.bold("题库概况"));
        System.out.println("    📝 总题目数:    " + total);
        System.out.println("    🏷️  标签数:     " + tags.size());
        System.out.println("    ⏰ 待复习:      " + (due > 0 ? Ansi.yellow(String.valueOf(due)) : Ansi.green("0")));
        System.out.println();

        // Today
        int wrongToday = reviewedToday - correctToday;
        System.out.println("  " + Ansi.bold("今日进度"));
        System.out.println("    📖 已复习:      " + reviewedToday + " 题");
        if (reviewedToday > 0) {
            System.out.println("    ✅ 正确:        " + correctToday);
            System.out.println("    ❌ 错误:        " + wrongToday);
            System.out.println("    📈 今日正确率:  " + String.format("%.1f%%", correctToday * 100.0 / reviewedToday));
        }
        System.out.println();

        // Overall
        if (total > 0) {
            System.out.println("  " + Ansi.bold("总体"));
            double avgAccuracy = dao.overallAccuracy();
            String accColor;
            if (avgAccuracy >= 80) accColor = Ansi.GREEN;
            else if (avgAccuracy >= 50) accColor = Ansi.YELLOW;
            else accColor = Ansi.RED;
            System.out.println("    🎯 总正确率:    " + accColor + String.format("%.1f%%", avgAccuracy) + Ansi.RESET);
        }

        // Tags
        if (!tags.isEmpty()) {
            System.out.println();
            System.out.println("  " + Ansi.bold("标签分布"));
            for (String tag : tags) {
                int count = dao.findByTag(tag.trim()).size();
                System.out.println("    " + Ansi.cyan(tag.trim()) + ": " + count + " 题");
            }
        }

        // Motivation
        System.out.println();
        if (due == 0 && reviewedToday >= 5) {
            System.out.println(Ansi.green("  🎉 今日任务全部完成！休息一下吧。"));
        } else if (due > 0) {
            System.out.println(Ansi.yellow("  💪 还有 " + due + " 道题待复习，加油！"));
        } else {
            System.out.println(Ansi.dim("  用 bagu add 添加题目开始学习吧！"));
        }
        System.out.println();

        return 0;
    }
}
