/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11239
 *  net.minecraft.class_11241
 *  net.minecraft.class_11246
 *  net.minecraft.class_11256
 *  net.minecraft.class_12137
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11239;
import net.minecraft.class_11241;
import net.minecraft.class_11246;
import net.minecraft.class_11256;
import net.minecraft.class_12137;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_11239.class})
public class PictureInPictureRendererM<T extends class_11256> {
    @Shadow
    @Nullable
    private GpuTextureView field_60572;

    @Overwrite
    public void method_72114(T pictureInPictureRenderState, class_11246 guiRenderState) {
        guiRenderState.method_71996(new class_11241(class_10799.field_59968, class_11231.method_70900((GpuTextureView)this.field_60572, (class_12137)RenderSystem.getSamplerCache().method_75297(FilterMode.NEAREST)), pictureInPictureRenderState.method_72127(), pictureInPictureRenderState.comp_4122(), pictureInPictureRenderState.comp_4123(), pictureInPictureRenderState.comp_4124(), pictureInPictureRenderState.comp_4125(), 0.0f, 1.0f, 0.0f, 1.0f, -1, pictureInPictureRenderState.comp_4128(), null));
    }
}

