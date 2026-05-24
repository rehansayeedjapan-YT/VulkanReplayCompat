/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.textures.GpuTexture
 *  net.minecraft.class_276
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frame;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class})
public class MinecraftMixin {
    @Shadow
    public boolean field_1743;

    @Inject(method={"method_1523"}, at={@At(value="HEAD")})
    private void preFrameOps(boolean bl, CallbackInfo ci) {
        Renderer.getInstance().beginFrame();
        Renderer.clearAttachments(16640);
    }

    @Redirect(method={"method_1523"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;ILcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private void beginRender(CommandEncoder instance, GpuTexture gpuTexture, int i, GpuTexture gpuTexture2, double v) {
        ImageUploadHelper.INSTANCE.submitCommands();
    }

    @Inject(method={"method_76795(Lnet/minecraft/class_437;Z)V"}, at={@At(value="RETURN")})
    private void beginRender2(CallbackInfo ci) {
    }

    @Redirect(method={"method_1523"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_276;method_1237()V"))
    private void removeBlit(class_276 instance) {
    }

    @Redirect(method={"method_1523"}, at=@At(value="INVOKE", target="Ljava/lang/Thread;yield()V"))
    private void removeThreadYield() {
    }
}

