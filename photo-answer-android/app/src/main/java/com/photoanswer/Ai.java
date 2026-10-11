package com.photoanswer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 统一的「发一次请求并落历史」流程。 */
public final class Ai {

    public interface Cb {
        void onStart(String statusText);

        void onDone(String answer);

        void onFail(String msg);
    }

    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    private static volatile boolean busy = false;

    private Ai() {
    }

    public static void run(Activity act, String mode, List<Uri> uris, String text, Cb cb) {
        final Context ctx = act.getApplicationContext();
        final String base = Store.get(ctx, Store.K_BASE, Store.DEF_BASE);
        final String key = Store.get(ctx, Store.K_KEY, "");
        final String model = Store.get(ctx, Store.K_MODEL, Store.DEF_MODEL);
        final String grade = Store.get(ctx, Store.K_GRADE, "三年级");
        final String subject = Store.get(ctx, Store.K_SUBJECT, "自动识别");

        if (key.trim().isEmpty()) {
            cb.onFail(ctx.getString(R.string.need_key));
            return;
        }
        if (busy) {
            cb.onFail("正在处理，请稍等");
            return;
        }

        final String system;
        if ("solve".equals(mode)) {
            system = Prompt.solve(grade, subject, uris == null ? 0 : uris.size());
        } else if ("grade".equals(mode)) {
            system = Prompt.gradeEssay(grade);
        } else {
            system = Prompt.writeEssay(grade, text == null ? "" : text);
        }

        // 每次作答都是一条全新的对话：只带 system + 一条 user，不带任何历史轮次，
        // 保证上一份作业不会串进这一次的上下文。
        busy = true;
        cb.onStart(ctx.getString(R.string.working));

        POOL.execute(() -> {
            try {
                List<String> b64 = new ArrayList<>();
                if (uris != null) {
                    for (Uri u : uris) b64.add(ImageUtil.jpegBase64(ctx, u));
                }
                final String answer = AiClient.chat(base, key, model, system, text, b64);

                String thumb = null;
                if (uris != null && !uris.isEmpty()) {
                    thumb = ImageUtil.saveThumb(ctx, uris.get(0),
                            new File(ctx.getFilesDir(), "hist"),
                            "h" + System.currentTimeMillis() + ".jpg");
                }
                String summary = text == null ? "" : text.trim();
                if (summary.isEmpty()) {
                    summary = (uris == null || uris.isEmpty()) ? "" : (uris.size() + " 张图片");
                }
                final String sum = summary;
                final String thumbPath = thumb;

                act.runOnUiThread(() -> {
                    busy = false;
                    Store.addHistory(ctx, Json.map(
                            "t", System.currentTimeMillis(),
                            "mode", mode,
                            "grade", grade,
                            "sub", subject,
                            "q", sum,
                            "img", thumbPath == null ? "" : thumbPath,
                            "a", answer));
                    cb.onDone(answer);
                });
            } catch (final Throwable t) {
                final String msg = t.getMessage() == null
                        ? t.getClass().getSimpleName() : t.getMessage();
                act.runOnUiThread(() -> {
                    busy = false;
                    cb.onFail(msg);
                });
            }
        });
    }
}
