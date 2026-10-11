package com.photoanswer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** 自建拍照界面（CameraX），不调用系统相机 App。产出一张存到相册的 content Uri。 */
public class CameraActivity extends AppCompatActivity {

    public static final String RESULT_URI = "uri";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private PreviewView previewView;
    private FloatingActionButton shutter;
    private ImageButton flash;

    private ProcessCameraProvider provider;
    private ImageCapture imageCapture;
    private boolean flashOn;

    private final ActivityResultLauncher<String[]> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                Boolean cam = r.get(Manifest.permission.CAMERA);
                if (cam != null && cam) {
                    startCamera();
                } else {
                    Ui.toast(this, R.string.no_camera_permission);
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        shutter = findViewById(R.id.btnShutter);
        flash = findViewById(R.id.btnFlash);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        flash.setOnClickListener(v -> toggleFlash());
        shutter.setOnClickListener(v -> takePhoto());

        if (hasPermission()) {
            startCamera();
        } else {
            permLauncher.launch(permArray());
        }
    }

    private boolean hasPermission() {
        for (String p : permArray()) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] permArray() {
        List<String> need = new ArrayList<>();
        need.add(Manifest.permission.CAMERA);
        // Android 9 及以下往 MediaStore 插图片需要写权限
        if (Build.VERSION.SDK_INT < 29) {
            need.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return need.toArray(new String[0]);
    }

    private void startCamera() {
        Future<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        Executor main = ContextCompat.getMainExecutor(this);
        pool.execute(() -> {
            ProcessCameraProvider got = null;
            Throwable failure = null;
            try {
                got = future.get();
            } catch (Throwable t) {
                failure = t;
            }
            final ProcessCameraProvider p = got;
            final Throwable err = failure;
            main.execute(() -> {
                if (p != null) {
                    provider = p;
                    bind();
                } else {
                    String msg = String.valueOf(err == null ? "unknown" : err.getMessage());
                    Toast.makeText(CameraActivity.this,
                            getString(R.string.camera_unavailable, msg),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void bind() {
        if (provider == null) return;
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                .build();

        try {
            provider.unbindAll();
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, imageCapture);
        } catch (Throwable t) {
            Toast.makeText(this,
                    getString(R.string.camera_unavailable, String.valueOf(t.getMessage())),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void toggleFlash() {
        flashOn = !flashOn;
        flash.setImageResource(flashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        if (imageCapture != null) {
            imageCapture.setFlashMode(flashOn
                    ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        }
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        shutter.setEnabled(false);

        ContentValues v = new ContentValues();
        v.put(MediaStore.Images.Media.DISPLAY_NAME, "PA_" + System.currentTimeMillis() + ".jpg");
        v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= 29) {
            v.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/拍照答题");
        }

        ImageCapture.OutputFileOptions opts = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v).build();

        imageCapture.takePicture(opts, pool, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults res) {
                Uri uri = res.getSavedUri();
                runOnUiThread(() -> {
                    shutter.setEnabled(true);
                    if (uri == null) {
                        Ui.toast(CameraActivity.this, R.string.save_failed);
                        return;
                    }
                    Intent data = new Intent();
                    data.putExtra(RESULT_URI, uri.toString());
                    setResult(Activity.RESULT_OK, data);
                    finish();
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                runOnUiThread(() -> {
                    shutter.setEnabled(true);
                    Ui.toast(CameraActivity.this,
                            getString(R.string.save_failed) + "：" + msg);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (provider != null) {
            try {
                provider.unbindAll();
            } catch (Throwable ignored) {
            }
        }
    }
}
