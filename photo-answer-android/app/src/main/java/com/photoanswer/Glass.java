package com.photoanswer;

import android.view.View;

import dev.liquidglass.view.LiquidGlassProviderLayout;
import dev.liquidglass.view.LiquidGlassView;

/** 全局液态玻璃样式：统一各处玻璃的观感，避免每个页面各写一套参数。 */
public final class Glass {

    private Glass() {
    }

    /** 底部栏/浮层类玻璃：厚重一点，带交互反馈。 */
    public static void bar(LiquidGlassView v, LiquidGlassProviderLayout provider) {
        v.setProvider(provider);
        v.setCornerRadiusDp(26f);
        v.setBlurRadiusDp(20f);
        v.setRefractionHeightDp(10f);
        v.setRefractionAmountDp(14f);
        v.setSaturation(1.4f);
        v.setChromaticAberration(0.35f);
        v.setHighlightWidthDp(1.5f);
        v.setHighlightAlpha(0.45f);
        v.setGlassInteractive(true);
    }

    /** 顶栏类玻璃：更轻薄。 */
    public static void topBar(LiquidGlassView v, LiquidGlassProviderLayout provider) {
        v.setProvider(provider);
        v.setCornerRadiusDp(24f);
        v.setBlurRadiusDp(18f);
        v.setRefractionHeightDp(8f);
        v.setRefractionAmountDp(10f);
        v.setSaturation(1.3f);
        v.setHighlightAlpha(0.35f);
    }

    /** 卡片类玻璃：圆角大、模糊适中。 */
    public static void card(LiquidGlassView v, LiquidGlassProviderLayout provider) {
        v.setProvider(provider);
        v.setCornerRadiusDp(18f);
        v.setBlurRadiusDp(12f);
        v.setRefractionHeightDp(6f);
        v.setRefractionAmountDp(8f);
        v.setSaturation(1.2f);
        v.setHighlightWidthDp(1f);
        v.setHighlightAlpha(0.3f);
    }

    /** 小控件（Chip / 按钮）类玻璃。 */
    public static void chip(LiquidGlassView v, LiquidGlassProviderLayout provider) {
        v.setProvider(provider);
        v.setCornerRadiusDp(14f);
        v.setBlurRadiusDp(8f);
        v.setRefractionHeightDp(4f);
        v.setRefractionAmountDp(6f);
        v.setSaturation(1.2f);
        v.setHighlightAlpha(0.28f);
    }

    /**
     * 给 Fragment 里的玻璃卡片绑定 Activity 的 provider。
     * Fragment 布局里的玻璃节点需要这个才能采样到背后的内容。
     */
    public static void bindCard(LiquidGlassView v, View fragmentRoot) {
        if (v == null || fragmentRoot == null) return;
        LiquidGlassProviderLayout provider =
                fragmentRoot.getRootView().findViewById(R.id.glassProvider);
        if (provider == null) {
            v.setVisibility(View.GONE);
            return;
        }
        card(v, provider);
    }

    /** 找不到 provider 时隐藏玻璃，避免出现没背景的空白层。 */
    public static void hideIfNoProvider(LiquidGlassView v) {
        if (v == null) return;
        if (v.getProvider() == null) v.setVisibility(View.GONE);
    }
}
