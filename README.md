# MYST Client

Client-side Fabric mod for Minecraft Java 1.21.11.

## Features in this starter build
- Right Shift opens the MYST menu.
- Streamer Mode locally replaces player names in the TAB list.
- Add aliases and toggle Streamer Mode from the menu.
- Settings are saved to `config/mystclient-streamer.json`.

## Build
Requires Java 21 and Gradle 9.2.1: `gradle build`.
The GitHub Actions workflow builds the mod and uploads the JAR as an artifact.

Fabric Loader and Fabric API are required separately. This is a local display-only name replacement; it does not change account names or server-side names. Android/ MoJoLauncher runtime compatibility still needs device testing.
