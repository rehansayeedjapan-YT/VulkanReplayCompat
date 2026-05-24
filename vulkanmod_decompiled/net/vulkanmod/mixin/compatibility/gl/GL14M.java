/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.system.NativeType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.compatibility.gl;

import net.vulkanmod.vulkan.VRenderSystem;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={GL14.class})
public class GL14M {
    @Overwrite(remap=false)
    public static void glBlendFuncSeparate(@NativeType(value="GLenum") int sfactorRGB, @NativeType(value="GLenum") int dfactorRGB, @NativeType(value="GLenum") int sfactorAlpha, @NativeType(value="GLenum") int dfactorAlpha) {
        VRenderSystem.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }
}

