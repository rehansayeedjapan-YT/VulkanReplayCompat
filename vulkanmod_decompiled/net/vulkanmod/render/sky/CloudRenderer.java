/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1011
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_3532
 *  net.minecraft.class_4063
 *  net.minecraft.class_9801
 *  org.apache.commons.lang3.Validate
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 */
package net.vulkanmod.render.sky;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.class_1011;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3532;
import net.minecraft.class_4063;
import net.minecraft.class_9801;
import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.render.VBO;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class CloudRenderer {
    private static final class_2960 TEXTURE_LOCATION = class_2960.method_60656((String)"textures/environment/clouds.png");
    private static final int DIR_NEG_Y_BIT = 1;
    private static final int DIR_POS_Y_BIT = 2;
    private static final int DIR_NEG_X_BIT = 4;
    private static final int DIR_POS_X_BIT = 8;
    private static final int DIR_NEG_Z_BIT = 16;
    private static final int DIR_POS_Z_BIT = 32;
    private static final byte Y_BELOW_CLOUDS = 0;
    private static final byte Y_ABOVE_CLOUDS = 1;
    private static final byte Y_INSIDE_CLOUDS = 2;
    private static final int CELL_WIDTH = 12;
    private static final int CELL_HEIGHT = 4;
    private CloudGrid cloudGrid;
    private int prevCloudX;
    private int prevCloudZ;
    private byte prevCloudY;
    private class_4063 prevCloudsType;
    private boolean generateClouds;
    private VBO cloudBuffer;

    public CloudRenderer() {
        this.loadTexture();
    }

    public void loadTexture() {
        this.cloudGrid = CloudRenderer.createCloudGrid(TEXTURE_LOCATION);
    }

    public void renderClouds(float cloudHeight, int cloudColor, double camX, double camY, double camZ, long gameTime, float partialTicks) {
        boolean disableCull;
        class_310 minecraft = class_310.method_1551();
        float timeOffset = (float)(gameTime % ((long)this.cloudGrid.width * 400L)) + partialTicks;
        double centerX = camX + (double)(timeOffset * 0.03f);
        double centerZ = camZ + (double)3.96f;
        double centerY = cloudHeight - (float)camY + 0.33f;
        int centerCellX = (int)Math.floor(centerX / 12.0);
        int centerCellZ = (int)Math.floor(centerZ / 12.0);
        byte yState = centerY < -4.0 ? (byte)0 : (centerY > 0.0 ? (byte)1 : 2);
        if (centerCellX != this.prevCloudX || centerCellZ != this.prevCloudZ || minecraft.field_1690.method_1632() != this.prevCloudsType || this.prevCloudY != yState || this.cloudBuffer == null) {
            this.prevCloudX = centerCellX;
            this.prevCloudZ = centerCellZ;
            this.prevCloudsType = minecraft.field_1690.method_1632();
            this.prevCloudY = yState;
            this.generateClouds = true;
        }
        if (this.generateClouds) {
            this.generateClouds = false;
            if (this.cloudBuffer != null) {
                this.cloudBuffer.close();
            }
            this.resetBuffer();
            class_9801 cloudsMesh = this.buildClouds(class_289.method_1348(), centerCellX, centerCellZ, centerY);
            if (cloudsMesh == null) {
                return;
            }
            this.cloudBuffer = new VBO(true);
            this.cloudBuffer.upload(cloudsMesh);
        }
        if (this.cloudBuffer == null) {
            return;
        }
        float xTranslation = (float)(centerX - (double)(centerCellX * 12));
        float yTranslation = (float)centerY;
        float zTranslation = (float)(centerZ - (double)(centerCellZ * 12));
        Renderer.getInstance().getMainPass().rebindMainTarget();
        Matrix4fStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushMatrix();
        poseStack.translate(-xTranslation, yTranslation, -zTranslation);
        VRenderSystem.applyModelViewMatrix((Matrix4f)poseStack);
        VRenderSystem.calculateMVP();
        VRenderSystem.setModelOffset(-xTranslation, 0.0f, -zTranslation);
        float r = ColorUtil.ARGB.unpackR(cloudColor);
        float g = ColorUtil.ARGB.unpackG(cloudColor);
        float b = ColorUtil.ARGB.unpackB(cloudColor);
        VRenderSystem.setShaderColor(r, g, b, 0.8f);
        GraphicsPipeline pipeline = PipelineManager.getCloudsPipeline();
        VRenderSystem.enableBlend();
        VRenderSystem.blendFuncSeparate(770, 771, 1, 0);
        VRenderSystem.enableDepthTest();
        VRenderSystem.depthFunc(515);
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask((boolean)true);
        GlStateManager._colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        GlStateManager._disablePolygonOffset();
        VRenderSystem.setPolygonModeGL(6914);
        VRenderSystem.setPrimitiveTopologyGL(4);
        boolean fastClouds = this.prevCloudsType == class_4063.field_18163;
        boolean insideClouds = yState == 2;
        boolean bl = disableCull = insideClouds || fastClouds && centerY <= 0.0;
        if (disableCull) {
            VRenderSystem.disableCull();
        } else {
            VRenderSystem.enableCull();
        }
        if (!fastClouds) {
            VRenderSystem.colorMask(false, false, false, false);
            this.cloudBuffer.bind(pipeline);
            this.cloudBuffer.draw();
            VRenderSystem.colorMask(true, true, true, true);
        }
        this.cloudBuffer.bind(pipeline);
        this.cloudBuffer.draw();
        poseStack.popMatrix();
        VRenderSystem.enableCull();
        VRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        VRenderSystem.setModelOffset(0.0f, 0.0f, 0.0f);
    }

    public void resetBuffer() {
        if (this.cloudBuffer != null) {
            this.cloudBuffer.close();
            this.cloudBuffer = null;
        }
    }

    private class_9801 buildClouds(class_289 tesselator, int centerCellX, int centerCellZ, double cloudY) {
        boolean insideClouds;
        float upFaceBrightness = 1.0f;
        float xDirBrightness = 0.9f;
        float downFaceBrightness = 0.7f;
        float zDirBrightness = 0.8f;
        class_287 bufferBuilder = tesselator.method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        int cloudRange = Math.min((Integer)class_310.method_1551().field_1690.method_71270().method_41753(), 128) * 16;
        int renderDistance = class_3532.method_15386((float)((float)cloudRange / 12.0f));
        boolean bl = insideClouds = this.prevCloudY == 2;
        if (this.prevCloudsType == class_4063.field_18164) {
            for (int cellX = -renderDistance; cellX < renderDistance; ++cellX) {
                for (int cellZ = -renderDistance; cellZ < renderDistance; ++cellZ) {
                    int color;
                    int cellIdx = this.cloudGrid.getWrappedIdx(centerCellX + cellX, centerCellZ + cellZ);
                    byte renderFaces = this.cloudGrid.renderFaces[cellIdx];
                    int baseColor = this.cloudGrid.pixels[cellIdx];
                    float x = cellX * 12;
                    float z = cellZ * 12;
                    if ((renderFaces & 2) != 0 && cloudY <= 0.0) {
                        color = ColorUtil.ARGB.multiplyRGB(baseColor, 1.0f);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 12.0f, color);
                    }
                    if ((renderFaces & 1) != 0 && cloudY >= -4.0) {
                        color = ColorUtil.ARGB.multiplyRGB(baseColor, 0.7f);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 12.0f, color);
                    }
                    if ((renderFaces & 8) != 0 && (x < 1.0f || insideClouds)) {
                        color = ColorUtil.ARGB.multiplyRGB(baseColor, 0.9f);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 0.0f, color);
                    }
                    if ((renderFaces & 4) != 0 && (x > -1.0f || insideClouds)) {
                        color = ColorUtil.ARGB.multiplyRGB(baseColor, 0.9f);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 0.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 12.0f, color);
                    }
                    if ((renderFaces & 0x20) != 0 && (z < 1.0f || insideClouds)) {
                        color = ColorUtil.ARGB.multiplyRGB(baseColor, 0.8f);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 12.0f, color);
                        CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 12.0f, color);
                    }
                    if ((renderFaces & 0x10) == 0 || !(z > -1.0f) && !insideClouds) continue;
                    color = ColorUtil.ARGB.multiplyRGB(baseColor, 0.8f);
                    CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 4.0f, z + 0.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 0.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 0.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 4.0f, z + 0.0f, color);
                }
            }
        } else {
            for (int cellX = -renderDistance; cellX < renderDistance; ++cellX) {
                for (int cellZ = -renderDistance; cellZ < renderDistance; ++cellZ) {
                    int cellIdx = this.cloudGrid.getWrappedIdx(centerCellX + cellX, centerCellZ + cellZ);
                    byte renderFaces = this.cloudGrid.renderFaces[cellIdx];
                    int baseColor = this.cloudGrid.pixels[cellIdx];
                    float x = cellX * 12;
                    float z = cellZ * 12;
                    if ((renderFaces & 1) == 0) continue;
                    int color = ColorUtil.ARGB.multiplyRGB(baseColor, 1.0f);
                    CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 12.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 0.0f, 0.0f, z + 0.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 0.0f, color);
                    CloudRenderer.putVertex(bufferBuilder, x + 12.0f, 0.0f, z + 12.0f, color);
                }
            }
        }
        return bufferBuilder.method_60794();
    }

    private static void putVertex(class_287 bufferBuilder, float x, float y, float z, int color) {
        bufferBuilder.method_22912(x, y, z).method_39415(color);
    }

    private static CloudGrid createCloudGrid(class_2960 textureLocation) {
        CloudGrid cloudGrid;
        block8: {
            class_3300 resourceManager = class_310.method_1551().method_1478();
            class_3298 resource = resourceManager.getResourceOrThrow(textureLocation);
            InputStream inputStream = resource.method_14482();
            try {
                class_1011 image = class_1011.method_4309((InputStream)inputStream);
                int width = image.method_4307();
                int height = image.method_4323();
                Validate.isTrue((width == height ? 1 : 0) != 0, (String)"Image width and height must be the same", (Object[])new Object[0]);
                int[] pixels = image.method_48463();
                cloudGrid = new CloudGrid(pixels, width);
                if (inputStream == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            inputStream.close();
        }
        return cloudGrid;
    }

    static class CloudGrid {
        final int width;
        final int[] pixels;
        final byte[] renderFaces;

        CloudGrid(int[] pixels, int width) {
            this.pixels = pixels;
            this.width = width;
            this.renderFaces = this.computeRenderFaces();
        }

        byte[] computeRenderFaces() {
            byte[] renderFaces = new byte[this.pixels.length];
            for (int z = 0; z < this.width; ++z) {
                for (int x = 0; x < this.width; ++x) {
                    int idx = this.getIdx(x, z);
                    int pixel = this.pixels[idx];
                    if (!CloudGrid.hasColor(pixel)) continue;
                    int faces = 3;
                    int adjPixel = this.getTexelWrapped(x - 1, z);
                    if (pixel != adjPixel) {
                        faces = (byte)(faces | 4);
                    }
                    if (pixel != (adjPixel = this.getTexelWrapped(x + 1, z))) {
                        faces = (byte)(faces | 8);
                    }
                    if (pixel != (adjPixel = this.getTexelWrapped(x, z - 1))) {
                        faces = (byte)(faces | 0x10);
                    }
                    if (pixel != (adjPixel = this.getTexelWrapped(x, z + 1))) {
                        faces = (byte)(faces | 0x20);
                    }
                    renderFaces[idx] = faces;
                }
            }
            return renderFaces;
        }

        int getTexelWrapped(int x, int z) {
            if (x < 0) {
                x = this.width - 1;
            }
            if (x > this.width - 1) {
                x = 0;
            }
            if (z < 0) {
                z = this.width - 1;
            }
            if (z > this.width - 1) {
                z = 0;
            }
            return this.pixels[this.getIdx(x, z)];
        }

        int getWrappedIdx(int x, int z) {
            x = Math.floorMod(x, this.width);
            z = Math.floorMod(z, this.width);
            return this.getIdx(x, z);
        }

        int getIdx(int x, int z) {
            return z * this.width + x;
        }

        private static boolean hasColor(int pixel) {
            return (pixel >> 24 & 0xFF) > 1;
        }
    }
}

