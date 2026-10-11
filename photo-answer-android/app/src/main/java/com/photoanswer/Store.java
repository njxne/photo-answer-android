package com.photoanswer;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 设置与历史记录（SharedPreferences）。 */
public final class Store {

    public static final String K_BASE = "base_url";
    public static final String K_KEY = "api_key";
    public static final String K_MODEL = "model";
    public static final String K_GRADE = "grade";
    public static final String K_SUBJECT = "subject";
    public static final String K_HISTORY = "history";

    public static final String DEF_BASE = "https://ai.murk.top/v1/chat/completions";
    public static final String DEF_MODEL = "deepseek-flash";

    private static final int MAX_HISTORY = 50;

    private Store() {
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("photoanswer", Context.MODE_PRIVATE);
    }

    /** 供界面监听设置变化（如年级改了要立刻刷新）。 */
    public static SharedPreferences prefs(Context c) {
        return sp(c);
    }

    public static String get(Context c, String k, String def) {
        return sp(c).getString(k, def);
    }

    public static void set(Context c, String k, String v) {
        sp(c).edit().putString(k, v).apply();
    }

    public static List<Object> history(Context c) {
        Object o = Json.parse(get(c, K_HISTORY, "[]"));
        List<Object> l = Json.asArr(o);
        return l == null ? new ArrayList<>() : l;
    }

    public static void saveHistory(Context c, List<Object> items) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) b.append(',');
            b.append(encode(Json.asObj(items.get(i))));
        }
        b.append(']');
        set(c, K_HISTORY, b.toString());
    }

    public static void addHistory(Context c, Map<String, Object> item) {
        List<Object> l = history(c);
        l.add(0, item);
        while (l.size() > MAX_HISTORY) {
            deleteThumb(Json.asObj(l.remove(l.size() - 1)));
        }
        saveHistory(c, l);
    }

    public static void removeHistory(Context c, Map<String, Object> item) {
        List<Object> l = history(c);
        l.remove(item);
        deleteThumb(item);
        saveHistory(c, l);
    }

    public static void clearHistory(Context c) {
        for (Object o : history(c)) deleteThumb(Json.asObj(o));
        set(c, K_HISTORY, "[]");
    }

    private static void deleteThumb(Map<String, Object> m) {
        if (m == null) return;
        String p = Json.str(m.get("img"));
        if (p != null && !p.isEmpty()) {
            try {
                new File(p).delete();
            } catch (Throwable ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String encode(Map<String, Object> m) {
        StringBuilder b = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) b.append(',');
            first = false;
            b.append('"').append(Json.esc(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Boolean) {
                b.append(v);
            } else {
                b.append('"').append(Json.esc(v == null ? "" : v.toString())).append('"');
            }
        }
        return b.append('}').toString();
    }
}
