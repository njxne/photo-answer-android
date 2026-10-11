package com.photoanswer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 记录页：本地历史列表。 */
public class HistoryFragment extends Fragment {

    private final List<Map<String, Object>> items = new ArrayList<>();
    private HistoryAdapter adapter;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        empty = v.findViewById(R.id.historyEmpty);

        RecyclerView list = v.findViewById(R.id.historyList);
        adapter = new HistoryAdapter(items, new HistoryAdapter.Listener() {
            @Override
            public void onClick(Map<String, Object> item) {
                showDetail(item);
            }

            @Override
            public void onLongClick(Map<String, Object> item) {
                confirmDelete(item);
            }
        });
        list.setAdapter(adapter);

        v.findViewById(R.id.btnClearHistory).setOnClickListener(x ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("清空历史记录？")
                        .setPositiveButton(R.string.history_clear, (d, w) -> {
                            Store.clearHistory(requireContext());
                            reload();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        if (adapter == null) return;
        items.clear();
        for (Object o : Store.history(requireContext())) {
            Map<String, Object> m = Json.asObj(o);
            if (m != null) items.add(m);
        }
        adapter.notifyDataSetChanged();
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDetail(Map<String, Object> item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Ui.modeLabel(Json.str(item.get("mode")))
                        + " · " + Json.str(item.get("grade")))
                .setMessage(Json.str(item.get("a")))
                .setPositiveButton(R.string.copy, (d, w) ->
                        Ui.copy(requireContext(), Json.str(item.get("a"))))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void confirmDelete(Map<String, Object> item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除这条记录？")
                .setPositiveButton(R.string.delete, (d, w) -> {
                    Store.removeHistory(requireContext(), item);
                    reload();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
