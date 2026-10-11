package com.photoanswer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 极简 JSON 编解码，够用即可。 */
public final class Json {

    private Json() {
    }

    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    b.append("\\\"");
                    break;
                case '\\':
                    b.append("\\\\");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\t':
                    b.append("\\t");
                    break;
                case '\b':
                    b.append("\\b");
                    break;
                case '\f':
                    b.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObj(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArr(Object o) {
        return (o instanceof List) ? (List<Object>) o : null;
    }

    public static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /** 解析入口：失败返回 null。 */
    public static Object parse(String s) {
        if (s == null) return null;
        try {
            P p = new P(s);
            Object v = p.read();
            return v;
        } catch (Throwable t) {
            return null;
        }
    }

    /** 沿路径取值，如 get(obj, "choices", 0, "message", "content")。 */
    public static Object get(Object root, Object... path) {
        Object cur = root;
        for (Object k : path) {
            if (cur == null) return null;
            if (k instanceof Integer) {
                List<Object> a = asArr(cur);
                if (a == null) return null;
                int i = (Integer) k;
                cur = (i >= 0 && i < a.size()) ? a.get(i) : null;
            } else {
                Map<String, Object> m = asObj(cur);
                if (m == null) return null;
                cur = m.get(String.valueOf(k));
            }
        }
        return cur;
    }

    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    public static List<Object> list(Object... items) {
        List<Object> l = new ArrayList<>();
        for (Object o : items) l.add(o);
        return l;
    }

    // ==================== 解析器 ====================

    private static final class P {
        private final String s;
        private int i;

        P(String s) {
            this.s = s;
        }

        Object read() {
            skip();
            if (i >= s.length()) return null;
            char c = s.charAt(i);
            switch (c) {
                case '{':
                    return object();
                case '[':
                    return array();
                case '"':
                    return string();
                case 't':
                    lit("true");
                    return Boolean.TRUE;
                case 'f':
                    lit("false");
                    return Boolean.FALSE;
                case 'n':
                    lit("null");
                    return null;
                default:
                    return number();
            }
        }

        private Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; // {
            skip();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return m;
            }
            while (i < s.length()) {
                skip();
                String k = string();
                skip();
                if (i < s.length() && s.charAt(i) == ':') i++;
                Object v = read();
                m.put(k, v);
                skip();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == '}') {
                    i++;
                    break;
                }
                break;
            }
            return m;
        }

        private List<Object> array() {
            List<Object> l = new ArrayList<>();
            i++; // [
            skip();
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return l;
            }
            while (i < s.length()) {
                l.add(read());
                skip();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == ']') {
                    i++;
                    break;
                }
                break;
            }
            return l;
        }

        private String string() {
            StringBuilder b = new StringBuilder();
            skip();
            if (i >= s.length() || s.charAt(i) != '"') return "";
            i++;
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    if (i >= s.length()) break;
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n':
                            b.append('\n');
                            break;
                        case 't':
                            b.append('\t');
                            break;
                        case 'r':
                            b.append('\r');
                            break;
                        case 'b':
                            b.append('\b');
                            break;
                        case 'f':
                            b.append('\f');
                            break;
                        case '/':
                            b.append('/');
                            break;
                        case '"':
                            b.append('"');
                            break;
                        case '\\':
                            b.append('\\');
                            break;
                        case 'u':
                            if (i + 4 <= s.length()) {
                                b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                                i += 4;
                            }
                            break;
                        default:
                            b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        private Object number() {
            int st = i;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E'
                        || (c >= '0' && c <= '9')) {
                    i++;
                } else {
                    break;
                }
            }
            String n = s.substring(st, i);
            try {
                if (n.contains(".") || n.contains("e") || n.contains("E")) {
                    return Double.parseDouble(n);
                }
                return Long.parseLong(n);
            } catch (Throwable t) {
                return n;
            }
        }

        private void lit(String w) {
            if (s.startsWith(w, i)) i += w.length();
        }

        private void skip() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    i++;
                } else {
                    break;
                }
            }
        }
    }
}
