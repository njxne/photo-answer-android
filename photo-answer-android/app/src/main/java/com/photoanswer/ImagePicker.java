package com.photoanswer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择：拍照走自建的 CameraActivity（不调用系统相机），相册走 GetMultipleContents 支持多选。
 * 必须在 Fragment 构造期创建（registerForActivityResult 的时机要求）。
 */
public class ImagePicker {

    public static final int MAX = 9;

    private final Fragment host;
    private final List<Uri> uris = new ArrayList<>();
    private ImageAdapter adapter;
    private TextView empty;

    private final ActivityResultLauncher<Intent> camLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<String[]> permLauncher;

    public ImagePicker(Fragment host) {
        this.host = host;
        camLauncher = host.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), r -> {
                    if (r.getResultCode() == android.app.Activity.RESULT_OK
                            && r.getData() != null) {
                        String s = r.getData().getStringExtra(CameraActivity.RESULT_URI);
                        if (s != null) add(Uri.parse(s));
                    }
                });
        galleryLauncher = host.registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), list -> {
                    if (list != null) {
                        for (Uri u : list) add(u);
                    }
                });
        permLauncher = host.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean cam = result.get(Manifest.permission.CAMERA);
                    if (cam != null && cam) {
                        launchCamera();
                    } else {
                        Ui.toast(host.requireContext(), R.string.no_camera_permission);
                    }
                });
    }

    public void bind(View root) {
        RecyclerView rv = root.findViewById(R.id.pickerList);
        empty = root.findViewById(R.id.pickerEmpty);
        adapter = new ImageAdapter(uris, pos -> {
            if (pos >= 0 && pos < uris.size()) {
                uris.remove(pos);
                sync();
            }
        });
        rv.setAdapter(adapter);
        root.findViewById(R.id.btnCamera).setOnClickListener(v -> takePhoto());
        root.findViewById(R.id.btnGallery).setOnClickListener(v -> gallery());
        sync();
    }

    public List<Uri> uris() {
        return uris;
    }

    public void clear() {
        uris.clear();
        sync();
    }

    private void add(Uri u) {
        if (u == null) return;
        if (uris.size() >= MAX) {
            Ui.toast(host.requireContext(), R.string.max_images);
            return;
        }
        uris.add(u);
        sync();
    }

    private void sync() {
        if (adapter != null) adapter.notifyDataSetChanged();
        if (empty != null) empty.setVisibility(uris.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void takePhoto() {
        List<String> need = new ArrayList<>();
        Context ctx = host.requireContext();
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.CAMERA);
        }
        // Android 9 及以下往 MediaStore 插图片需要写权限
        if (Build.VERSION.SDK_INT < 29
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!need.isEmpty()) {
            permLauncher.launch(need.toArray(new String[0]));
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        try {
            camLauncher.launch(new android.content.Intent(
                    host.requireContext(), CameraActivity.class));
        } catch (Throwable t) {
            Ui.toast(host.requireContext(), "打不开相机：" + t.getMessage());
        }
    }

    private void gallery() {
        galleryLauncher.launch("image/*");
    }
}
