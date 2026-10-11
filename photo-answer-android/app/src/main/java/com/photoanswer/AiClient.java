package com.photoanswer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** OpenAI 兼容的 chat/completions 与 models 客户端。 */
public final class AiClient {

    private AiClient() {
    }

    /** 取可用模型列表（GET /v1/models）。 */
    public static List<String> listModels(String baseUrl, String apiKey) throws IOException {
        String resp = request("GET", modelsUrl(baseUrl), apiKey, null, 30000);
        Object root = Json.parse(resp);
        if (root == null) throw new IOException("返回内容无法解析：" + brief(resp));
        String err = errorOf(root, resp);
        if (err != null) throw new IOException(err);
        List<Object> data = Json.asArr(Json.get(root, "data"));
        List<String> out = new ArrayList<>();
        if (data != null) {
            for (Object o : data) {
                String id = Json.str(Json.get(o, "id"));
                if (id != null && !id.isEmpty()) out.add(id);
            }
        }
        if (out.isEmpty()) throw new IOException("接口没有返回模型列表");
        return out;
    }

    /** 发一次对话请求，返回答案文本。 */
    public static String chat(String baseUrl, String apiKey, String model,
                              String system, String userText, List<String> imagesB64) throws IOException {
        List<Object> content = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        if (userText != null && !userText.trim().isEmpty()) {
            text.append(userText.trim());
        }
        if (imagesB64 != null && !imagesB64.isEmpty()) {
            if (text.length() > 0) text.append("\n\n");
            text.append("（下方共 ").append(imagesB64.size()).append(" 张图片，按顺序阅读）");
        }
        if (text.length() == 0) text.append("请按系统指令作答。");
        content.add(Json.map("type", "text", "text", text.toString()));
        if (imagesB64 != null) {
            for (String b64 : imagesB64) {
                content.add(Json.map("type", "image_url",
                        "image_url", Json.map("url", "data:image/jpeg;base64," + b64)));
            }
        }

        List<Object> messages = new ArrayList<>();
        if (system != null && !system.isEmpty()) {
            messages.add(Json.map("role", "system", "content", system));
        }
        messages.add(Json.map("role", "user", "content", content));

        Map<String, Object> body = Json.map(
                "model", model,
                "messages", messages,
                "temperature", 0.2,
                "stream", false);

        String resp = request("POST", chatUrl(baseUrl), apiKey, toJson(body), 180000);
        Object root = Json.parse(resp);
        if (root == null) throw new IOException("返回内容无法解析：" + brief(resp));
        String err = errorOf(root, resp);
        if (err != null) throw new IOException(err);
        Object contentOut = Json.get(root, "choices", 0, "message", "content");
        if (contentOut == null) throw new IOException("返回里没有答案：" + brief(resp));
        String out = String.valueOf(contentOut).trim();
        if (out.isEmpty()) throw new IOException("模型返回了空答案");
        return out;
    }

    // ==================== 地址拼装 ====================

    private static String base(String baseUrl) {
        String u = baseUrl == null ? "" : baseUrl.trim();
        if (u.isEmpty()) throw new IllegalArgumentException("还没填接口地址");
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return u;
    }

    private static String chatUrl(String baseUrl) {
        String u = base(baseUrl);
        if (u.endsWith("/chat/completions")) return u;
        if (u.endsWith("/v1")) return u + "/chat/completions";
        return u + "/v1/chat/completions";
    }

    private static String modelsUrl(String baseUrl) {
        String u = base(baseUrl);
        String tail = "/chat/completions";
        if (u.endsWith(tail)) u = u.substring(0, u.length() - tail.length());
        if (u.endsWith("/models")) return u;
        if (u.endsWith("/v1")) return u + "/models";
        return u + "/v1/models";
    }

    // ==================== HTTP ====================

    private static String request(String method, String url, String apiKey,
                                  String payload, int timeoutMs) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setRequestMethod(method);
            c.setConnectTimeout(20000);
            c.setReadTimeout(timeoutMs);
            c.setRequestProperty("Accept", "application/json");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                c.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            }
            if (payload != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                byte[] data = payload.getBytes(StandardCharsets.UTF_8);
                c.setFixedLengthStreamingMode(data.length);
                OutputStream os = c.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            }
            int code = c.getResponseCode();
            InputStream in = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
            String body = readAll(in);
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code + "：" + apiError(body));
            }
            return body;
        } finally {
            c.disconnect();
        }
    }

    private static String errorOf(Object root, String body) {
        Object err = Json.get(root, "error");
        if (err == null) return null;
        String msg = Json.str(Json.get(err, "message"));
        if (msg == null) msg = Json.str(Json.get(root, "message"));
        return "接口报错：" + (msg == null ? brief(body) : msg);
    }

    private static String apiError(String body) {
        Object root = Json.parse(body);
        String msg = Json.str(Json.get(root, "error", "message"));
        if (msg == null) msg = Json.str(Json.get(root, "message"));
        return (msg != null && !msg.isEmpty()) ? msg : brief(body);
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        in.close();
        return new String(bo.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String brief(String s) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > 200 ? t.substring(0, 200) + "…" : t;
    }

    // ==================== 序列化 ====================

    @SuppressWarnings("unchecked")
    private static String toJson(Object o) {
        StringBuilder b = new StringBuilder();
        write(b, o);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder b, Object o) {
        if (o == null) {
            b.append("null");
        } else if (o instanceof String) {
            b.append('"').append(Json.esc((String) o)).append('"');
        } else if (o instanceof Boolean || o instanceof Number) {
            b.append(o.toString());
        } else if (o instanceof Map) {
            b.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet()) {
                if (!first) b.append(',');
                first = false;
                b.append('"').append(Json.esc(e.getKey())).append("\":");
                write(b, e.getValue());
            }
            b.append('}');
        } else if (o instanceof List) {
            b.append('[');
            boolean first = true;
            for (Object e : (List<Object>) o) {
                if (!first) b.append(',');
                first = false;
                write(b, e);
            }
            b.append(']');
        } else {
            b.append('"').append(Json.esc(o.toString())).append('"');
        }
    }
}
