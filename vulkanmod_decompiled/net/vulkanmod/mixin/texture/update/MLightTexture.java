/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1309
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_757
 *  net.minecraft.class_765
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package net.vulkanmod.mixin.texture.update;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_757;
import net.minecraft.class_765;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={class_765.class})
public class MLightTexture {
    @Unique
    private static final Vector3f END_FLASH_SKY_LIGHT_COLOR = new Vector3f(0.9f, 0.5f, 1.0f);
    @Shadow
    @Final
    private class_310 field_4137;
    @Shadow
    @Final
    private class_757 field_4134;
    @Shadow
    private boolean field_4135;
    @Shadow
    private float field_21528;
    @Unique
    private class_1043 lightTexture;
    @Unique
    private GpuTextureView textureView;
    @Unique
    private class_1011 lightPixels;
    private Vector3f[] tempVecs;

    @Unique
    private float getDarknessGamma(float f) {
        class_1293 mobEffectInstance = this.field_4137.field_1724.method_6112(class_1294.field_38092);
        return mobEffectInstance != null ? mobEffectInstance.method_55653((class_1309)this.field_4137.field_1724, f) : 0.0f;
    }

    @Unique
    private float calculateDarknessScale(class_1309 livingEntity, float f, float g) {
        float h = 0.45f * f;
        return Math.max(0.0f, class_3532.method_15362((double)(((float)livingEntity.field_6012 - g) * (float)Math.PI * 0.025f)) * h);
    }

    @Unique
    private static float lerp(float a, float x, float t) {
        return (x - a) * t + a;
    }

    @Unique
    private static void clampColor(Vector3f vector3f) {
        vector3f.set(class_3532.method_15363((float)vector3f.x, (float)0.0f, (float)1.0f), class_3532.method_15363((float)vector3f.y, (float)0.0f, (float)1.0f), class_3532.method_15363((float)vector3f.z, (float)0.0f, (float)1.0f));
    }

    @Unique
    private float notGamma(float f) {
        float g = 1.0f - f;
        g *= g;
        return 1.0f - g * g;
    }

    @Unique
    private static float getBrightness(float ambientLight, int i) {
        float f = (float)i / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return class_3532.method_16439((float)ambientLight, (float)g, (float)1.0f);
    }
}

