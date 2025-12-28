# `KodiRemote`

Wear OS app for controlling `Kodi`.

## Build

- Debug APK: `./gradlew assembleDebug`
- Release APK: `./gradlew assembleRelease`

## VS Code tasks / debugging

This repo includes VS Code tasks and launch configs under [VS Code config](.vscode) to build, install, and run the app on:

- a Wear OS emulator (`emulator-5554`)
- a physical watch connected via ADB over TCP (`ip:port`)

## Wear OS “main screen” surfaces

This app exposes two Wear OS surfaces you can add from the watch UI:

- **Tile**: quick controls (Play/Pause, Next, Stop)
- **Complication**: a watch face complication provider

### Add the tile

1. Install the app (debug or release) using the tasks above.
2. On the watch, open the **Tiles** carousel (how you get there varies by device/OS).
3. Scroll to the end and tap **+** / **Add tiles**.
4. Look for `Kodi Remote` and add it.

If you can’t find `Kodi Remote` in the **+** list, it may already be active in the carousel (active tiles typically don’t appear in the add list). In that case, remove it from the carousel first (Edit tiles → remove), then add it again.

### Add the complication

1. Long-press the watch face → **Customize** (or similar).
2. Go to **Complications**.
3. Pick a slot and choose `Kodi Remote` as the data source.

## Local (not committed) config files

Some files are intentionally not checked into git because they contain machine-specific settings or credentials.

### 1) Watch ADB “serial” (IP:port)

Tasks/debug configs reference this VS Code setting:

- `kodiremote.watchSerial` (example: `192.168.86.226:41913`)

Why “serial”? In ADB terminology, the string you pass to `adb -s <...>` is the device identifier and is called the device *serial*. For TCP-connected devices, that serial is formatted as `ip:port`, so the setting uses the same wording.

Create a local workspace settings file (git-ignored):

1. Copy [VS Code settings example](.vscode/settings.example.json) to `.vscode/settings.json`
2. Update `kodiremote.watchSerial`

Alternative: set the same key in VS Code **User Settings (JSON)** (applies globally).

## Enable debugging on a physical watch

These steps enable ADB debugging on a Wear OS watch so you can install/run/debug from your dev machine.

1. Enable Developer options on the watch
   - Settings → About → Versions (or “Software info”)
   - Tap **Build number** 7 times
2. Turn on ADB debugging
   - Settings → Developer options
   - Enable **ADB debugging**
3. Connect over Wi‑Fi (recommended)
   - In Developer options, enable **Wireless debugging** / **Debug over Wi‑Fi**
   - Open Wireless debugging and note the shown address/ports
     - Newer Android/Wear OS: use pairing + connect
        - On the watch, tap **Pair new device** to display the pairing code and pairing port
        - Run `adb pair <ip>:<pairingPort>` and enter the pairing code
        - Run `adb connect <ip>:<adbPort>`
     - Older flows: it may show a single `<ip>:<port>`; run `adb connect <ip>:<port>`
4. Verify it’s connected
   - Run `adb devices` and confirm the watch shows as `device`
5. Set VS Code watch target
   - Put the connected device identifier into `kodiremote.watchSerial` (it will be the same `ip:port` you connected to)

### 2) `Kodi` connection settings

The app reads these from `local.properties` (also git-ignored):

- `kodi.host`
- `kodi.username`
- `kodi.password`

Create `local.properties` in the repo root with values appropriate for your setup.

### 3) Release signing (for installing a release APK)

Android requires APKs to be signed. This repo is configured so that:

- if you set `signing.*` values in `local.properties`, the `release` build will be signed with your `keystore`
- otherwise, `release` will be signed with the default debug `keystore` (fine for local installs; not for Play Store)

Add these optional keys to `local.properties`:

- `signing.storeFile` (path to your `.jks` / `.keystore`)
- `signing.storePassword`
- `signing.keyAlias`
- `signing.keyPassword`

Then you can install/run the release build via VS Code tasks:

- `buildRelease`
- `installWatchRelease`
- `runWatchRelease`
