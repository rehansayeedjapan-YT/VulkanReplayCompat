/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.system.NativeType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.compatibility.gl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.vulkanmod.gl.VkGlBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={GL15.class})
public class GL15M {
    @Overwrite(remap=false)
    @NativeType(value="void")
    public static int glGenBuffers() {
        return VkGlBuffer.glGenBuffers();
    }

    @Overwrite(remap=false)
    public static void glBindBuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int buffer) {
        VkGlBuffer.glBindBuffer(target, buffer);
    }

    @Overwrite(remap=false)
    public static void glBufferData(@NativeType(value="GLenum") int target, @NativeType(value="void const *") ByteBuffer data, @NativeType(value="GLenum") int usage) {
        VkGlBuffer.glBufferData(target, data, usage);
    }

    @Overwrite(remap=false)
    public static void glBufferData(int i, long l, int j) {
        VkGlBuffer.glBufferData(i, l, j);
    }

    @Overwrite(remap=false)
    @NativeType(value="void *")
    public static ByteBuffer glMapBuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int access) {
        return VkGlBuffer.glMapBuffer(target, access);
    }

    @Overwrite(remap=false)
    @NativeType(value="void *")
    @Nullable
    public static ByteBuffer glMapBuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int access, long length, @Nullable ByteBuffer old_buffer) {
        return VkGlBuffer.glMapBuffer(target, access);
    }

    @Overwrite(remap=false)
    @NativeType(value="GLboolean")
    public static boolean glUnmapBuffer(@NativeType(value="GLenum") int target) {
        return VkGlBuffer.glUnmapBuffer(target);
    }

    @Overwrite(remap=false)
    public static void glDeleteBuffers(int i) {
        VkGlBuffer.glDeleteBuffers(i);
    }

    @Overwrite(remap=false)
    public static void glDeleteBuffers(@NativeType(value="GLuint const *") IntBuffer buffers) {
        VkGlBuffer.glDeleteBuffers(buffers);
    }
}

