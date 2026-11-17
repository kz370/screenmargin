package com.local.screenmargin;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Screen Margin: adjustable black borders (left/right and top/bottom) for a
 * screen protector that covers the edges of the display. No root needed, only
 * WRITE_SECURE_SETTINGS granted once over adb. The margins stay after a reboot.
 */
public class MainActivity extends Activity {
    static final String GRANT = "adb shell pm grant com.local.screenmargin "
            + "android.permission.WRITE_SECURE_SETTINGS";
    static final int MAX = 100;                      // px per side

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private LinearLayout list;
    private TextView status;
    private int pw, ph;                              // physical size, portrait
    private final Runnable apply = new Runnable() {
        @Override
        public void run() {
            applyNow();
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("margin", MODE_PRIVATE);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        Display.Mode m = getWindowManager().getDefaultDisplay().getMode();
        pw = Math.min(m.getPhysicalWidth(), m.getPhysicalHeight());
        ph = Math.max(m.getPhysicalWidth(), m.getPhysicalHeight());

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Ui.BG);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        scroll.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                list.setPadding(dp(18), dp(20) + in.getSystemWindowInsetTop(),
                        dp(18), dp(28) + in.getSystemWindowInsetBottom());
                return in;
            }
        });
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private boolean granted() {
        return checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void build() {
        list.removeAllViews();
        final TextView title = text("Screen Margin", 32, Ui.TEXT, Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setPadding(dp(4), 0, 0, 0);
        title.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
                float w = title.getPaint().measureText(title.getText().toString());
                title.getPaint().setShader(new LinearGradient(0, 0, w, 0, Ui.CORAL, Ui.PINK, Shader.TileMode.CLAMP));
                title.invalidate();
            }
        });
        list.addView(title);
        TextView s = text("Black borders for a screen protector. The picture is shrunk and centered, "
                + "touch still lines up, and it stays after a reboot.", 14, Ui.SUB, Typeface.DEFAULT);
        s.setPadding(dp(4), dp(6), dp(8), dp(4));
        s.setLineSpacing(0, 1.15f);
        list.addView(s);

        if (!granted()) {
            section("One-time setup");
            setupBox = card();
            fillSetup();
        }

        section("Margins");
        LinearLayout c = card();
        slider(c, "Left & right", "h");
        divider(c);
        slider(c, "Top & bottom", "v");
        status = note("");
        status.setPadding(dp(18), dp(2), dp(18), dp(14));
        c.addView(status);
        showStatus(null);

        section("Check it");
        LinearLayout t = card();
        t.addView(note("Opens a full-screen pattern with a line on every edge and a ruler. Adjust "
                + "there until every edge line shows."));
        t.addView(button("Edge test", true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        }));

        View reset = button("Full screen (reset)", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putInt("h", 0).putInt("v", 0).apply();
                handler.removeCallbacks(apply);
                applyNow();
                build();
            }
        });
        list.addView(reset);

        TextView foot = text("Screen " + pw + " x " + ph + " px. Margins are real pixels per side. "
                + "Stuck? From a PC: adb shell wm size reset && adb shell wm scaling auto",
                12, Ui.SUB, Typeface.DEFAULT);
        foot.setPadding(dp(4), dp(14), dp(4), 0);
        foot.setLineSpacing(0, 1.15f);
        list.addView(foot);
    }

    /** A monospace command box with its own Copy button. */
    private Runnable marked;                          // reset action of the copied command box
    private int method;                              // setup tab: 0 PC, 1 Termux, 2 Shizuku
    private LinearLayout setupBox;

    /** The one-time setup card: three tabs, only the chosen method's steps show. */
    private void fillSetup() {
        setupBox.removeAllViews();
        setupBox.addView(note("Android needs a one-time permission before an app can change the "
                + "screen size. Pick one way. After it is done you never need it again."));
        LinearLayout tabs = new LinearLayout(this);
        String[] names = {"PC", "Termux", "Shizuku"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            TextView tab = text(names[i], 14, i == method ? 0xFF1A0E14 : Ui.SUB, Typeface.DEFAULT_BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(0, dp(10), 0, dp(10));
            tab.setBackground(i == method ? Ui.accent(dp(20)) : Ui.outline(dp(20), dp(1)));
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    method = idx;
                    fillSetup();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(i == 0 ? dp(16) : dp(4), dp(4), i == 2 ? dp(16) : dp(4), dp(4));
            tabs.addView(tab, lp);
        }
        setupBox.addView(tabs);
        LinearLayout c = setupBox;
        if (method == 0) {
            c.addView(note("Connect the phone to a PC with adb (USB or wireless debugging) and run:"));
            command(c, GRANT);
        } else if (method == 1) {
            c.addView(note("No Shizuku needed. Termux runs its own adb and connects to this phone's "
                    + "wireless debugging (Android 11+, on Wi-Fi).\n\n"
                    + "1. In Termux install adb:"));
            command(c, "pkg install android-tools");
            c.addView(note("2. Developer options > Wireless debugging on > Pair device with pairing "
                    + "code. Keep that dialog on screen (split screen or a floating Termux window): "
                    + "the code dies if you leave it. In Termux, use the pairing port shown there:"));
            command(c, "adb pair localhost:PAIRING_PORT");
            c.addView(note("3. Type the 6-digit code when it asks, then use the port shown on the "
                    + "main Wireless debugging screen (a different number):"));
            command(c, "adb connect localhost:PORT");
            c.addView(note("4. Give the permission:"));
            command(c, "adb shell pm grant com.local.screenmargin "
                    + "android.permission.WRITE_SECURE_SETTINGS");
        } else {
            c.addView(note("1. Install Shizuku and a terminal that works with it, for example "
                    + "aShell You.\n"
                    + "2. Developer options > Wireless debugging on. In Shizuku tap \"Start via "
                    + "Wireless debugging\" and follow its pairing steps. On Xiaomi/HyperOS set the "
                    + "notification style to Android (Settings > Notifications) first, or you cannot "
                    + "type the pairing code into the notification.\n"
                    + "3. In the terminal app run:"));
            command(c, "pm grant com.local.screenmargin android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private int icon(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void command(LinearLayout card, final String cmd) {
        final LinearLayout box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(Ui.outline(dp(14), 1));
        // allow a line break after each dot (the copied text stays unchanged)
        TextView t = text(cmd.replace(".", ".​"), 13, Ui.TEAL, Typeface.MONOSPACE);
        t.setPadding(dp(14), dp(12), dp(4), dp(12));
        t.setTextIsSelectable(true);
        box.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final android.widget.ImageView copy = new android.widget.ImageView(this);
        copy.setImageResource(icon("ic_copy"));
        copy.setColorFilter(Ui.CORAL);
        copy.setPadding(dp(10), dp(10), dp(10), dp(10));
        copy.setContentDescription("Copy");
        final Runnable unmark = new Runnable() {
            @Override
            public void run() {
                copy.setImageResource(icon("ic_copy"));
                copy.setColorFilter(Ui.CORAL);
                box.setBackground(Ui.outline(dp(14), 1));
            }
        };
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("command", cmd));
                if (marked != null) {                    // only one box is marked at a time
                    handler.removeCallbacks(marked);
                    marked.run();
                }
                marked = unmark;
                copy.setImageResource(icon("ic_check"));
                copy.setColorFilter(Ui.TEAL);
                box.setBackground(Ui.highlight(dp(14), dp(2)));
                Toast.makeText(MainActivity.this, "Copied: " + (cmd.length() > 44
                        ? cmd.substring(0, 44) + "..." : cmd), Toast.LENGTH_SHORT).show();
                handler.postDelayed(unmark, 2500);
            }
        });
        box.addView(copy, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(16), dp(4), dp(16), dp(12));
        card.addView(box, lp);
    }

    private void showStatus(String err) {
        int h = prefs.getInt("h", 0), v = prefs.getInt("v", 0);
        if (err != null) {
            status.setText("Could not change the screen:\n" + err);
            status.setTextColor(Ui.ERROR);
        } else if (!granted()) {
            status.setText("Do the one-time setup above first.");
            status.setTextColor(Ui.WARN);
        } else if (h == 0 && v == 0) {
            status.setText("Off  ·  full screen");
            status.setTextColor(Ui.SUB);
        } else {
            status.setText("On  ·  picture " + size(pw, h) + " x " + size(ph, v));
            status.setTextColor(Ui.TEAL);
        }
    }

    static int size(int full, int margin) {
        return (full - 2 * margin) / 2 * 2;          // even, so it stays centered
    }

    private void applyNow() {
        if (!granted()) {
            showStatus(null);
            return;
        }
        final int h = prefs.getInt("h", 0), v = prefs.getInt("v", 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String err = (h == 0 && v == 0) ? Margin.reset() : Margin.set(size(pw, h), size(ph, v));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status != null) {
                            showStatus(err);
                        }
                    }
                });
            }
        }).start();
    }

    // ------------------------------------------------------------ widgets --

    /** A 1 px step slider with - / + buttons; applies 0.6 s after the last change. */
    private void slider(LinearLayout card, String title, final String key) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(14));
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(text(title, 16, Ui.TEXT, Typeface.create("sans-serif-medium", Typeface.NORMAL)),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final TextView value = text("", 14, Ui.CORAL, Typeface.DEFAULT_BOLD);
        value.setPadding(dp(12), dp(4), dp(12), dp(4));
        value.setBackground(Ui.outline(dp(12), 0));
        head.addView(value);
        box.addView(head);

        final SeekBar bar = new SeekBar(this);
        bar.setMax(MAX);
        Ui.tint(bar);
        bar.setProgress(prefs.getInt(key, 0));
        value.setText(label(bar.getProgress()));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean user) {
                value.setText(label(p));
                prefs.edit().putInt(key, p).apply();
                handler.removeCallbacks(apply);
                handler.postDelayed(apply, 600);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        });

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(0, dp(12), 0, 0);
        controls.addView(step("−", bar, -1));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, dp(32), 1f);
        blp.setMargins(dp(6), 0, dp(6), 0);
        controls.addView(bar, blp);
        controls.addView(step("+", bar, 1));
        box.addView(controls);
        card.addView(box);
    }

    static String label(int p) {
        return p == 0 ? "Off" : p + " px";
    }

    private View step(String sign, final SeekBar bar, final int d) {
        TextView b = text(sign, 20, Ui.TEXT, Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(Ui.CHIP));
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setProgress(Math.max(0, Math.min(MAX, bar.getProgress() + d)));
            }
        });
        return b;
    }

    private View button(String label, boolean filled, View.OnClickListener l) {
        TextView b = text(label, 15, filled ? 0xFF1A0E14 : Ui.TEXT, Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        b.setBackground(filled ? Ui.accent(dp(26)) : Ui.outline(dp(26), dp(1)));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(16), dp(6), dp(16), dp(14));
        b.setLayoutParams(lp);
        return b;
    }

    private void divider(LinearLayout card) {
        View d = new View(this);
        d.setBackgroundColor(Ui.STROKE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(18), 0, dp(18), 0);
        card.addView(d, lp);
    }

    private void section(String name) {
        TextView t = text(name.toUpperCase(), 12, Ui.SUB, Typeface.DEFAULT_BOLD);
        t.setLetterSpacing(0.12f);
        t.setPadding(dp(6), dp(24), 0, dp(10));
        list.addView(t);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(Ui.card(dp(24)));
        c.setPadding(0, dp(6), 0, dp(4));
        list.addView(c, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return c;
    }

    private TextView note(String s) {
        TextView t = text(s, 13, Ui.SUB, Typeface.DEFAULT);
        t.setPadding(dp(18), dp(12), dp(18), dp(8));
        t.setLineSpacing(0, 1.15f);
        return t;
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
