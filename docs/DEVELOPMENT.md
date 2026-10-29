# Development Guide

## Toolchain

- Android Studio stable release.
- Android Gradle Plugin configured in `gradle/libs.versions.toml`.
- Gradle Wrapper included in the repository.
- JDK 11 or the bundled Android Studio JDK 21.
- Android SDK 36 and an API 26+ device/emulator.

## Local Setup

1. Clone the repository.
2. Open the root directory in Android Studio.
3. Let Gradle download dependencies.
4. Let Android Studio create `local.properties`; this file is ignored by Git.
5. Run the `app` module.

## Build Commands

```bash
# Windows
gradlew.bat assembleDebug
gradlew.bat test

# macOS/Linux
./gradlew assembleDebug
./gradlew test
```

There is no separate formatter or linter task in the repository. Use Android
Studio inspections and keep changes focused on the relevant module.

## Debugging Checklist

### Blank map

Check network access, WMS URL, layer name, coordinate reference system, and
cleartext/HTTPS policy. The client currently uses a course-era service URL that
must be replaced for a public deployment.

### Route cannot be found

Check that the WFS endpoint is reachable, the layer name matches, `viewparams`
names are accepted, and both points fall within the routable network.

### Route is shifted

Confirm that WMS, WFS, GeoJSON, and the client agree on EPSG:3857/Web Mercator.
Check that the server is not returning longitude/latitude pairs while the client
expects projected meters.

### GPS is unavailable

Check runtime permission, device location settings, outdoor signal quality, and
the configured update interval in the Settings screen.

## Code Areas

Keep projection logic in `WebMecator`, network code in `WhuRouteTask`, and
GeoJSON conversion in `WhuRoute`. Avoid blocking the Android main thread.

