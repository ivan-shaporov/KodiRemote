# KodiRemote

Wear OS app for controlling Kodi.

## Build

- Debug APK: `./gradlew assembleDebug`

## VS Code tasks / debugging

This repo includes VS Code tasks and launch configs under [.vscode](.vscode) to build, install, and run the app on:

- a Wear OS emulator (`emulator-5554`)
- a physical watch connected via ADB over TCP (`ip:port`)

## Local (not committed) config files

Some files are intentionally not checked into git because they contain machine-specific settings or credentials.

### 1) Watch ADB “serial” (ip:port)

Tasks/debug configs reference this VS Code setting:

- `kodiremote.watchSerial` (example: `192.168.86.226:41913`)

Why “serial”? In ADB terminology, the string you pass to `adb -s <...>` is the device identifier and is called the device *serial*. For TCP-connected devices, that serial is formatted as `ip:port`, so the setting uses the same wording.

Create a local workspace settings file (gitignored):

1. Copy [.vscode/settings.example.json](.vscode/settings.example.json) to `.vscode/settings.json`
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

### 2) Kodi connection settings

The app reads these from `local.properties` (also gitignored):

- `kodi.host`
- `kodi.username`
- `kodi.password`

Create `local.properties` in the repo root with values appropriate for your setup.
