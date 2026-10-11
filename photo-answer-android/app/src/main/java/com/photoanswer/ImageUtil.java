package com.photoanswer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 图片解码 / 压缩 / 缩略图。 */
public final class ImageUtil {

    /** 上传图的长边上限。作业本上的字很小，分辨率不够就会糊成一片。 */
    public static final int UPLOAD_MAX_SIDE = 2048;

    /** 单张 base64 的体积预算（字节）。超了就自动降质量，避免请求体过大被拒。 */
    private static final int BUDGET_BYTES = 1_200_000;

    /** 依次尝试的质量档位：从清晰到模糊，取第一个不超预算的。 */
    private static final int[] QUALITY_STEPS = {95, 92, 88, 82, 75};

    /** 质量都压不下去时，依次降到的长边。 */
    private static final int[] SIDE_STEPS = {2048, 1600, 1280, 1024};

    private ImageUtil() {
    }

    /** 解码并按最长边缩放到 maxSide。 */
    public static Bitmap decode(Context c, Uri uri, int maxSide) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        InputStream in = open(c, uri);
        BitmapFactory.decodeStream(in, null, bounds);
        close(in);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("图片读不出来");
        }
        // inSampleSize 只能取 2 的幂，直接套会让 3000px 的图被砍到 1500px 再拉回 2048，
        // 白白损失细节。所以降到「略大于目标」即可，剩下的交给下面高质量缩放。
        int sample = 1;
        int max = Math.max(bounds.outWidth, bounds.outHeight);
        while (max / (sample * 2) >= maxSide) sample *= 2;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = sample;
        InputStream in2 = open(c, uri);
        Bitmap bm = BitmapFactory.decodeStream(in2, null, opt);
        close(in2);
        if (bm == null) throw new IOException("图片解码失败");

        int m = Math.max(bm.getWidth(), bm.getHeight());
        if (m > maxSide) {
            float s = maxSide / (float) m;
            Bitmap scaled = Bitmap.createScaledBitmap(bm,
                    Math.max(1, Math.round(bm.getWidth() * s)),
                    Math.max(1, Math.round(bm.getHeight() * s)), true);
            if (scaled != bm) bm.recycle();
            bm = scaled;
        }
        return bm;
    }

    /**
     * 上传给模型用的 base64（JPEG），自适应体积与内存。
     *
     * 先按最高清晰度压，超出预算才逐级降级 —— 拍题图片以文字为主，
     * JPEG 质量一低边缘就糊、模型认不出，所以默认往清楚了压。
     *
     * 编码完立刻回收 Bitmap：9 张 2048 的图同时驻留会 OOM。
     */
    public static String jpegBase64(Context c, Uri uri) throws IOException {
        Bitmap full = decode(c, uri, UPLOAD_MAX_SIDE);
        byte[] best = null;
        try {
            for (int side : SIDE_STEPS) {
                Bitmap bm = (side == UPLOAD_MAX_SIDE) ? full : resize(full, side);
                try {
                    for (int q : QUALITY_STEPS) {
                        byte[] j = toJpeg(bm, q);
                        // base64 后体积约为原始的 4/3
                        if (j.length * 4 / 3 <= BUDGET_BYTES) return b64(j);
                        if (best == null) best = j;
                    }
                } finally {
                    if (bm != full) bm.recycle();
                }
            }
            return best == null ? b64(toJpeg(full, 75)) : b64(best);
        } finally {
            full.recycle();
        }
    }

    private static byte[] toJpeg(Bitmap bm, int quality) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, bo);
        return bo.toByteArray();
    }

    private static String b64(byte[] b) {
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    /** 等比缩到长边 side。 */
    private static Bitmap resize(Bitmap src, int side) {
        int m = Math.max(src.getWidth(), src.getHeight());
        if (m <= side) return src;
        float s = side / (float) m;
        return Bitmap.createScaledBitmap(src,
                Math.max(1, Math.round(src.getWidth() * s)),
                Math.max(1, Math.round(src.getHeight() * s)), true);
    }

    /** 缩略图，用于界面预览。 */
    public static Bitmap thumb(Context c, Uri uri, int size) {
        try {
            return decode(c, uri, size);
        } catch (Throwable t) {
            return null;
        }
    }

    /** 把图片存成历史记录里的小图，返回路径。 */
    public static String saveThumb(Context c, Uri uri, File dir, String name) {
        try {
            Bitmap bm = decode(c, uri, 360);
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, name);
            FileOutputStream fo = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 75, fo);
            fo.close();
            bm.recycle();
            return f.getAbsolutePath();
        } catch (Throwable t) {
            return null;
        }
    }

    private static InputStream open(Context c, Uri uri) throws IOException {
        InputStream in = c.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("打不开图片");
        return in;
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }
}
