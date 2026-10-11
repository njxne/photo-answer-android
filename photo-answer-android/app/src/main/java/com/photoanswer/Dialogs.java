package com.photoanswer;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** 年级 / 学科 选择弹窗。 */
public final class Dialogs {

    private Dialogs() {
    }

    public static void grade(Context c, Runnable after) {
        int cur = Prompt.gradeIndex(Store.get(c, Store.K_GRADE, "三年级"));
        new MaterialAlertDialogBuilder(c)
                .setTitle(R.string.choose_grade)
                .setSingleChoiceItems(Prompt.GRADES, cur, (d, which) -> {
                    Store.set(c, Store.K_GRADE, Prompt.GRADES[which]);
                    d.dismiss();
                    if (after != null) after.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void subject(Context c, Runnable after) {
        String cur = Store.get(c, Store.K_SUBJECT, "自动识别");
        int idx = 0;
        for (int i = 0; i < Prompt.SUBJECTS.length; i++) {
            if (Prompt.SUBJECTS[i].equals(cur)) idx = i;
        }
        new MaterialAlertDialogBuilder(c)
                .setTitle(R.string.choose_subject)
                .setSingleChoiceItems(Prompt.SUBJECTS, idx, (d, which) -> {
                    Store.set(c, Store.K_SUBJECT, Prompt.SUBJECTS[which]);
                    d.dismiss();
                    if (after != null) after.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
