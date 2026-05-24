/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.system.NativeType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.compatibility.gl;

import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.gl.VkGlRenderbuffer;
import net.vulkanmod.gl.VkGlTexture;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={GL30.class})
public class GL30M {
    @Overwrite(remap=false)
    public static void glGenerateMipmap(@NativeType(value="GLenum") int target) {
        VkGlTexture.generateMipmap(target);
    }

    @NativeType(value="void")
    @Overwrite(remap=false)
    public static int glGenFramebuffers() {
        return VkGlFramebuffer.genFramebufferId();
    }

    @Overwrite(remap=false)
    public static void glBindFramebuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int framebuffer) {
        VkGlFramebuffer.bindFramebuffer(target, framebuffer);
    }

    @Overwrite(remap=false)
    public static void glFramebufferTexture2D(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int attachment, @NativeType(value="GLenum") int textarget, @NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level) {
        VkGlFramebuffer.framebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Overwrite(remap=false)
    public static void glFramebufferRenderbuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int attachment, @NativeType(value="GLenum") int renderbuffertarget, @NativeType(value="GLuint") int renderbuffer) {
    }

    @Overwrite(remap=false)
    public static void glDeleteFramebuffers(@NativeType(value="GLuint const *") int framebuffer) {
        VkGlFramebuffer.deleteFramebuffer(framebuffer);
    }

    @Overwrite(remap=false)
    @NativeType(value="GLenum")
    public static int glCheckFramebufferStatus(@NativeType(value="GLenum") int target) {
        return VkGlFramebuffer.glCheckFramebufferStatus(target);
    }

    @Overwrite(remap=false)
    public static void glBlitFramebuffer(@NativeType(value="GLint") int srcX0, @NativeType(value="GLint") int srcY0, @NativeType(value="GLint") int srcX1, @NativeType(value="GLint") int srcY1, @NativeType(value="GLint") int dstX0, @NativeType(value="GLint") int dstY0, @NativeType(value="GLint") int dstX1, @NativeType(value="GLint") int dstY1, @NativeType(value="GLbitfield") int mask, @NativeType(value="GLenum") int filter) {
        VkGlFramebuffer.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @NativeType(value="void")
    @Overwrite(remap=false)
    public static int glGenRenderbuffers() {
        return VkGlRenderbuffer.genId();
    }

    @Overwrite(remap=false)
    public static void glBindRenderbuffer(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int framebuffer) {
        VkGlRenderbuffer.bindRenderbuffer(target, framebuffer);
    }

    @Overwrite(remap=false)
    public static void glRenderbufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="GLenum") int internalformat, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height) {
        VkGlRenderbuffer.renderbufferStorage(target, internalformat, width, height);
    }

    @Overwrite(remap=false)
    public static void glDeleteRenderbuffers(@NativeType(value="GLuint const *") int renderbuffer) {
        VkGlRenderbuffer.deleteRenderbuffer(renderbuffer);
    }
}

