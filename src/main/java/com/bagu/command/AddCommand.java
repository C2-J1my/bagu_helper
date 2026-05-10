package com.bagu.command;

import com.bagu.db.QuestionDao;
import com.bagu.model.Question;
import com.bagu.util.Ansi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "add", description = "添加新题目", aliases = {"a"})
public class AddCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "题目内容", defaultValue = "")
    String questionText;

    @Parameters(index = "1", description = "答案内容", defaultValue = "")
    String answerText;

    @Option(names = {"-t", "--tags"}, description = "标签，逗号分隔，如: JVM,并发")
    String tags;

    @Option(names = {"-d", "--difficulty"}, description = "难度 1-5", defaultValue = "2")
    int difficulty;

    @Option(names = {"-s", "--source"}, description = "来源")
    String source;

    @Option(names = {"-i", "--interactive"}, description = "交互式添加")
    boolean interactive;

    @Override
    public Integer call() {
        QuestionDao dao = new QuestionDao();

        if (interactive || (questionText.isEmpty() && answerText.isEmpty())) {
            return interactiveAdd(dao);
        }

        if (answerText.isEmpty()) {
            System.out.println(Ansi.red("❌ 答案不能为空"));
            System.out.println("用法: bagu add \"题目\" \"答案\" [--tags tags] [--difficulty 1-5]");
            return 1;
        }

        Question q = new Question(questionText, answerText, tags != null ? tags : "", difficulty);
        if (source != null) q.setSource(source);
        dao.insert(q);
        System.out.println(Ansi.green("✅ 已添加题目 #" + q.getId()));
        return 0;
    }

    private int interactiveAdd(QuestionDao dao) {
        Scanner sc = new Scanner(System.in);
        System.out.println(Ansi.bold("\n📝 添加新题目\n"));

        System.out.print("题目: ");
        String qText = sc.nextLine().trim();
        if (qText.isEmpty()) {
            System.out.println(Ansi.red("题目不能为空"));
            return 1;
        }

        System.out.println("答案 (输入 . 结束):");
        StringBuilder answer = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).equals(".")) {
            answer.append(line).append("\n");
        }

        System.out.print("标签 (逗号分隔，如: JVM,并发): ");
        String tagInput = sc.nextLine().trim();

        System.out.print("难度 (1-5): ");
        int diff = 2;
        try {
            diff = Integer.parseInt(sc.nextLine().trim());
            if (diff < 1 || diff > 5) diff = 2;
        } catch (NumberFormatException ignored) {}

        Question q = new Question(qText, answer.toString().trim(), tagInput, diff);
        dao.insert(q);
        System.out.println(Ansi.green("✅ 已添加题目 #" + q.getId()));
        return 0;
    }
}
