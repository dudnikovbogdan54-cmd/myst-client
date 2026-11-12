# Mist Visuals

Original client-side Fabric visual mod by **Aloxmyst_YT** for Minecraft Java **1.21.11**.

## Implemented modules

- **HUD:** watermark, FPS, XYZ coordinates, server ping, Target HUD, block name.
- **Visuals:** custom crosshair and Fullbright using the 1.21.11 lightmap brightness hooks.
- **Effects:** a brief purple decorative frame when the currently targeted living entity is defeated.
- **Marks:** one saved personal waypoint. Press **B** in-world to save your current coordinates; when Waypoints is enabled, the HUD shows its coordinates and live distance.
- **Utilities:** hold **C** for temporary zoom when Zoom is enabled; Keystrokes displays W/A/S/D and mouse buttons.
- **Streamer Mode:** local player-name aliases in the TAB list. Aliases are only visible on this client.
- Module switches and waypoint data are saved under the Minecraft `config` directory.
- Original high-resolution purple menu texture is bundled in the JAR.

## Controls

- **Right Shift** — open the menu.
- **B** — save/update the personal waypoint.
- **C (hold)** — temporary zoom, when enabled.
- Click implemented module cards to toggle them.

## Compatibility

- Minecraft Java 1.21.11
- Fabric Loader 0.19.5+
- Fabric API for 1.21.11
- Java 21+ at runtime

The project is built by GitHub Actions using JDK 25 and targets Java 21 bytecode. The CI job checks archive contents and requires a JAR size of at least 5 MiB. Successful CI compilation does not prove runtime compatibility on every launcher or Android device; MojoLauncher still needs an in-game test.

## Not implemented yet

Block Overlay, Clear Weather FX, Hit Particles, Totem Pop FX, extra animations, Death Marker, CPS Counter, and full config profiles remain marked as upcoming in the menu. They are not presented as working features.

No through-wall player ESP, x-ray, or server-side modifications are included.
