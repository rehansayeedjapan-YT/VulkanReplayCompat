/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.vulkanmod.mixin.render.frame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={RenderSystem.class})
public class RenderSystemMixin {
    @Redirect(method={"flipFrame"}, at=@At(value="INVOKE", target="Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"), remap=false)
    private static void endFrame(long window) {
        Renderer.getInstance().endFrame();
    }
}

