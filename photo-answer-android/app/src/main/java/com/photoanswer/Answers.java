package com.photoanswer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 把模型返回的整份答案按「#第N题」切成单题，用于本地切换浏览。 */
public final class Answers {

    /** 行首的「#第N题」标记，容错 Markdown 余留符号与空格。 */
    private static final Pattern MARK = Pattern.compile(
            "^[\\s>*\\-]*#*\\s*第\\s*(\\d+)\\s*题\\s*#*\\s*[:：、.]?\\s*$");

    private Answers() {
    }

    public static List<Part> split(String raw) {
        List<Part> all = new ArrayList<>();
        if (raw == null) return all;
        String text = raw.replace("\r\n", "\n").replace('\r', '\n');

        String[] lines = text.split("\n", -1);
        StringBuilder body = new StringBuilder();
        Integer curNum = null;
        boolean sawMark = false;

        for (String line : lines) {
            Matcher m = MARK.matcher(strip(line));
            if (m.matches()) {
                if (curNum != null) add(all, curNum, body);
                sawMark = true;
                curNum = Integer.parseInt(m.group(1));
                body = new StringBuilder();
                continue;
            }
            // 第一个标记之前的内容（模型偶尔先寒暄一句）丢掉
            if (!sawMark) continue;
            body.append(line).append('\n');
        }
        if (curNum != null) add(all, curNum, body);

        if (all.isEmpty() && !text.trim().isEmpty()) {
            all.add(new Part(1, trimEdges(text)));
        }
        return all;
    }

    private static final Pattern JUNK = Pattern.compile("[\\s>*_`~]+");

    /** 去掉行首尾的 Markdown 装饰与空白，便于匹配纯净的题号行。 */
    private static String strip(String line) {
        return JUNK.matcher(line).replaceAll("").trim();
    }

    /** 空内容的题不要出现在界面上。 */
    private static void add(List<Part> out, int num, StringBuilder body) {
        String s = trimEdges(body.toString());
        if (!s.trim().isEmpty()) out.add(new Part(num, s));
    }

    private static String trimEdges(String s) {
        String[] lines = s.split("\n", -1);
        int a = 0;
        int b = lines.length - 1;
        while (a <= b && lines[a].trim().isEmpty()) a++;
        while (b >= a && lines[b].trim().isEmpty()) b--;
        if (a > b) return "";
        StringBuilder out = new StringBuilder();
        for (int i = a; i <= b; i++) {
            out.append(lines[i]);
            if (i < b) out.append('\n');
        }
        return out.toString().trim() + "\n";
    }

    public static class Part {
        public final int num;
        public final String body;

        Part(int num, String body) {
            this.num = num;
            this.body = body;
        }
    }
}
