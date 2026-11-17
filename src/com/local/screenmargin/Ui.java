package com.local.screenmargin;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.widget.SeekBar;

/** Colors and shared drawables: ink-navy surfaces, a coral-to-pink accent, teal highlights. */
final class Ui {
    static final int BG = 0xFF0B0D17;
    static final int SURFACE = 0xFF151827;
    static final int STROKE = 0xFF252A42;
    static final int CHIP = 0xFF1F2336;
    static final int TEXT = 0xFFF3F4FB;
    static final int SUB = 0xFF8A90B0;
    static final int CORAL = 0xFFFF8A5B;
    static final int PINK = 0xFFFF5E8A;
    static final int TEAL = 0xFF5EE6CF;
    static final int ERROR = 0xFFFF7A85;
    static final int WARN = 0xFFFFC66B;

    private Ui() {
    }

    static GradientDrawable card(float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(radius);
        d.setColor(SURFACE);
        d.setStroke(1, STROKE);
        return d;
    }

    static GradientDrawable accent(float radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{CORAL, PINK});
        d.setCornerRadius(radius);
        return d;
    }

    static GradientDrawable outline(float radius, int strokePx) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(radius);
        d.setColor(CHIP);
        d.setStroke(strokePx, STROKE);
        return d;
    }

    static GradientDrawable highlight(float radius, int strokePx) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(radius);
        d.setColor(0xFF132B2E);
        d.setStroke(strokePx, TEAL);
        return d;
    }

    static GradientDrawable round(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    static void tint(SeekBar bar) {
        bar.setProgressTintList(ColorStateList.valueOf(CORAL));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(STROKE));
        bar.setThumbTintList(ColorStateList.valueOf(PINK));
    }
}
