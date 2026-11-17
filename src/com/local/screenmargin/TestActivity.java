package com.local.screenmargin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Edge test: the test pattern fills the whole picture (status and navigation
 * bars hidden), with - / + controls in the middle. Each change is applied live,
 * so you can watch the edge lines come out from under the screen protector.
 */
public class TestActivity extends Activity {
    private static final int MAX = 100;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private int pw, ph;
    private TextView hValue, vValue, status;
    private final Runnable apply = new Runnable() {
        @Override
        public void run() {
            final int h = prefs.getInt("h", 0), v = prefs.getInt("v", 0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String err = (h == 0 && v == 0) ? Margin.reset()
                            : Margin.set((pw - 2 * h) / 2 * 2, (ph - 2 * v) / 2 * 2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText(err == null ? "Every coral edge line should be fully visible."
                                    : "Could not change the screen - do the one-time setup first.");
                        }
                    });
                }
            }).start();
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("margin", MODE_PRIVATE);
        Display.Mode m = getWindowManager().getDefaultDisplay().getMode();
        pw = Math.min(m.getPhysicalWidth(), m.getPhysicalHeight());
        ph = Math.max(m.getPhysicalWidth(), m.getPhysicalHeight());

        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= 30) {
            wlp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= 28) {
            wlp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(wlp);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);     // draw under the cutout / bars
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        FrameLayout root = new FrameLayout(this);
        root.addView(new EdgeView(this));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(14), dp(18), dp(14));
        GradientDrawable bg = Ui.card(dp(24));
        bg.setColor(0xEB151827);
        panel.setBackground(bg);
        TextView title = text("Edge test", 18, Ui.TEXT, Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);
        hValue = text("", 15, Ui.CORAL, Typeface.DEFAULT_BOLD);
        vValue = text("", 15, Ui.CORAL, Typeface.DEFAULT_BOLD);
        panel.addView(row("Left & right", "h", hValue));
        panel.addView(row("Top & bottom", "v", vValue));
        status = text("Tap - / + until every coral edge line is fully visible.", 13, Ui.SUB,
                Typeface.DEFAULT);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(8), 0, dp(10));
        panel.addView(status);
        TextView done = text("Done", 15, 0xFF1A0E14, Typeface.DEFAULT_BOLD);
        done.setGravity(Gravity.CENTER);
        done.setPadding(0, dp(10), 0, dp(10));
        done.setBackground(Ui.accent(dp(22)));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        panel.addView(done);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(300),
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(panel, plp);
        setContentView(root);
        refresh();
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);
        if (focus) {
            hideBars();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void refresh() {
        hValue.setText(label(prefs.getInt("h", 0)));
        vValue.setText(label(prefs.getInt("v", 0)));
    }

    private static String label(int p) {
        return p == 0 ? "Off" : p + " px";
    }

    private LinearLayout row(String name, final String key, TextView value) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(10), 0, 0);
        r.addView(text(name, 15, Ui.TEXT, Typeface.DEFAULT),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(step("−", key, -1));
        value.setGravity(Gravity.CENTER);
        r.addView(value, new LinearLayout.LayoutParams(dp(64), ViewGroup.LayoutParams.WRAP_CONTENT));
        r.addView(step("+", key, 1));
        return r;
    }

    private View step(String sign, final String key, final int d) {
        TextView b = text(sign, 20, Ui.TEXT, Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(Ui.CHIP));
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int p = Math.max(0, Math.min(MAX, prefs.getInt(key, 0) + d));
                prefs.edit().putInt(key, p).apply();
                refresh();
                handler.removeCallbacks(apply);
                handler.postDelayed(apply, 400);
            }
        });
        return b;
    }

    private TextView text(CharSequence s, int sp, int color, Typeface tf) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setTypeface(tf);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
