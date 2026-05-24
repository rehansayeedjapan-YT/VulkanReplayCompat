/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3675
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.vulkanmod.mixin.wayland;

import net.minecraft.class_3675;
import net.vulkanmod.config.Platform;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_3675.class})
public class InputConstantsM {
    @Redirect(method={"method_15984"}, at=@At(value="INVOKE", target="Lorg/lwjgl/glfw/GLFW;glfwSetCursorPos(JDD)V"))
    private static void grabOrReleaseMouse(long window, double xpos, double ypos) {
        if (!Platform.isWayLand()) {
            GLFW.glfwSetCursorPos((long)window, (double)xpos, (double)ypos);
        }
    }
}

