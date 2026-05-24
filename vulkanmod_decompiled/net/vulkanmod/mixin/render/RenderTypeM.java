/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$class_5590
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_11219
 *  net.minecraft.class_12247
 *  net.minecraft.class_12247$class_12337
 *  net.minecraft.class_1921
 *  net.minecraft.class_276
 *  net.minecraft.class_9801
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.class_11219;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_9801;
import net.vulkanmod.interfaces.ExtendedRenderType;
import net.vulkanmod.mixin.render.RenderSetupAccessor;
import net.vulkanmod.render.engine.VkCommandEncoder;
import net.vulkanmod.render.engine.VkRenderPass;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1921.class})
public class RenderTypeM
implements ExtendedRenderType {
    @Unique
    TerrainRenderType terrainRenderType;
    @Shadow
    @Final
    private class_12247 field_64013;
    @Shadow
    @Final
    protected String field_64011;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void inj(String string, class_12247 renderSetup, CallbackInfo ci) {
        this.terrainRenderType = switch (string) {
            case "solid" -> TerrainRenderType.SOLID;
            case "cutout" -> TerrainRenderType.CUTOUT;
            case "translucent" -> TerrainRenderType.TRANSLUCENT;
            case "tripwire" -> TerrainRenderType.TRIPWIRE;
            default -> null;
        };
    }

    @Override
    public TerrainRenderType getTerrainRenderType() {
        return this.terrainRenderType;
    }

    @Overwrite
    public void method_60895(class_9801 meshData) {
        GpuTextureView gpuTextureView;
        VertexFormat.class_5595 indexType;
        GpuBuffer gpuBuffer2;
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        RenderSetupAccessor renderSetupAccessor = (RenderSetupAccessor)this.field_64013;
        Consumer consumer = renderSetupAccessor.layeringTransform().method_75918();
        if (consumer != null) {
            matrix4fStack.pushMatrix();
            consumer.accept(matrix4fStack);
        }
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)renderSetupAccessor.textureTransform().method_76030());
        Map map = this.field_64013.method_75926();
        GpuBuffer gpuBuffer = renderSetupAccessor.pipeline().getVertexFormat().uploadImmediateVertexBuffer(meshData.method_60818());
        if (meshData.method_60821() == null) {
            RenderSystem.class_5590 autoStorageIndexBuffer = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)meshData.method_60822().comp_752());
            gpuBuffer2 = autoStorageIndexBuffer.method_68274(meshData.method_60822().comp_751());
            indexType = autoStorageIndexBuffer.method_31924();
        } else {
            gpuBuffer2 = renderSetupAccessor.pipeline().getVertexFormat().uploadImmediateIndexBuffer(meshData.method_60821());
            indexType = meshData.method_60822().comp_753();
        }
        class_276 renderTarget = renderSetupAccessor.outputTarget().method_75921();
        GpuTextureView gpuTextureView2 = gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.method_71639();
        GpuTextureView gpuTextureView22 = renderTarget.field_1478 ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.method_71640()) : null;
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + this.field_64011, gpuTextureView, OptionalInt.empty(), gpuTextureView22, OptionalDouble.empty());){
            renderPass.setPipeline(renderSetupAccessor.pipeline());
            class_11219 scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.method_72091()) {
                renderPass.enableScissor(scissorState.method_72092(), scissorState.method_72093(), scissorState.method_72094(), scissorState.method_72095());
            }
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, gpuBuffer);
            for (Map.Entry entry : map.entrySet()) {
                renderPass.bindTexture((String)entry.getKey(), ((class_12247.class_12337)entry.getValue()).comp_5226(), ((class_12247.class_12337)entry.getValue()).comp_5227());
            }
            VRenderSystem.applyModelViewMatrix(RenderSystem.getModelViewMatrix());
            VRenderSystem.calculateMVP();
            renderPass.setIndexBuffer(gpuBuffer2, indexType);
            VkCommandEncoder commandEncoder = (VkCommandEncoder)RenderSystem.getDevice().createCommandEncoder();
            commandEncoder.trySetup((VkRenderPass)renderPass);
            Renderer.getDrawer().draw(meshData.method_60818(), meshData.method_60821(), meshData.method_60822().comp_752(), meshData.method_60822().comp_749(), meshData.method_60822().comp_750());
        }
        if (meshData != null) {
            meshData.close();
        }
        if (consumer != null) {
            matrix4fStack.popMatrix();
        }
    }
}

