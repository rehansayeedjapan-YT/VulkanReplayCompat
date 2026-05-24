# VulkanReplayCompat

A Fabric 1.21.11 compatibility bridge mod designed to fix rendering issues when using **VulkanMod** alongside the **ReplayMod (Flashback)**.

## The Problem
When using ReplayMod (specifically the Flashback UI) with VulkanMod, the custom ImGui interface either crashes the game or completely fails to render. This happens because ReplayMod's UI is heavily optimized for OpenGL (`GLCapabilities`, direct `glDrawElements`, etc.), which completely bypasses the rendering hooks that VulkanMod provides to translate Minecraft's rendering into Vulkan APIs.

## The Solution
This bridge mod intercepts the custom ImGui rendering pipeline within Flashback and forcefully translates the raw vertex/index data into vanilla Minecraft `BufferBuilder` calls. These calls are natively intercepted and correctly processed by VulkanMod.

### Key Fixes Implemented:
1. **GLCapabilities Bypass:** Overrode ReplayMod's `updateFontsTexture` to stop it from directly making raw OpenGL calls that query `GLCapabilities` (which crashes under Vulkan). Instead, we natively create the font textures using VulkanMod's OpenGL wrappers.
2. **Pipeline State Forcing:** VulkanMod heavily optimizes rendering by caching pipeline states. To ensure the UI draws correctly, we use reflection to forcefully disable Culling, disable Depth Testing, enable Blending, and push Identity matrices to `VRenderSystem`.
3. **UBO Updates:** Forced `UniformBufferObject (UBO)` updates via reflection on the `GUI_TEXTURED` pipeline. This prevents the UI from drawing invisibly due to stale/zeroed MVP matrices from the previous frame.
4. **imgui-java Buffer Bug Fix:** Resolved a critical bug involving `imgui-java` (v1.86.11). The library utilizes a single shared static `ByteBuffer` for performance to fetch data from JNI. Fetching the index buffer immediately after the vertex buffer overwrote the vertex memory with index data. We allocate a new direct `ByteBuffer` and manually copy the vertex data out to prevent this memory overwrite.
5. **Byte Order Fix:** Re-enabled `ByteOrder.nativeOrder()` to prevent the JVM from interpreting little-endian native ImGui C++ structures (like indices and float coordinates) as big-endian Java primitives, which previously resulted in garbage sub-normal floats (e.g. `1.102E-39`).
6. **Correct Pipeline Fetching:** Intercepted the correct `GUI_TEXTURED` VulkanMod pipeline via `RenderPipelines` instead of fetching the currently bound shader (which would randomly apply chunk shaders or entity shaders to the UI).

## Building
To build this mod, run:
```bash
./gradlew build
```
The compiled jar will be available in `build/libs/`.

## Requirements
- Fabric Loader 1.21.11
- VulkanMod 1.21.11
- Flashback (ReplayMod) 1.21.11
