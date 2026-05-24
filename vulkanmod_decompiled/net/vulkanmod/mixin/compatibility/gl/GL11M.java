/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.NativeType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.compatibility.gl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={GL11.class})
public class GL11M {
    @Overwrite(remap=false)
    public static void glScissor(@NativeType(value="GLint") int x, @NativeType(value="GLint") int y, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height) {
        Renderer.setScissor(x, y, width, height);
    }

    @Overwrite(remap=false)
    public static void glViewport(@NativeType(value="GLint") int x, @NativeType(value="GLint") int y, @NativeType(value="GLsizei") int w, @NativeType(value="GLsizei") int h) {
        Renderer.setViewport(x, y, w, h);
    }

    @Overwrite(remap=false)
    public static void glBindTexture(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int texture) {
        VkGlTexture.bindTexture(texture);
    }

    @Overwrite(remap=false)
    public static void glLineWidth(@NativeType(value="GLfloat") float width) {
        VRenderSystem.setLineWidth(width);
    }

    @NativeType(value="void")
    @Overwrite(remap=false)
    public static int glGenTextures() {
        return VkGlTexture.genTextureId();
    }

    @NativeType(value="GLboolean")
    @Overwrite(remap=false)
    public static boolean glIsEnabled(@NativeType(value="GLenum") int cap) {
        return true;
    }

    @Overwrite(remap=false)
    public static void glClear(@NativeType(value="GLbitfield") int mask) {
        VRenderSystem.clear(mask);
    }

    @NativeType(value="GLenum")
    @Overwrite(remap=false)
    public static int glGetError() {
        return 0;
    }

    @Overwrite(remap=false)
    public static void glClearColor(@NativeType(value="GLfloat") float red, @NativeType(value="GLfloat") float green, @NativeType(value="GLfloat") float blue, @NativeType(value="GLfloat") float alpha) {
        VRenderSystem.setClearColor(red, green, blue, alpha);
    }

    @Overwrite(remap=false)
    public static void glDepthFunc(@NativeType(value="GLenum") int func) {
        VRenderSystem.depthFunc(func);
    }

    @Overwrite(remap=false)
    public static void glClearDepth(@NativeType(value="GLdouble") double depth) {
        VRenderSystem.clearDepth(depth);
    }

    @Overwrite(remap=false)
    public static void glDepthMask(@NativeType(value="GLboolean") boolean flag) {
        VRenderSystem.depthMask(flag);
    }

    @NativeType(value="void")
    @Overwrite(remap=false)
    public static int glGetInteger(@NativeType(value="GLenum") int pname) {
        return 0;
    }

    @Overwrite(remap=false)
    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
        VkGlTexture.texImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Overwrite(remap=false)
    public static void glTexImage2D(@NativeType(value="GLenum") int target, @NativeType(value="GLint") int level, @NativeType(value="GLint") int internalformat, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLint") int border, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") long pixels) {
        VkGlTexture.texImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Overwrite(remap=false)
    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, long pixels) {
        VkGlTexture.texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, pixels);
    }

    @Overwrite(remap=false)
    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, @Nullable ByteBuffer pixels) {
        VkGlTexture.texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, pixels);
    }

    @Overwrite(remap=false)
    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, @Nullable IntBuffer pixels) {
        VkGlTexture.texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, MemoryUtil.memByteBuffer((IntBuffer)pixels));
    }

    @Overwrite(remap=false)
    public static void glTexParameteri(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int pname, @NativeType(value="GLint") int param) {
        VkGlTexture.texParameteri(target, pname, param);
    }

    @Overwrite(remap=false)
    public static void glTexParameterf(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int pname, @NativeType(value="GLfloat") float param) {
    }

    @Overwrite(remap=false)
    public static int glGetTexParameteri(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int pname) {
        return VkGlTexture.getTexParameteri(target, pname);
    }

    @Overwrite(remap=false)
    public static int glGetTexLevelParameteri(@NativeType(value="GLenum") int target, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int pname) {
        return VkGlTexture.getTexLevelParameter(target, level, pname);
    }

    @Overwrite(remap=false)
    public static void glPixelStorei(@NativeType(value="GLenum") int pname, @NativeType(value="GLint") int param) {
        VkGlTexture.pixelStoreI(pname, param);
    }

    @Overwrite(remap=false)
    public static void glEnable(@NativeType(value="GLenum") int target) {
    }

    @Overwrite(remap=false)
    public static void glDisable(@NativeType(value="GLenum") int target) {
    }

    @Overwrite(remap=false)
    public static void glFinish() {
    }

    @Overwrite(remap=false)
    public static void glHint(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int hint) {
    }

    @Overwrite(remap=false)
    public static void glDeleteTextures(@NativeType(value="GLuint const *") int texture) {
        VkGlTexture.glDeleteTextures(texture);
    }

    @Overwrite(remap=false)
    public static void glDeleteTextures(@NativeType(value="GLuint const *") IntBuffer textures) {
        VkGlTexture.glDeleteTextures(textures);
    }

    @Overwrite(remap=false)
    public static void glGetTexImage(@NativeType(value="GLenum") int tex, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void *") long pixels) {
        VkGlTexture.getTexImage(tex, level, format, type, pixels);
    }

    @Overwrite(remap=false)
    public static void glGetTexImage(@NativeType(value="GLenum") int tex, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void *") ByteBuffer pixels) {
        VkGlTexture.getTexImage(tex, level, format, type, MemoryUtil.memAddress((ByteBuffer)pixels));
    }

    @Overwrite(remap=false)
    public static void glGetTexImage(@NativeType(value="GLenum") int tex, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void *") IntBuffer pixels) {
        VkGlTexture.getTexImage(tex, level, format, type, MemoryUtil.memAddress((IntBuffer)pixels));
    }

    @Overwrite(remap=false)
    public static void glCopyTexSubImage2D(@NativeType(value="GLenum") int target, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int x, @NativeType(value="GLint") int y, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height) {
    }

    @Overwrite(remap=false)
    public static void glBlendFunc(@NativeType(value="GLenum") int sfactor, @NativeType(value="GLenum") int dfactor) {
    }

    @Overwrite(remap=false)
    public static void glPolygonOffset(@NativeType(value="GLfloat") float factor, @NativeType(value="GLfloat") float units) {
        VRenderSystem.polygonOffset(factor, units);
    }
}

