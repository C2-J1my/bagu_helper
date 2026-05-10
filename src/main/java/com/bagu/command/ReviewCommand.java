package com.bagu.command;

import com.bagu.db.QuestionDao;
import com.bagu.util.Ansi;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "review", description = "复习待复习题目", aliases = {"r"})
public class ReviewCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        QuestionDao dao = new QuestionDao();
        int dueCount = dao.countDue();
        if (dueCount == 0) {
            System.out.println(Ansi.green("\n✅ 没有待复习的题目！\n"));
            return 0;
        }
        System.out.println(Ansi.cyan("\n📚 你有 " + dueCount + " 道题待复习\n"));
        QuizCommand.showDue();
        return 0;
    }
}
