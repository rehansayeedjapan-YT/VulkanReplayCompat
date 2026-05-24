/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  net.minecraft.class_11531
 *  net.minecraft.class_11532
 *  net.minecraft.class_11658
 *  net.minecraft.class_11661
 *  net.minecraft.class_11684
 *  net.minecraft.class_12137
 *  net.minecraft.class_1923
 *  net.minecraft.class_2338
 *  net.minecraft.class_310
 *  net.minecraft.class_3191
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4599
 *  net.minecraft.class_4604
 *  net.minecraft.class_638
 *  net.minecraft.class_757
 *  net.minecraft.class_761
 *  net.minecraft.class_824
 *  net.minecraft.class_898
 *  net.minecraft.class_9779
 *  net.minecraft.class_9922
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.chunk;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.SortedSet;
import net.minecraft.class_11531;
import net.minecraft.class_11532;
import net.minecraft.class_11658;
import net.minecraft.class_11661;
import net.minecraft.class_11684;
import net.minecraft.class_12137;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_3191;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_4604;
import net.minecraft.class_638;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_824;
import net.minecraft.class_898;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<class_3191>> field_20950;
    @Unique
    private WorldRenderer worldRenderer;
    @Unique
    double camX;
    @Unique
    double camY;
    @Unique
    double camZ;
    @Unique
    Matrix4f modelView;
    @Unique
    Matrix4f projection;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void init(class_310 minecraft, class_898 entityRenderDispatcher, class_824 blockEntityRenderDispatcher, class_4599 renderBuffers, class_11658 levelRenderState, class_11684 featureRenderDispatcher, CallbackInfo ci) {
        this.worldRenderer = WorldRenderer.init(entityRenderDispatcher, blockEntityRenderDispatcher, renderBuffers, levelRenderState, featureRenderDispatcher);
    }

    @Inject(method={"method_3244"}, at={@At(value="RETURN")})
    private void setLevel(class_638 clientLevel, CallbackInfo ci) {
        this.worldRenderer.setLevel(clientLevel);
    }

    @Inject(method={"method_3279"}, at={@At(value="RETURN")})
    private void onAllChanged(CallbackInfo ci) {
        this.worldRenderer.allChanged();
    }

    @Inject(method={"method_74314"}, at={@At(value="HEAD")}, cancellable=true)
    private void onExtractVisibleBlockEntities(class_4184 camera, float partialTick, class_11658 levelRenderState, CallbackInfo ci) {
        this.worldRenderer.setPartialTick(partialTick);
        ci.cancel();
    }

    @Inject(method={"method_62208"}, at={@At(value="RETURN")}, cancellable=true)
    private void onSubmitBlockEntities(class_4587 poseStack, class_11658 levelRenderState, class_11661 submitNodeStorage, CallbackInfo ci) {
        this.worldRenderer.renderBlockEntities(poseStack, levelRenderState, submitNodeStorage, this.field_20950);
        ci.cancel();
    }

    @Overwrite
    private void method_74752(class_4184 camera, class_4604 frustum, boolean spectator) {
        this.worldRenderer.setupRenderer(camera, frustum, false, spectator);
    }

    @Overwrite
    public boolean method_40050(class_2338 blockPos) {
        return this.worldRenderer.isSectionCompiled(blockPos);
    }

    @Inject(method={"method_22710"}, at={@At(value="HEAD")})
    private void updateMatrices(class_9922 graphicsResourceAllocator, class_9779 deltaTracker, boolean bl, class_4184 camera, Matrix4f modelView, Matrix4f projection, Matrix4f matrix4f, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        this.modelView = modelView;
        this.projection = projection;
    }

    @Overwrite
    private class_11532 method_72157(Matrix4fc matrix4fc, double camX, double camY, double camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
        return null;
    }

    @Redirect(method={"method_62214"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11532;method_72170(Lnet/minecraft/class_11531;Lnet/minecraft/class_12137;)V"))
    private void renderSectionLayer(class_11532 instance, class_11531 chunkSectionLayerGroup, class_12137 gpuSampler) {
        if (chunkSectionLayerGroup == class_11531.field_61022) {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.push("Opaque_terrain");
            this.worldRenderer.renderSectionLayer(TerrainRenderType.SOLID, this.camX, this.camY, this.camZ, this.modelView, this.projection);
            this.worldRenderer.renderSectionLayer(TerrainRenderType.CUTOUT, this.camX, this.camY, this.camZ, this.modelView, this.projection);
        } else if (chunkSectionLayerGroup == class_11531.field_61023) {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.pop();
            profiler.push("Translucent_terrain");
            this.worldRenderer.renderSectionLayer(TerrainRenderType.TRANSLUCENT, this.camX, this.camY, this.camZ, this.modelView, this.projection);
            profiler.pop();
        }
    }

    @Overwrite
    public void method_65201(class_1923 chunkPos) {
    }

    @Overwrite
    private void method_3295(int x, int y, int z, boolean flag) {
        this.worldRenderer.setSectionDirty(x, y, z, flag);
    }

    @Overwrite
    public String method_3289() {
        return this.worldRenderer.getChunkStatistics();
    }

    @Overwrite
    public boolean method_3281() {
        return !this.worldRenderer.graphNeedsUpdate() && this.worldRenderer.getTaskDispatcher().isIdle();
    }

    @Overwrite
    public int method_3246() {
        return this.worldRenderer.getVisibleSectionsCount();
    }

    @Redirect(method={"method_62203"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_757;method_32796()F"))
    private float getRenderDistanceZFar(class_757 instance) {
        return instance.method_3193() * 4.0f;
    }
}

