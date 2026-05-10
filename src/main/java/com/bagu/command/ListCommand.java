package com.bagu.command;

import com.bagu.db.QuestionDao;
import com.bagu.model.Question;
import com.bagu.util.Ansi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list", description = "列出题目", aliases = {"ls", "l"})
public class ListCommand implements Callable<Integer> {

    @Option(names = {"-t", "--tag"}, description = "按标签筛选")
    String tag;

    @Option(names = {"--due"}, description = "只显示待复习的题目")
    boolean due;

    @Option(names = {"--nondue"}, description = "只显示不需要复习的题目")
    boolean nonDue;

    @Option(names = {"-a", "--all"}, description = "显示详细信息")
    boolean all;

    @Override
    public Integer call() {
        QuestionDao dao = new QuestionDao();
        List<Question> questions;

        if (tag != null) {
            questions = dao.findByTag(tag);
        } else if (due) {
            questions = dao.findByDueStatus(true);
        } else if (nonDue) {
            questions = dao.findByDueStatus(false);
        } else {
            questions = dao.findAll();
        }

        if (questions.isEmpty()) {
            System.out.println(Ansi.yellow("没有找到题目"));
            return 0;
        }

        System.out.println(Ansi.bold("\n📋 题目列表 (" + questions.size() + " 题)\n"));

        int i = 1;
        for (Question q : questions) {
            String tags = q.getTags() != null && !q.getTags().isBlank()
                    ? Ansi.cyan("[" + q.getTags() + "]") : "";
            String dueMarker = isDue(q.getId()) ? Ansi.yellow(" ⏰待复习") : "";
            System.out.printf("  %s#%d%s %s%s%n",
                    Ansi.dim(String.format("%2d.", i++)),
                    q.getId(), Ansi.RESET,
                    q.getQuestion(), dueMarker);
            if (all) {
                System.out.println("      " + Ansi.dim("答案: ") + truncate(q.getAnswer(), 80));
                System.out.println("      " + Ansi.dim("难度: ") + Ansi.formatDifficulty(q.getDifficulty())
                        + "  " + Ansi.dim("标签: ") + tags);
            } else {
                System.out.println("      " + Ansi.dim("难度: ") + Ansi.formatDifficulty(q.getDifficulty())
                        + "  " + tags);
            }
            System.out.println();
        }

        return 0;
    }

    private boolean isDue(int questionId) {
        QuestionDao dao = new QuestionDao();
        var sr = dao.getSpacedRepetition(questionId);
        return sr.isDue();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        String oneLine = s.replace("\n", " ");
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max) + "...";
    }
}
