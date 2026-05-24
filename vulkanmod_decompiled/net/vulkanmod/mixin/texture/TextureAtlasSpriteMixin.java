/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  net.minecraft.class_1058
 *  net.minecraft.class_7764
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.system.MemoryUtil
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.texture;

import com.mojang.blaze3d.buffers.Std140Builder;
import java.nio.ByteBuffer;
import net.minecraft.class_1058;
import net.minecraft.class_7764;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_1058.class})
public class TextureAtlasSpriteMixin {
    @Shadow
    @Final
    private class_7764 field_40553;
    @Shadow
    @Final
    private int field_64248;
    @Shadow
    @Final
    private int field_5258;
    @Shadow
    @Final
    private int field_5256;

    @Overwrite
    public void method_76320(ByteBuffer byteBuffer, int i, int maxMipLevel, int width, int height, int uboSize) {
        for (int n = 0; n <= maxMipLevel; ++n) {
            Std140Builder.intoBuffer((ByteBuffer)MemoryUtil.memSlice((ByteBuffer)byteBuffer, (int)(i + n * uboSize), (int)uboSize)).putMat4f((Matrix4fc)new Matrix4f().ortho2D(0.0f, (float)(width >> n), (float)(height >> n), 0.0f)).putMat4f((Matrix4fc)new Matrix4f().translate((float)(this.field_5258 >> n), (float)(this.field_5256 >> n), 0.0f).scale((float)(this.field_40553.method_45807() + this.field_64248 * 2 >> n), (float)(this.field_40553.method_45815() + this.field_64248 * 2 >> n), 1.0f)).putFloat((float)this.field_64248 / (float)this.field_40553.method_45807()).putFloat((float)this.field_64248 / (float)this.field_40553.method_45815()).putInt(n);
        }
    }
}

