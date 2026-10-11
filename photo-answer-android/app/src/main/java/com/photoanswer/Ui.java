package com.photoanswer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 杂项小工具。 */
public final class Ui {

    private Ui() {
    }

    public static void copy(Context c, String s) {
        if (s == null || s.isEmpty()) {
            Toast.makeText(c, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("answer", s));
        Toast.makeText(c, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context c, CharSequence s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context c, int res) {
        Toast.makeText(c, res, Toast.LENGTH_SHORT).show();
    }

    public static String modeLabel(String mode) {
        if ("grade".equals(mode)) return "改作文";
        if ("write".equals(mode)) return "生作文";
        return "拍题";
    }

    public static long asLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Throwable e) {
            return 0L;
        }
    }

    public static String time(long ms) {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(ms));
    }
}
