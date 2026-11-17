package com.local.screenmargin;

import android.os.IBinder;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Shrinks the display and centers it, the same as "adb shell wm size WxH" plus
 * "wm scaling off". The window manager checks for WRITE_SECURE_SETTINGS (granted
 * once, see MainActivity). Its methods are hidden from apps, so the hidden-API
 * check is lifted first (org.lsposed.hiddenapibypass).
 */
final class Margin {
    private static final int DISPLAY = 0;           // built-in screen
    private static final int SCALING_OFF = 1;       // FORCE_SCALING_MODE_DISABLED
    private static final int SCALING_AUTO = 0;      // FORCE_SCALING_MODE_AUTO

    private static boolean unlocked;

    private Margin() {
    }

    /** Returns null on success, or what went wrong. */
    static synchronized String set(int w, int h) {
        return run(true, w, h);
    }

    static synchronized String reset() {
        return run(false, 0, 0);
    }

    private static String run(boolean on, int w, int h) {
        try {
            if (!unlocked) {
                HiddenApiBypass.addHiddenApiExemptions("L");   // every class
                unlocked = true;
            }
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "window");
            Object wm = Class.forName("android.view.IWindowManager$Stub")
                    .getMethod("asInterface", IBinder.class).invoke(null, b);
            Class<?> iface = Class.forName("android.view.IWindowManager");
            if (on) {
                call(wm, iface, "setForcedDisplaySize", DISPLAY, w, h);
                call(wm, iface, "setForcedDisplayScalingMode", DISPLAY, SCALING_OFF);
            } else {
                call(wm, iface, "clearForcedDisplaySize", DISPLAY);
                call(wm, iface, "setForcedDisplayScalingMode", DISPLAY, SCALING_AUTO);
            }
            return null;
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause() != null ? e.getCause() : e;
            if (c instanceof SecurityException) {
                return "Permission missing - do the one-time setup (" + c.getMessage() + ")";
            }
            return String.valueOf(c);
        } catch (Throwable t) {
            return String.valueOf(t);
        }
    }

    /** Calls the window-manager method with this name and these int arguments. */
    private static void call(Object wm, Class<?> iface, String name, int... args) throws Exception {
        StringBuilder seen = new StringBuilder();
        for (Method m : iface.getMethods()) {
            if (!m.getName().equals(name)) {
                continue;
            }
            Class<?>[] p = m.getParameterTypes();
            boolean ints = p.length == args.length;
            for (Class<?> t : p) {
                ints &= t == int.class;
            }
            if (ints) {
                Object[] a = new Object[args.length];
                for (int i = 0; i < a.length; i++) {
                    a[i] = args[i];
                }
                m.invoke(wm, a);
                return;
            }
            seen.append(' ').append(m.toGenericString());
        }
        throw new NoSuchMethodException(name + " with " + args.length + " int arguments"
                + (seen.length() > 0 ? "; found:" + seen : " (not in this Android version)"));
    }
}
