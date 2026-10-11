package com.photoanswer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.transition.MaterialFadeThrough;

import java.util.ArrayList;
import java.util.List;

/** 拍题页：选图 → 一次性出全部答案 → 用上方圆形题号在本地切换。 */
public class SolveFragment extends Fragment {

    private final ImagePicker picker = new ImagePicker(this);

    private View answerCard;
    private View questionStrip;
    private LinearLayout questionBar;
    private final List<Chip> chips = new ArrayList<>();
    private TextView status;
    private TextView answer;

    private final List<Answers.Part> parts = new ArrayList<>();
    private String rawAnswer = "";
    private int curIndex = 0;

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        MaterialFadeThrough t = new MaterialFadeThrough();
        t.setDuration(220);
        setEnterTransition(t);
        setExitTransition(t);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_solve, container, false);
        picker.bind(v);

        answerCard = v.findViewById(R.id.answerCard);
        questionStrip = v.findViewById(R.id.questionStrip);
        questionBar = v.findViewById(R.id.questionBar);
        status = v.findViewById(R.id.txtStatus);
        answer = v.findViewById(R.id.txtAnswer);

        v.findViewById(R.id.btnCopyAll).setOnClickListener(x -> Ui.copy(requireContext(), rawAnswer));
        v.findViewById(R.id.btnCopy).setOnClickListener(x ->
                Ui.copy(requireContext(), answer.getText().toString()));

        v.findViewById(R.id.btnSolve).setOnClickListener(x -> {
            if (picker.uris().isEmpty()) {
                Ui.toast(requireContext(), R.string.need_image);
                return;
            }
            parts.clear();
            rawAnswer = "";
            curIndex = 0;
            questionBar.removeAllViews();
            chips.clear();
            questionStrip.setVisibility(View.GONE);
            answerCard.setVisibility(View.GONE);

            Ai.run(requireActivity(), "solve", new ArrayList<>(picker.uris()), null,
                    new Ai.Cb() {
                        @Override
                        public void onStart(String s) {
                            status.setText(s);
                            answer.setText("");
                        }

                        @Override
                        public void onDone(String a) {
                            rawAnswer = a;
                            parts.clear();
                            parts.addAll(Answers.split(a));
                            buildChips();
                            answerCard.setVisibility(View.VISIBLE);
                            showPart(0);
                        }

                        @Override
                        public void onFail(String m) {
                            answerCard.setVisibility(View.VISIBLE);
                            questionStrip.setVisibility(View.GONE);
                            answer.setText("");
                            status.setText("失败：" + m);
                        }
                    });
        });
        return v;
    }

    private void buildChips() {
        questionBar.removeAllViews();
        chips.clear();
        if (parts.size() <= 1) {
            questionStrip.setVisibility(View.GONE);
            return;
        }
        questionStrip.setVisibility(View.VISIBLE);
        float density = getResources().getDisplayMetrics().density;
        int gap = (int) (10 * density + 0.5f);
        for (int i = 0; i < parts.size(); i++) {
            final int idx = i;
            Chip chip = new Chip(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.leftMargin = gap;
            chip.setLayoutParams(lp);
            chip.setText(String.valueOf(parts.get(i).num));
            chip.setOnClickListener(x -> showPart(idx));
            applyChipState(chip, idx == curIndex);
            questionBar.addView(chip);
            chips.add(chip);
        }
    }

    /** 纯本地切换，不触发任何网络请求。 */
    private void showPart(int idx) {
        if (parts.isEmpty()) return;
        if (idx < 0 || idx >= parts.size()) idx = 0;
        curIndex = idx;
        answer.setText(parts.get(idx).body);
        if (parts.size() > 1) {
            status.setText(getString(R.string.question_of, idx + 1, parts.size()));
        } else {
            status.setText("完成 · " + Store.get(requireContext(), Store.K_GRADE, "")
                    + " · " + Ui.time(System.currentTimeMillis()));
        }
        for (int i = 0; i < chips.size(); i++) {
            applyChipState(chips.get(i), i == curIndex);
        }
    }

    /** 选中态用 Material 官方的 ColorStateList 表达，不自己画控件。 */
    private void applyChipState(Chip chip, boolean selected) {
        chip.setChecked(selected);
        chip.setChipBackgroundColorResource(selected ? R.color.chip_selected : R.color.chip_normal);
        chip.setTextColor(getResources().getColor(
                selected ? R.color.chip_selected_text : R.color.chip_normal_text, null));
    }
}
