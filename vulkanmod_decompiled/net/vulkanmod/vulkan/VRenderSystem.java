/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_7285
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_7285;
import net.vulkanmod.render.engine.VkGpuBuffer;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.util.ColorUtil;
import net.vulkanmod.vulkan.util.MappedBuffer;
import net.vulkanmod.vulkan.util.VUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

public abstract class VRenderSystem {
    private static final float DEFAULT_DEPTH_VALUE = 1.0f;
    private static long window;
    public static boolean depthTest;
    public static boolean depthMask;
    public static int depthFun;
    public static int topology;
    public static int polygonMode;
    public static boolean canSetLineWidth;
    public static int colorMask;
    public static boolean cull;
    public static boolean logicOp;
    public static int logicOpFun;
    private static final GpuTextureView[] shaderTextures;
    public static float clearDepthValue;
    public static FloatBuffer clearColor;
    public static MappedBuffer modelViewMatrix;
    public static MappedBuffer projectionMatrix;
    public static MappedBuffer TextureMatrix;
    public static MappedBuffer MVP;
    public static MappedBuffer modelOffset;
    public static MappedBuffer lightDirection0;
    public static MappedBuffer lightDirection1;
    public static MappedBuffer shaderColor;
    public static MappedBuffer shaderFogColor;
    public static class_7285 fogData;
    public static MappedBuffer screenSize;
    public static MappedBuffer textureSize;
    public static MappedBuffer texelSize;
    public static float alphaCutout;
    private static boolean depthBiasEnabled;
    private static float depthBiasConstant;
    private static float depthBiasSlope;
    private static int currentTime;

    public static void initRenderer() {
        Vulkan.initVulkan(window);
        VRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void setShaderTexture(int i, @Nullable GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();
        if (i >= 0 && i < shaderTextures.length) {
            VRenderSystem.shaderTextures[i] = gpuTextureView;
        }
    }

    @Nullable
    public static GpuTextureView getShaderTexture(int i) {
        RenderSystem.assertOnRenderThread();
        return i >= 0 && i < shaderTextures.length ? shaderTextures[i] : null;
    }

    public static MappedBuffer getScreenSize() {
        VRenderSystem.updateScreenSize();
        return screenSize;
    }

    public static void updateScreenSize() {
        class_1041 window = class_310.method_1551().method_22683();
        screenSize.putFloat(0, window.method_4489());
        screenSize.putFloat(4, window.method_4506());
    }

    public static MappedBuffer getTextureSize() {
        return textureSize;
    }

    public static MappedBuffer getTexelSize() {
        return texelSize;
    }

    public static void setTextureSize(int width, int height) {
        textureSize.putInt(0, width);
        textureSize.putInt(4, height);
        texelSize.putFloat(0, 1.0f / (float)width);
        texelSize.putFloat(4, 1.0f / (float)height);
    }

    public static void setWindow(long window) {
        VRenderSystem.window = window;
    }

    public static ByteBuffer getModelOffset() {
        return VRenderSystem.modelOffset.buffer;
    }

    public static int maxSupportedTextureSize() {
        return DeviceManager.deviceProperties.limits().maxImageDimension2D();
    }

    public static void applyMVP(Matrix4f MV, Matrix4f P) {
        VRenderSystem.applyModelViewMatrix(MV);
        VRenderSystem.applyProjectionMatrix(P);
        VRenderSystem.calculateMVP();
    }

    public static void applyModelViewMatrix(Matrix4f mat) {
        mat.get(VRenderSystem.modelViewMatrix.buffer.asFloatBuffer());
    }

    public static void applyProjectionMatrix(Matrix4f mat) {
        mat.get(VRenderSystem.projectionMatrix.buffer.asFloatBuffer());
    }

    public static void applyProjectionMatrix(GpuBufferSlice bufferSlice) {
        long ptr = ((VkGpuBuffer)bufferSlice.buffer()).getBuffer().getDataPtr();
        ByteBuffer byteBuffer = MemoryUtil.memByteBuffer((long)(ptr + bufferSlice.offset()), (int)((int)bufferSlice.length()));
        Matrix4f matrix4f = new Matrix4f().set(byteBuffer);
        matrix4f.get(VRenderSystem.projectionMatrix.buffer.asFloatBuffer());
    }

    public static void calculateMVP() {
        Matrix4f MV = new Matrix4f(VRenderSystem.modelViewMatrix.buffer.asFloatBuffer());
        Matrix4f P = new Matrix4f(VRenderSystem.projectionMatrix.buffer.asFloatBuffer());
        P.mul((Matrix4fc)MV).get(VRenderSystem.MVP.buffer);
    }

    public static void setTextureMatrix(Matrix4f mat) {
        mat.get(VRenderSystem.TextureMatrix.buffer.asFloatBuffer());
    }

    public static MappedBuffer getTextureMatrix() {
        return TextureMatrix;
    }

    public static MappedBuffer getModelViewMatrix() {
        return modelViewMatrix;
    }

    public static MappedBuffer getProjectionMatrix() {
        return projectionMatrix;
    }

    public static MappedBuffer getMVP() {
        return MVP;
    }

    public static void setModelOffset(float x, float y, float z) {
        long ptr = VRenderSystem.modelOffset.ptr;
        VUtil.UNSAFE.putFloat(ptr, x);
        VUtil.UNSAFE.putFloat(ptr + 4L, y);
        VUtil.UNSAFE.putFloat(ptr + 8L, z);
    }

    public static void setShaderColor(float f1, float f2, float f3, float f4) {
        ColorUtil.setRGBA_Buffer(shaderColor, f1, f2, f3, f4);
    }

    public static void setShaderFogColor(float f1, float f2, float f3, float f4) {
        ColorUtil.setRGBA_Buffer(shaderFogColor, f1, f2, f3, f4);
    }

    public static MappedBuffer getShaderColor() {
        return shaderColor;
    }

    public static MappedBuffer getShaderFogColor() {
        return shaderFogColor;
    }

    public static class_7285 getFogData() {
        return fogData;
    }

    public static void setCurrentTime(int currentTime) {
        VRenderSystem.currentTime = currentTime;
    }

    public static int getCurrentTime() {
        return currentTime;
    }

    public static void setClearColor(float f1, float f2, float f3, float f4) {
        ColorUtil.setRGBA_Buffer(clearColor, f1, f2, f3, f4);
    }

    public static void clear(int mask) {
        Renderer.clearAttachments(mask);
    }

    public static void clearDepth(double depth) {
        clearDepthValue = (float)depth;
    }

    public static void disableDepthTest() {
        depthTest = false;
    }

    public static void depthMask(boolean b) {
        depthMask = b;
    }

    public static void setPrimitiveTopologyGL(int mode) {
        topology = switch (mode) {
            case 1, 3 -> 1;
            case 4, 5, 6 -> 3;
            case 0 -> 0;
            default -> throw new RuntimeException(String.format("Unknown GL primitive topology: %s", mode));
        };
    }

    public static void setPolygonModeGL(int mode) {
        polygonMode = switch (mode) {
            case 6912 -> 2;
            case 6913 -> 1;
            case 6914 -> 0;
            default -> throw new RuntimeException(String.format("Unknown GL polygon mode: %s", mode));
        };
    }

    public static void setLineWidth(float width) {
        if (canSetLineWidth) {
            Renderer.setLineWidth(width);
        }
    }

    public static void colorMask(boolean b, boolean b1, boolean b2, boolean b3) {
        colorMask = PipelineState.ColorMask.getColorMask(b, b1, b2, b3);
    }

    public static int getColorMask() {
        return colorMask;
    }

    public static void enableDepthTest() {
        depthTest = true;
    }

    public static void enableCull() {
        cull = true;
    }

    public static void disableCull() {
        cull = false;
    }

    public static void depthFunc(int depthFun) {
        VRenderSystem.depthFun = depthFun;
    }

    public static void enableBlend() {
        PipelineState.blendInfo.enabled = true;
    }

    public static void disableBlend() {
        PipelineState.blendInfo.enabled = false;
    }

    public static void blendFunc(int srcFactor, int dstFactor) {
        PipelineState.blendInfo.setBlendFunction(srcFactor, dstFactor);
    }

    public static void blendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
        PipelineState.blendInfo.setBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
    }

    public static void blendOp(int op) {
        PipelineState.blendInfo.setBlendOp(op);
    }

    public static void enableColorLogicOp() {
        logicOp = true;
    }

    public static void disableColorLogicOp() {
        logicOp = false;
    }

    public static void logicOp(int glLogicOp) {
        logicOpFun = glLogicOp;
    }

    public static void polygonOffset(float slope, float biasConstant) {
        if (depthBiasConstant != biasConstant || depthBiasSlope != slope) {
            depthBiasConstant = biasConstant;
            depthBiasSlope = slope;
            Renderer.setDepthBias(depthBiasConstant, depthBiasSlope);
        }
    }

    public static void enablePolygonOffset() {
        if (!depthBiasEnabled) {
            Renderer.setDepthBias(depthBiasConstant, depthBiasSlope);
            depthBiasEnabled = true;
        }
    }

    public static void disablePolygonOffset() {
        if (depthBiasEnabled) {
            Renderer.setDepthBias(0.0f, 0.0f);
            depthBiasEnabled = false;
        }
    }

    static {
        depthTest = true;
        depthMask = true;
        depthFun = 515;
        topology = 3;
        polygonMode = 0;
        canSetLineWidth = false;
        colorMask = PipelineState.ColorMask.getColorMask(true, true, true, true);
        cull = true;
        logicOp = false;
        logicOpFun = 0;
        shaderTextures = new GpuTextureView[12];
        clearDepthValue = 1.0f;
        clearColor = MemoryUtil.memCallocFloat((int)4);
        modelViewMatrix = new MappedBuffer(64);
        projectionMatrix = new MappedBuffer(64);
        TextureMatrix = new MappedBuffer(64);
        MVP = new MappedBuffer(64);
        modelOffset = new MappedBuffer(12);
        lightDirection0 = new MappedBuffer(12);
        lightDirection1 = new MappedBuffer(12);
        shaderColor = new MappedBuffer(16);
        shaderFogColor = new MappedBuffer(16);
        screenSize = new MappedBuffer(8);
        textureSize = new MappedBuffer(8);
        texelSize = new MappedBuffer(8);
        alphaCutout = 0.0f;
        depthBiasEnabled = false;
        depthBiasConstant = 0.0f;
        depthBiasSlope = 0.0f;
    }
}

