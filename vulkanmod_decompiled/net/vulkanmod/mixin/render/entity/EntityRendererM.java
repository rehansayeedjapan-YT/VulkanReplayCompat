/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_4604
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.vulkanmod.mixin.render.entity;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4604;
import net.minecraft.class_897;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_897.class})
public class EntityRendererM<T extends class_1297> {
    @Redirect(method={"method_3933"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_4604;method_23093(Lnet/minecraft/class_238;)Z"))
    private boolean isVisible(class_4604 frustum, class_238 aABB) {
        if (Initializer.CONFIG.entityCulling) {
            WorldRenderer worldRenderer = WorldRenderer.getInstance();
            class_243 pos = aABB.method_1005();
            RenderSection section = worldRenderer.getSectionGrid().getSectionAtBlockPos((int)pos.method_10216(), (int)pos.method_10214(), (int)pos.method_10215());
            if (section == null) {
                return frustum.method_23093(aABB);
            }
            return worldRenderer.getLastFrame() == section.getLastFrame();
        }
        return frustum.method_23093(aABB);
    }
}

