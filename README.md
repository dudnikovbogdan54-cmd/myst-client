# Mist Visuals

An original client-side Fabric mod by **Aloxmyst_YT** for Minecraft Java **1.21.11**.

## Current modules

- Right Shift opens the purple Mist Visuals menu.
- HUD: watermark, FPS, coordinates, ping, Target HUD and block information.
- Visuals: custom crosshair.
- Utilities: keystrokes display (W/A/S/D and mouse buttons).
- Effects: a short decorative purple frame effect when the currently targeted living entity is defeated.
- Streamer Mode: local TAB-list nickname aliases; changes are visible only on this client.
- Settings are saved locally in the Minecraft `config` directory.
- The menu uses an original high-resolution generated purple background texture.

## Controls

- **Right Shift** — open or close the Mist Visuals menu.
- In the menu, select a category and click an active module card to toggle it.
- In **Streamer**, enter the original nickname and the display alias, then press **Save Nickname**.

## Compatibility

- Minecraft Java 1.21.11
- Fabric Loader 0.19.5+
- Fabric API for 1.21.11
- Java 21+ at runtime

The build uses JDK 25 and Gradle 9.7.1. The mod compiles for Java 21. MojoLauncher/Android runtime compatibility still requires an in-game test on the target device; a successful CI build is not proof of that device test.

## Build

```sh
gradle build
```

The GitHub Actions workflow verifies the mod metadata, icon, generated GUI texture, and a JAR size of at least 5 MiB before uploading the artifact.

## Scope and limitations

This release focuses on client-side visual and privacy features. The other menu sections remain marked as upcoming until their functions are implemented and tested. It does not include through-wall player ESP, x-ray, or server-side changes.
