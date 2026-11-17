# Screen Margin

Adjustable black borders for a screen protector that covers the edges of the display. The
whole picture (status bar, apps, navigation bar) is shrunk and centered, and touch still lines
up. No root needed. The margins stay after a reboot.

It replaces the old `wm overscan` command, which Android removed. It uses `wm size` plus
`wm scaling off` through the window manager.

## Features
- Separate **left & right** and **top & bottom** margins, 0-100 px per side, 1 px steps with
  − / + buttons.
- **Edge test**: a full-screen pattern with a line on every edge, rulers and a grid, with the
  controls floating on top, so you can watch the edge lines come out from under the protector.
- **Full screen (reset)** button.
- One-time setup screen with tabs (PC / Termux / Shizuku) and copy buttons for each command.

## Install
1. Download `ScreenMargin-v1.0.apk` from the Releases page and install it
   (`adb install -r ScreenMargin-v1.0.apk`, or open the file on the phone).
2. Give it the one-time permission. See [COMMANDS.md](COMMANDS.md): from a PC, or on the phone
   with Termux or Shizuku. After that, no PC or helper app is needed again.
3. Open the app, move the sliders, and use **Edge test** to check every edge line is visible.

The permission is `android.permission.WRITE_SECURE_SETTINGS`. Android does not let an app grant
it to itself, which is why this one step needs adb.

## Tested on
- Redmi Note 13 4G (HyperOS 2.0, Android 15): margin applied, edge test, reset and the setup
  screen checked on the phone.
- The Termux route is written from the standard adb steps and has not been run on that phone.

Android 15 hides the window-manager methods from normal apps, so the app lifts that
restriction first with
[AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass).

## Build
```
bash build.sh
```
Needs a JDK, the Android SDK (build-tools 36, platform 34), Git Bash and `cpulimit`. It uses
plain SDK tools (aapt2, javac, d8, zipalign, apksigner), not Gradle, and runs every step under
`cpulimit 60`. Output: `out/ScreenMargin.apk` and `release/ScreenMargin-v<version>.apk` with
`release/SHA256SUMS.txt`.

The APK is signed with `local.keystore`, created on the first build and kept out of git.
**Back that file up**: an update signed with a different key cannot be installed over the old
app, so users would have to uninstall first (and grant the permission again).

Bump `android:versionCode` and `versionName` in `AndroidManifest.xml` for each release.

## Layout
- `src/` - the app (`MainActivity` settings screen, `TestActivity` + `EdgeView` edge test,
  `Margin` window-manager calls, `Ui` colors).
- `res/` - adaptive icon and copy / check icons.
- `libs/` - `hiddenapibypass` (Apache-2.0), see `libs/README.txt`.
- `build.sh` - the build.
- `COMMANDS.md` - every command, in one place.
