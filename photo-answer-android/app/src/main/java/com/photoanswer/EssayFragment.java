package com.photoanswer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/** 作文页：批改作文 / AI 生作文。 */
public class EssayFragment extends Fragment {

    private final ImagePicker picker = new ImagePicker(this);

    private View panelGrade;
    private View panelWrite;
    private TextInputEditText essayInput;
    private TextInputEditText writeTopic;
    private TextView gradeStatus;
    private TextView gradeAnswer;
    private TextView writeStatus;
    private TextView writeAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_essay, container, false);
        picker.bind(v);

        panelGrade = v.findViewById(R.id.panelGrade);
        panelWrite = v.findViewById(R.id.panelWrite);
        essayInput = v.findViewById(R.id.essayInput);
        writeTopic = v.findViewById(R.id.writeTopic);
        gradeStatus = v.findViewById(R.id.gradeStatus);
        gradeAnswer = v.findViewById(R.id.gradeAnswer);
        writeStatus = v.findViewById(R.id.writeStatus);
        writeAnswer = v.findViewById(R.id.writeAnswer);

        v.findViewById(R.id.btnCopyGrade).setOnClickListener(x ->
                Ui.copy(requireContext(), gradeAnswer.getText().toString()));
        v.findViewById(R.id.btnCopyWrite).setOnClickListener(x ->
                Ui.copy(requireContext(), writeAnswer.getText().toString()));

        v.findViewById(R.id.btnGrade).setOnClickListener(x -> {
            String text = essayInput.getText() == null ? "" : essayInput.getText().toString();
            if (picker.uris().isEmpty() && text.trim().isEmpty()) {
                Ui.toast(requireContext(), R.string.need_essay);
                return;
            }
            Ai.run(requireActivity(), "grade", new ArrayList<>(picker.uris()), text,
                    new Ai.Cb() {
                        @Override
                        public void onStart(String s) {
                            gradeStatus.setText(s);
                            gradeAnswer.setText("");
                        }

                        @Override
                        public void onDone(String a) {
                            gradeAnswer.setText(a);
                            gradeStatus.setText("完成 · " + Store.get(requireContext(), Store.K_GRADE, ""));
                        }

                        @Override
                        public void onFail(String m) {
                            gradeStatus.setText("失败：" + m);
                        }
                    });
        });

        v.findViewById(R.id.btnWrite).setOnClickListener(x -> {
            String topic = writeTopic.getText() == null ? "" : writeTopic.getText().toString().trim();
            if (topic.isEmpty()) {
                Ui.toast(requireContext(), R.string.need_topic);
                return;
            }
            Ai.run(requireActivity(), "write", null, topic, new Ai.Cb() {
                @Override
                public void onStart(String s) {
                    writeStatus.setText(s);
                    writeAnswer.setText("");
                }

                @Override
                public void onDone(String a) {
                    writeAnswer.setText(a);
                    writeStatus.setText("完成 · " + Store.get(requireContext(), Store.K_GRADE, ""));
                }

                @Override
                public void onFail(String m) {
                    writeStatus.setText("失败：" + m);
                }
            });
        });

        TabLayout tabs = v.findViewById(R.id.essayTabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showPanel(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        showPanel(true);
        return v;
    }

    private void showPanel(boolean gradeMode) {
        panelGrade.setVisibility(gradeMode ? View.VISIBLE : View.GONE);
        panelWrite.setVisibility(gradeMode ? View.GONE : View.VISIBLE);
    }
}
