package com.bagu.command;

import com.bagu.db.QuestionDao;
import com.bagu.service.QuizService;
import com.bagu.util.Ansi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "quiz", description = "进入 Quiz 模式，连续出题", aliases = {"q"})
public class QuizCommand implements Callable<Integer> {

    @Option(names = {"-c", "--count"}, description = "出题数量", defaultValue = "5")
    int count;

    @Override
    public Integer call() {
        QuestionDao dao = new QuestionDao();
        QuizService quiz = new QuizService(dao);
        quiz.runQuiz(count);
        return 0;
    }

    /** Used by default `bagu` command to show a random/due question */
    public static void showRandom() {
        QuestionDao dao = new QuestionDao();
        QuizService quiz = new QuizService(dao);
        quiz.showRandom();
    }

    /** Used by `bagu review` to show due questions */
    public static void showDue() {
        QuestionDao dao = new QuestionDao();
        QuizService quiz = new QuizService(dao);
        quiz.reviewDue();
    }
}
