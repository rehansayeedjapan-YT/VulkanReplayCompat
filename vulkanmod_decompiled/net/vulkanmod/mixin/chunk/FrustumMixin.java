/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4604
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_4604;
import net.vulkanmod.interfaces.FrustumMixed;
import net.vulkanmod.render.chunk.frustum.VFrustum;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4604.class})
public class FrustumMixin
implements FrustumMixed {
    @Shadow
    private double field_20995;
    @Shadow
    private double field_20996;
    @Shadow
    private double field_20997;
    @Shadow
    @Final
    private Matrix4f field_40824;
    @Shadow
    private Vector4f field_34821;
    @Unique
    private final VFrustum vFrustum = new VFrustum();

    @Inject(method={"method_23092"}, at={@At(value="HEAD")})
    private void calculateFrustum(Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {
        this.vFrustum.calculateFrustum(modelView, projection);
        this.field_34821 = this.field_40824.transformTranspose(new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
    }

    @Inject(method={"method_23088"}, at={@At(value="RETURN")})
    public void prepare(double d, double e, double f, CallbackInfo ci) {
        this.vFrustum.setCamOffset(this.field_20995, this.field_20996, this.field_20997);
    }

    @Override
    public VFrustum customFrustum() {
        return this.vFrustum;
    }
}

