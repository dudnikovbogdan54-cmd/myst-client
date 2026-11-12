# MYST Client

A custom client-side Fabric mod for Minecraft Java 1.21.11, created by **Owner Aloxmyst_YT**.

## Features
- Right Shift opens the MYST Client menu.
- Streamer Mode locally replaces player names in the TAB list.
- Add or update local nickname aliases from the menu.
- Toggle Streamer Mode on or off.
- Settings are saved to `config/mystclient-streamer.json`.
- Mod Menu listing includes a custom icon, owner credit, and localized description.

## Compatibility
- Minecraft Java 1.21.11
- Fabric Loader 0.19.5 or newer
- Fabric API for 1.21.11
- Java 21 or newer at runtime

Streamer aliases are client-side display changes only. They do not change account names or server-side names. Android and MojoLauncher runtime compatibility must still be confirmed by testing on the target device.

## Build
This project uses Fabric Loom 1.18.2, which requires JDK 25 and Gradle 9.7.1 for building. The resulting mod is compiled for Java 21 and is intended to run on Minecraft's Java 21 runtime.

```sh
gradle build
```

The GitHub Actions workflow builds the mod and uploads the JAR as the `MYST-Client-1.21.11` artifact.
