package com.bagu;

import com.bagu.command.*;
import com.bagu.db.DatabaseManager;
import com.bagu.util.Ansi;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "bagu",
    description = "Java 后端面试八股记忆工具  @|bold,green 按 Enter 查看答案，自评对错，间隔重复巩固记忆|@",
    version = "bagu 1.0.0",
    mixinStandardHelpOptions = true,
    subcommands = {
        AddCommand.class,
        ListCommand.class,
        QuizCommand.class,
        ReviewCommand.class,
        StatsCommand.class
    }
)
public class BaguApplication implements Runnable {

    public static void main(String[] args) {
        DatabaseManager.initialize();
        int exitCode = new CommandLine(new BaguApplication())
                .setColorScheme(CommandLine.Help.defaultColorScheme(
                        CommandLine.Help.Ansi.ON))
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Default: show welcome + random/due question
        printBanner();
        QuizCommand.showRandom();
    }

    private void printBanner() {
        System.out.println();
        System.out.println(Ansi.bold("   ____    _    ____  _   _"));
        System.out.println(Ansi.bold("  | __ )  / \\  / ___|| | | |"));
        System.out.println(Ansi.bold("  |  _ \\ / _ \\| |  _| | | |"));
        System.out.println(Ansi.bold("  | |_) / ___ \\ |_| | |_| |"));
        System.out.println(Ansi.bold("  |____/_/   \\_\\____|\\___/ "));
        System.out.println(Ansi.cyan("  八股助手 v1.0.0  —  Java 后端面试伴侣"));
        System.out.println(Ansi.dim("  输入 bagu --help 查看所有命令"));
        System.out.println();
    }
}
