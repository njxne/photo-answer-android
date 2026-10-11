package com.photoanswer;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.transition.MaterialSharedAxis;

/** 单 Activity + 四个 Tab（Fragment 用 show/hide 切换，保留各自界面状态）。 */
public class MainActivity extends AppCompatActivity {

    private static final String[] TAGS = {"solve", "essay", "history", "me"};

    private Chip gradeChip;
    private int curId;

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            (sp, key) -> {
                if (Store.K_GRADE.equals(key) || Store.K_SUBJECT.equals(key)) {
                    refreshGradeChip();
                }
            };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        gradeChip = findViewById(R.id.gradeChip);
        gradeChip.setOnClickListener(v -> Dialogs.grade(this, this::refreshGradeChip));
        refreshGradeChip();

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            select(item.getItemId());
            return true;
        });

        int initial = (b == null) ? R.id.nav_solve : nav.getSelectedItemId();
        nav.setSelectedItemId(initial == 0 ? R.id.nav_solve : initial);
        select(nav.getSelectedItemId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Store.prefs(this).registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onStop() {
        Store.prefs(this).unregisterOnSharedPreferenceChangeListener(prefListener);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGradeChip();
    }

    public void refreshGradeChip() {
        if (gradeChip != null) {
            gradeChip.setText(Store.get(this, Store.K_GRADE, "三年级"));
        }
    }

    private Fragment obtain(int id) {
        Fragment f = getSupportFragmentManager().findFragmentByTag(tagOf(id));
        if (f != null) return f;
        if (id == R.id.nav_essay) return new EssayFragment();
        if (id == R.id.nav_history) return new HistoryFragment();
        if (id == R.id.nav_me) return new MeFragment();
        return new SolveFragment();
    }

    private String tagOf(int id) {
        if (id == R.id.nav_essay) return TAGS[1];
        if (id == R.id.nav_history) return TAGS[2];
        if (id == R.id.nav_me) return TAGS[3];
        return TAGS[0];
    }

    /** 分页方向：记录/我的在右侧，进入时从右往左滑。 */
    private int slotOf(int id) {
        if (id == R.id.nav_essay) return 1;
        if (id == R.id.nav_history) return 2;
        if (id == R.id.nav_me) return 3;
        return 0;
    }

    private void select(int id) {
        if (id == curId) return;
        boolean forward = slotOf(id) >= slotOf(curId);
        curId = id;

        Fragment target = obtain(id);
        applyAxis(target, forward);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!target.isAdded()) {
            ft.add(R.id.container, target, tagOf(id));
        }
        for (String tag : TAGS) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null && f != target && !f.isHidden()) ft.hide(f);
        }
        ft.show(target);
        ft.commitAllowingStateLoss();
    }

    private void applyAxis(Fragment f, boolean forward) {
        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, forward);
        enter.setDuration(260);
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, forward);
        exit.setDuration(260);
        f.setEnterTransition(enter);
        f.setExitTransition(exit);
        f.setReenterTransition(enter);
        f.setReturnTransition(exit);
    }
}
