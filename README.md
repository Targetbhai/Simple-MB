# Simple Motion Blur
Lightweight camera-based motion blur for Minecraft 1.21.1 (Fabric). Works with Sodium and VulkanMod.

- Strength slider + on/off toggle (Mod Menu config screen, needs Mod Menu)
- Keybind toggle (default: B)
- Config file: `config/simplemotionblur.json`

It blends the previous frame into the current one based on how fast the camera turns/moves,
so cost is just two screen-sized quads per frame (very low FPS impact).

## Build
Push to GitHub -> Actions tab -> "build" -> download the artifact jar.
Or locally: `gradle build` (Java 21) -> `build/libs/`.
