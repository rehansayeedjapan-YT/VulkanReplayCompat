/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_5365
 *  net.minecraft.class_542
 *  net.minecraft.class_7172
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_5365;
import net.minecraft.class_542;
import net.minecraft.class_7172;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import org.spongepowered.asm.mixin.Final;
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
    @Shadow
    @Final
    public class_315 field_1690;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void forceGraphicsMode(class_542 gameConfig, CallbackInfo ci) {
        class_7172 graphicsModeOption = this.field_1690.method_75329();
        if (graphicsModeOption.method_41753() == class_5365.field_25429) {
            Initializer.LOGGER.error("Fabulous graphics mode not supported, forcing Fancy.");
            graphicsModeOption.method_41748((Object)class_5365.field_25428);
        }
        if (((Boolean)this.field_1690.method_75337().method_41753()).booleanValue()) {
            Initializer.LOGGER.error("Improved transparency currently not supported, forcing it off.");
            this.field_1690.method_75337().method_41748((Object)false);
        }
    }

    @Inject(method={"method_1523"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_310;method_1574()V")})
    private void redirectResourceTick(boolean bl, CallbackInfo ci, @Local(ordinal=0) int i, @Local(ordinal=1) int j) {
        int n = Math.min(10, i) - 1;
        boolean doUpload = j == n;
        SpriteUpdateUtil.setDoUpload(doUpload);
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    public void close(CallbackInfo ci) {
        Vulkan.waitIdle();
    }

    @Inject(method={"close"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_3682;close()V")})
    public void close2(CallbackInfo ci) {
        Vulkan.cleanUp();
    }

    @Inject(method={"method_15993"}, at={@At(value="HEAD")})
    public void onResolutionChanged(CallbackInfo ci) {
        Renderer.scheduleSwapChainUpdate();
    }

    @Redirect(method={"method_1507"}, at=@At(value="FIELD", target="Lnet/minecraft/class_310;field_1743:Z", opcode=181))
    private void keepVar(class_310 instance, boolean value) {
    }
}

