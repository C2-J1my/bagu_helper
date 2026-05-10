package com.bagu.util;

public class Ansi {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String MAGENTA = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";
    public static final String BG_RED = "\033[41m";
    public static final String BG_GREEN = "\033[42m";
    public static final String BG_YELLOW = "\033[43m";
    public static final String BG_BLUE = "\033[44m";

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String red(String text) {
        return RED + text + RESET;
    }

    public static String green(String text) {
        return GREEN + text + RESET;
    }

    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }

    public static String cyan(String text) {
        return CYAN + text + RESET;
    }

    public static String blue(String text) {
        return BLUE + text + RESET;
    }

    public static String dim(String text) {
        return DIM + text + RESET;
    }

    public static String magenta(String text) {
        return MAGENTA + text + RESET;
    }

    public static String formatDifficulty(int diff) {
        return switch (diff) {
            case 1 -> green("★") + dim("★★★★");
            case 2 -> green("★★") + dim("★★★");
            case 3 -> yellow("★★★") + dim("★★");
            case 4 -> yellow("★★★★") + dim("★");
            case 5 -> red("★★★★★");
            default -> green("★") + dim("★★★★");
        };
    }
}
