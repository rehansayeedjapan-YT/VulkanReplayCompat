/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_11228
 *  net.minecraft.class_11231
 *  net.minecraft.class_11241
 *  net.minecraft.class_11245
 *  net.minecraft.class_11246
 *  net.minecraft.class_12137
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10799;
import net.minecraft.class_11228;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_11245;
import net.minecraft.class_11246;
import net.minecraft.class_12137;
import net.vulkanmod.render.engine.VkRenderPass;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11228.class})
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    private class_11246 field_59914;
    @Shadow
    @Nullable
    private GpuTextureView field_60571;

    @Inject(method={"method_70887"}, at={@At(value="HEAD")}, cancellable=true)
    private void submitBlitFromItemAtlas(class_11245 guiItemRenderState, float u, float v, int size, int atlasSize, CallbackInfo ci) {
        v = 1.0f - v;
        float u1 = u + (float)size / (float)atlasSize;
        float v1 = v + (float)size / (float)atlasSize;
        this.field_59914.method_71996(new class_11241(class_10799.field_59968, class_11231.method_70900((GpuTextureView)this.field_60571, (class_12137)RenderSystem.getSamplerCache().method_75297(FilterMode.NEAREST)), guiItemRenderState.method_72120(), guiItemRenderState.method_72122(), guiItemRenderState.method_72123(), guiItemRenderState.method_72122() + 16, guiItemRenderState.method_72123() + 16, u, u1, v, v1, -1, guiItemRenderState.method_72124(), null));
        ci.cancel();
    }

    @Redirect(method={"method_70886"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$class_5595;)V"))
    private void removeIndexBuffer(RenderPass instance, GpuBuffer gpuBuffer, VertexFormat.class_5595 indexType) {
    }

    @Redirect(method={"method_70886"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
    private void useVertexCount(RenderPass renderPass, int baseVertex, int firstIndex, int indexCount, int instanceCount) {
        VkRenderPass vkRenderPass = (VkRenderPass)renderPass;
        if (vkRenderPass.getPipeline().getVertexFormatMode() != VertexFormat.class_5596.field_27379) {
            int vertexCount = indexCount * 2 / 3;
            renderPass.drawIndexed(baseVertex, 0, vertexCount, 1);
        } else {
            renderPass.drawIndexed(baseVertex, 0, indexCount, 1);
        }
    }
}

