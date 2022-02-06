package it.oha.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    public static void debug(String fmt, Object... args) {
        log(DEBUG, fmt, args);
    }

    public static void info(String fmt, Object... args) {
        log(INFO, fmt, args);
    }

    public static void notice(String fmt, Object... args) {
        log(NOTICE, fmt, args);
    }

    public static void warning(String fmt, Object... args) {
        log(WARNING, fmt, args);
    }

    public static void error(String fmt, Object... args) {
        log(ERROR, fmt, args);
    }

    synchronized private static void log(Level level, String fmt, Object[] args) {
        var t = Thread.currentThread();
        var st = t.getStackTrace();
        System.out.printf("%s %s %s -- %s:%d -- ",
                df.format(new Date()),
                level.label, t.getName(),
                st[3].getFileName(), st[3].getLineNumber());
        System.out.printf(fmt, args);
        System.out.println();
    }

    private static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    private record Level(
            int level,
            String label
    ) {

    }

    public static Level DEBUG = new Level(1, "debug");
    public static Level INFO = new Level(2, "info");
    public static Level NOTICE = new Level(3, "notice");
    public static Level WARNING = new Level(4, "warning");
    public static Level ERROR = new Level(5, "error");
}
