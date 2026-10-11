package com.photoanswer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 我的页：年级 / 学科、AI 接口配置、模型自动拉取。 */
public class MeFragment extends Fragment {

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private MaterialButton btnGrade;
    private MaterialButton btnSubject;
    private MaterialButton btnFetchModels;
    private TextInputEditText inBase;
    private TextInputEditText inKey;
    private TextInputEditText inModel;

    private boolean fetching;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_me, container, false);

        btnGrade = v.findViewById(R.id.btnGrade);
        btnSubject = v.findViewById(R.id.btnSubject);
        btnFetchModels = v.findViewById(R.id.btnFetchModels);
        inBase = v.findViewById(R.id.inBase);
        inKey = v.findViewById(R.id.inKey);
        inModel = v.findViewById(R.id.inModel);

        btnGrade.setOnClickListener(x ->
                Dialogs.grade(requireContext(), this::refreshChoices));
        btnSubject.setOnClickListener(x ->
                Dialogs.subject(requireContext(), this::refreshChoices));
        btnFetchModels.setOnClickListener(x -> fetchModels());
        v.findViewById(R.id.btnSave).setOnClickListener(x -> save());

        fillFields();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshChoices();
    }

    private void fillFields() {
        refreshChoices();
        inBase.setText(Store.get(requireContext(), Store.K_BASE, Store.DEF_BASE));
        inKey.setText(Store.get(requireContext(), Store.K_KEY, ""));
        inModel.setText(Store.get(requireContext(), Store.K_MODEL, Store.DEF_MODEL));
    }

    private void refreshChoices() {
        btnGrade.setText(Store.get(requireContext(), Store.K_GRADE, "三年级"));
        btnSubject.setText(Store.get(requireContext(), Store.K_SUBJECT, "自动识别"));
    }

    private void save() {
        Store.set(requireContext(), Store.K_BASE, text(inBase));
        Store.set(requireContext(), Store.K_KEY, text(inKey));
        Store.set(requireContext(), Store.K_MODEL, text(inModel));
        Ui.toast(requireContext(), R.string.saved);
    }

    private void fetchModels() {
        if (fetching) return;
        final String base = text(inBase);
        final String key = text(inKey);
        if (key.isEmpty()) {
            Ui.toast(requireContext(), R.string.need_key);
            return;
        }
        // 先把地址与 Key 存起来，拉取用的就是当前填的值
        Store.set(requireContext(), Store.K_BASE, base);
        Store.set(requireContext(), Store.K_KEY, key);

        fetching = true;
        btnFetchModels.setEnabled(false);
        btnFetchModels.setText(R.string.fetching);

        pool.execute(() -> {
            try {
                final List<String> models = AiClient.listModels(base, key);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    fetching = false;
                    btnFetchModels.setEnabled(true);
                    btnFetchModels.setText(R.string.me_fetch_models);
                    showModelDialog(models);
                });
            } catch (final Throwable t) {
                final String msg = t.getMessage() == null
                        ? t.getClass().getSimpleName() : t.getMessage();
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    fetching = false;
                    btnFetchModels.setEnabled(true);
                    btnFetchModels.setText(R.string.me_fetch_models);
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("拉取失败")
                            .setMessage(msg)
                            .setPositiveButton(R.string.close, null)
                            .show();
                });
            }
        });
    }

    private void showModelDialog(List<String> models) {
        final String[] arr = models.toArray(new String[0]);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_model)
                .setItems(arr, (d, which) -> {
                    inModel.setText(arr[which]);
                    Store.set(requireContext(), Store.K_MODEL, arr[which]);
                    Ui.toast(requireContext(), "已选择 " + arr[which]);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
