# Screen Margin - commands

Everything you may need to type, in one place. The app's setup screen shows the same
grant commands with Copy buttons.

Package name: `com.local.screenmargin`

## 1. One-time permission grant

Android only lets an app change the screen size after it holds
`WRITE_SECURE_SETTINGS`, and an app cannot give itself that permission. Grant it **once**
in any one of the ways below. It survives reboots and app updates, and is lost only if you
uninstall the app.

### A. From a PC (adb)

Connect the phone by USB, or by wireless debugging, and run:

```
adb shell pm grant com.local.screenmargin android.permission.WRITE_SECURE_SETTINGS
```

### B. On the phone with Termux (no PC, no Shizuku)

Needs Android 11 or newer and Wi-Fi. Termux runs its own `adb` and connects to this phone's
wireless debugging.

1. In Termux:

   ```
   pkg install android-tools
   ```

2. Settings > Developer options > turn **Wireless debugging** on > **Pair device with
   pairing code**. Keep that dialog on screen (use split screen or a floating Termux
   window): the code stops working if you leave it. In Termux, use the pairing IP and port
   shown in the dialog:

   ```
   adb pair localhost:PAIRING_PORT
   ```

   Type the 6-digit code when it asks.

3. Go back to the main Wireless debugging screen. Its port is a **different number** from the
   pairing port:

   ```
   adb connect localhost:PORT
   ```

4. Grant the permission:

   ```
   adb shell pm grant com.local.screenmargin android.permission.WRITE_SECURE_SETTINGS
   ```

### C. On the phone with Shizuku

1. Install Shizuku and a terminal that works with it (for example aShell You).
2. Settings > Developer options > Wireless debugging on. In Shizuku tap **Start via
   Wireless debugging** and follow its pairing steps. On Xiaomi / HyperOS, first set the
   notification style to **Android** (Settings > Notifications), or you cannot type the
   pairing code into the notification.
3. In the terminal app (no `adb` prefix here):

   ```
   pm grant com.local.screenmargin android.permission.WRITE_SECURE_SETTINGS
   ```

## 2. Install the app

```
adb install -r ScreenMargin-v1.0.apk
```

On Xiaomi / HyperOS, turn on **Developer options > Install via USB** first, or adb install
fails with `INSTALL_FAILED_USER_RESTRICTED`.

## 3. Check or remove the permission

Check (Windows `findstr`, Linux / Git Bash `grep`):

```
adb shell dumpsys package com.local.screenmargin | findstr WRITE_SECURE
adb shell dumpsys package com.local.screenmargin | grep WRITE_SECURE
```

`granted=true` means it is on.

Remove it (to test the setup again). Tap **Full screen (reset)** in the app first, because
the app cannot undo a margin without the permission:

```
adb shell pm revoke com.local.screenmargin android.permission.WRITE_SECURE_SETTINGS
```

If `pm revoke` errors, uninstalling always removes it:

```
adb uninstall com.local.screenmargin
```

## 4. More than one device connected

adb says `more than one device/emulator`. List them, then pick one with `-s`:

```
adb devices
adb -s 192.168.1.50:5555 shell wm size
```

## 5. The same thing without the app

The app calls the window manager the same way `wm size` does:

```
adb shell wm size              # show physical and override size
adb shell wm size 1040x2360    # shrink the picture and center it
adb shell wm scaling off       # keep it small (black borders), do not stretch it back
```

Size = physical size minus 2 x margin on each axis, rounded down to an even number.
Example for a 1080x2400 screen with a 20 px side margin and a 10 px top/bottom margin:
`1080 - 2*20 = 1040`, `2400 - 2*10 = 2380`, so `wm size 1040x2380`.

Undo:

```
adb shell wm size reset
adb shell wm scaling auto
```

The old `wm overscan` command was removed from Android; `wm size` plus `wm scaling off`
replaces it. The setting is kept by Android, so it stays after a reboot.

## 6. Stuck with a wrong size

If the screen looks wrong and the app is not reachable, run from a PC:

```
adb shell wm size reset
adb shell wm scaling auto
```

## 7. Build the app (developers)

Needs a JDK, the Android SDK (build-tools 36, platform 34), Git Bash and `cpulimit`.

```
bash build.sh
```

Output: `out/ScreenMargin.apk` and a versioned copy `release/ScreenMargin-v<version>.apk`
with its SHA-256 in `release/SHA256SUMS.txt`.
