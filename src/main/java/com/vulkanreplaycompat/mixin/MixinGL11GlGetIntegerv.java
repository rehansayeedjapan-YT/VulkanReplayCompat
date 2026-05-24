package com.vulkanreplaycompat.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.IntBuffer;

/**
 * Intercepts GL11.glGetIntegerv (both array and IntBuffer overloads) which VulkanMod does not override.
 *
 * VulkanMod overrides glGetInteger(int) → returns 0, but NOT glGetIntegerv(int, int[]).
 * Without this, any call to glGetIntegerv crashes with "No GLCapabilities instance has been set"
 * because there is no real OpenGL context when VulkanMod is active.
 *
 * Returning all-zeros is safe — callers use these to save/restore state, and zero is a valid
 * "no texture bound / no program bound" default (same semantic as VulkanMod's glGetInteger=0).
 */
@Mixin(value = GL11.class, remap = false)
public class MixinGL11GlGetIntegerv {

    /** @author VulkanReplayCompat @reason No real OpenGL context under VulkanMod */
    @Overwrite(remap = false)
    public static void glGetIntegerv(int pname, int[] params) {
        if (params != null) {
            java.util.Arrays.fill(params, 0);
        }
    }

    /** @author VulkanReplayCompat @reason No real OpenGL context under VulkanMod */
    @Overwrite(remap = false)
    public static void glGetIntegerv(int pname, IntBuffer params) {
        // Return zeros — callers treat 0 as "nothing bound" which is safe
        if (params != null) {
            for (int i = params.position(); i < params.limit(); i++) {
                params.put(i, 0);
            }
        }
    }

    /** @author VulkanReplayCompat @reason No real OpenGL context under VulkanMod */
    @Overwrite(remap = false)
    public static String glGetString(int pname) {
        if (pname == GL11.GL_VERSION) {
            return "3.3.0 VulkanMod Dummy";
        }
        return "";
    }
}
