# DATE CONVERTOR

Native Windows desktop app that converts dates between English (AD / Gregorian) and Nepali (Bikram Sambat). It runs as a fast JavaFX app — no web server, no browser.

Supported range: **BS 1970–2100 (AD 1913–2044)**. Dates outside this range are rejected because authoritative BS data does not exist beyond it (Hamro Patro itself 404s past BS 2100).

## Features

- Convert **BS → AD** or **AD → BS** with one click.
- Result shows the date plus the weekday (e.g. `2001-08-08` *Wednesday*).
- **Today** button fills in the current AD date (AD mode only).
- **Ctrl+C** copies only the converted date (no weekday name).
- **Esc** clears the fields and returns focus to the Year box.
- **Backspace** deletes like a word processor, even when a button has focus.
- Today's date shown as a read-only reference (AD + BS).

## Download and run (end user)

No Java or Maven needed — everything is bundled in the installer.

1. Download **`DateConvertor-Setup.exe`** from the project root (or the release).
2. Double-click it and follow the installer.
3. Launch **Date Convertor** from the Start Menu or the desktop shortcut.

That's it. The app opens instantly — no server, no browser.

> Windows SmartScreen may show a warning for an unsigned installer. Click **More info → Run anyway**.

## Prerequisites

- **JDK 17** or later (`JAVA_HOME` set)
- **Maven 3.6+**
- Windows (for the installer; the jar itself is cross-platform)

## Run from source

```powershell
mvn package
java -jar target\dateconvertor_java-0.0.1-SNAPSHOT.jar
```

The first build downloads dependencies (JavaFX, Spring Boot, ADBS) into your local Maven repository.

## Build the installer (one step)

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File build-installer.ps1
```

This runs, in order:

1. `mvn package` — builds the fat jar
2. `jpackage` — packages `dist\DateConvertor\` (standalone app image with bundled JRE)
3. Inno Setup (`ISCC`) — produces `DateConvertor-Setup.exe`

Installers are written to `DateConvertor-Setup.exe` in the project root. To run without installing, launch `dist\DateConvertor\DateConvertor.exe`.

### Installer prerequisites

- [Inno Setup 6](https://jrsoftware.org/isinfo.php) — default per-user path is
  `C:\Users\<you>\AppData\Local\Programs\Inno Setup 6\ISCC.exe`. If it is elsewhere,
  update the path in `build-installer.ps1`.
- `jpackage` ships with the JDK (JDK 14+).

## Project layout

```
src/main/java/com/dateconvertor_java/
    DesktopApp.java          Native JavaFX UI
    DesktopMain.java         Entry point (no-arg main)
    service/DateConvertorService.java   Conversion logic (web API)
    controller/DateConvertorController.java  REST endpoints
src/main/resources/
    desktop.css              UI styling
    icon.png                 Window icon (Nepal flag)
static/index.html, css, js   Legacy web UI (not used by the desktop app)
build-installer.ps1          One-step build script
installer.iss                Inno Setup script
icon.ico                     Installer / exe icon (Nepal flag)
```

## Notes

- Date conversion is done by the **ADBS** library (`com.nepalidate:adbs-core:2.0.0`, local Maven repo). Its lookup table (BS 1970–2100) is authoritative.
- The web UI/REST API still exists and is unchanged, but the desktop app calls the ADBS library directly and does not run a server.
