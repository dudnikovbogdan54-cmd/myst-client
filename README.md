# Myst Visuals

Original client-side Fabric visual mod by **Aloxmyst_YT** for Minecraft Java **1.21.11**.

## Implemented modules

- **HUD:** watermark, FPS, XYZ coordinates, server ping, Target HUD, block name.
- **Visuals:** custom crosshair and Fullbright using the 1.21.11 lightmap brightness hooks.
- **Effects:** a brief purple decorative frame when the currently targeted living entity is defeated.
- **Marks / Waypoints:** multiple personal waypoints with exact editable X/Y/Z coordinates, custom names, six colors, per-waypoint enable/disable and optional distance display. Waypoints are saved locally.
- **Utilities:** hold **C** for temporary zoom when Zoom is enabled; Keystrokes displays W/A/S/D and mouse buttons.
- **Streamer Mode:** local player-name aliases in the TAB list. Aliases are only visible on this client.
- Module switches and waypoint data are saved under the Minecraft `config` directory.
- A compact resizable menu with saved accent themes and compact/standard card density.
- HUD panels now have saved background and opacity controls, and their accent follows the selected Myst theme.
- A custom purple menu texture is bundled in the JAR and optimized for a smaller download.

## Controls

- **Right Shift** — open the menu.
- **B** — add a waypoint at your exact current player coordinates.
- **C (hold)** — temporary zoom, when enabled.
- On the **Marks** page, left-click the Waypoints card to toggle the module; right-click it to open the waypoint manager.
- In the waypoint manager, enter a name and exact X/Y/Z, then save. Left-click a listed waypoint to enable/disable it; right-click it to load its settings for editing. You can toggle its distance display, cycle its color, add a point at your current location, or delete the selected point.

## Compatibility

- Minecraft Java 1.21.11
- Fabric Loader 0.19.5+
- Fabric API for 1.21.11
- Java 21+ at runtime

The project is built by GitHub Actions using JDK 25 and targets Java 21 bytecode. CI verifies the JAR archive and required classes/resources. A direct JAR download is published as a GitHub Release. Successful CI compilation does not prove runtime compatibility on every launcher or Android device; MojoLauncher still needs an in-game test.

## Not implemented yet

Block Overlay, Clear Weather FX, Hit Particles, Totem Pop FX, extra animations, Death Marker, CPS Counter, and full config profiles remain marked as upcoming in the menu. They are not presented as working features.

No through-wall player ESP, x-ray, or server-side modifications are included.
