package com.vulkanreplaycompat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RawProjectionMatrix;
import com.mojang.blaze3d.systems.ProjectionType;
import org.joml.Matrix4f;
import imgui.moulberry90.ImDrawData;
import imgui.moulberry90.ImVec4;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(targets = "com.moulberry.flashback.editor.ui.CustomImGuiImplGl3")
public class MixinCustomImGuiImplGl3 {

    @Unique
    private static RawProjectionMatrix rawProjMatrix = null;

    // Cache VRenderSystem methods to avoid repeated reflection
    @Unique
    private static Method vrsApplyProj = null;
    @Unique
    private static Method vrsApplyModelView = null;
    @Unique
    private static Method vrsCalculateMVP = null;
    @Unique
    private static Method vrsDisableCull = null;
    @Unique
    private static Method vrsEnableCull = null;
    @Unique
    private static Method vrsDisableDepthTest = null;
    @Unique
    private static Method vrsEnableDepthTest = null;
    @Unique
    private static Method vrsEnableBlend = null;
    @Unique
    private static Method vrsDisableBlend = null;
    @Unique
    private static Method vrsBlendFunc = null;
    @Unique
    private static boolean vrsInitialized = false;
    @Unique
    private static boolean vrsAvailable = false;

    @Unique
    private static net.minecraft.util.Identifier fontTextureId = null;

    @Unique
    private static void ensureFontTexture() {
        if (fontTextureId == null) {
            fontTextureId = net.minecraft.util.Identifier.of("flashback", "font");
        }
    }

    @Unique
    private static boolean checkVrsAvailable() {
        try {
            Class.forName("net.vulkanmod.vulkan.VRenderSystem");
            return false; // FORCE VANILLA PATH
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Unique
    private static void initVRenderSystem() {
        if (vrsInitialized) return;
        vrsInitialized = true;
        try {
            Class<?> cls = Class.forName("net.vulkanmod.vulkan.VRenderSystem");
            vrsApplyProj       = cls.getMethod("applyProjectionMatrix", Matrix4f.class);
            vrsApplyModelView  = cls.getMethod("applyModelViewMatrix",  Matrix4f.class);
            vrsCalculateMVP    = cls.getMethod("calculateMVP");
            vrsDisableCull     = cls.getMethod("disableCull");
            vrsEnableCull      = cls.getMethod("enableCull");
            vrsDisableDepthTest= cls.getMethod("disableDepthTest");
            vrsEnableDepthTest = cls.getMethod("enableDepthTest");
            vrsEnableBlend     = cls.getMethod("enableBlend");
            vrsDisableBlend    = cls.getMethod("disableBlend");
            vrsBlendFunc       = cls.getMethod("blendFunc", int.class, int.class);
            vrsAvailable = true;
        } catch (Exception e) {
            System.err.println("[VulkanReplayCompat] VRenderSystem not found: " + e.getMessage());
        }
    }

    /**
     * @author Antigravity
     * @reason Replace OpenGL ImGui rendering with VulkanMod-compatible rendering for Minecraft 1.21.11
     */
    @Inject(method = "renderDrawData", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderDrawData(ImDrawData drawData, CallbackInfo ci) {
        ci.cancel();
        if (drawData.getCmdListsCount() <= 0) {
            return;
        }

        System.out.println("[VulkanReplayCompat] Drawing ImGui: " + drawData.getCmdListsCount() + " command lists!");
        int fbWidth  = (int) drawData.getDisplaySizeX();
        int fbHeight = (int) drawData.getDisplaySizeY();
        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        float clipOffX = drawData.getDisplayPosX();
        float clipOffY = drawData.getDisplayPosY();

        // Build orthographic projection matching pixel coordinates (ImGui style: top-left origin)
        Matrix4f ortho    = new Matrix4f().setOrtho(0.0f, fbWidth, fbHeight, 0.0f, -1000.0f, 1000.0f);
        Matrix4f identity = new Matrix4f().identity();

        // ---------------------------------------------------------------
        // 1. Push matrices to VulkanMod's VRenderSystem AND call calculateMVP
        //    calculateMVP() is CRITICAL — without it the MVP UBO stays stale
        //    and the GPU reads garbage, causing VK_ERROR_DEVICE_LOST.
        // ---------------------------------------------------------------
        initVRenderSystem();
        if (vrsAvailable) {
            try {
                vrsDisableCull.invoke(null);
                vrsDisableDepthTest.invoke(null);
                vrsEnableBlend.invoke(null);
                vrsBlendFunc.invoke(null, 770, 771);
                vrsApplyProj.invoke(null, ortho);
                vrsApplyModelView.invoke(null, identity);
                vrsCalculateMVP.invoke(null); // <-- THE KEY FIX
            } catch (Exception e) {
                System.err.println("[VulkanReplayCompat] Failed to update VRenderSystem matrices: " + e.getMessage());
            }
        }

        // 2. Set Minecraft RenderSystem projection matrix (for pipeline UBO binding)
        try {
            if (rawProjMatrix == null) {
                rawProjMatrix = new RawProjectionMatrix("FlashbackImGui_Proj");
            }
            com.mojang.blaze3d.buffers.GpuBufferSlice slice = rawProjMatrix.set(ortho);
            RenderSystem.setProjectionMatrix(slice, ProjectionType.ORTHOGRAPHIC);
        } catch (Exception e) {
            System.err.println("[VulkanReplayCompat] Failed to set projection matrix: " + e.getMessage());
            e.printStackTrace();
        }

        // 3. Get the GUI_TEXTURED VulkanMod pipeline (cached via ExtendedRenderPipeline)
        Object vkPipeline     = null;
        Object rendererInst   = null;
        Method bindPipeline   = null;
        Method uploadUBOs     = null;
        Method pushConstants  = null;
        try {
            Object mcPipeline = net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED;
            Class<?> extClass = Class.forName("net.vulkanmod.interfaces.shader.ExtendedRenderPipeline");
            
            System.out.println("[VulkanReplayCompat] mcPipeline=" + mcPipeline);
            System.out.println("[VulkanReplayCompat] extClass.isInstance=" + extClass.isInstance(mcPipeline));

            if (extClass.isInstance(mcPipeline)) {
                // Try to get pipeline
                vkPipeline = extClass.getMethod("getPipeline").invoke(mcPipeline);
                System.out.println("[VulkanReplayCompat] Initial vkPipeline=" + vkPipeline);
                
                // If null, it hasn't been compiled yet. Force compile it!
                if (vkPipeline == null) {
                    try {
                        Class<?> rendererClass = Class.forName("net.vulkanmod.vulkan.Renderer");
                        Object rInst = rendererClass.getMethod("getInstance").invoke(null);
                        Object device = rendererClass.getMethod("getDevice").invoke(rInst);
                        Class<?> deviceClass = Class.forName("net.vulkanmod.render.engine.VkGpuDevice");
                        Class<?> rpClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipeline");
                        deviceClass.getMethod("compilePipeline", rpClass).invoke(device, mcPipeline);
                        
                        // Try again
                        vkPipeline = extClass.getMethod("getPipeline").invoke(mcPipeline);
                        System.out.println("[VulkanReplayCompat] Forced compilation of GUI_TEXTURED pipeline! Success=" + (vkPipeline != null));
                    } catch (Exception e) {
                        System.err.println("[VulkanReplayCompat] Failed to compile GUI_TEXTURED: " + e.getMessage());
                    }
                }
                
                rendererInst = Renderer.getInstance();
                Class<?> graphPipeClass = Class.forName("net.vulkanmod.vulkan.shader.GraphicsPipeline");
                Class<?> basePipeClass  = Class.forName("net.vulkanmod.vulkan.shader.Pipeline");
                bindPipeline  = rendererInst.getClass().getMethod("bindGraphicsPipeline", graphPipeClass);
                uploadUBOs    = rendererInst.getClass().getMethod("uploadAndBindUBOs",    basePipeClass);
                pushConstants = rendererInst.getClass().getMethod("pushConstants",        basePipeClass);
            }
        } catch (Exception e) {
            System.err.println("[VulkanReplayCompat] Could not resolve VulkanMod pipeline: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. Locate Drawer.draw(ByteBuffer, DrawMode, VertexFormat, int)
        Object drawerInst = null;
        Method drawerDraw = null;
        try {
            drawerInst = Renderer.getDrawer();
            for (Method m : drawerInst.getClass().getMethods()) {
                if (m.getName().equals("draw") && m.getParameterCount() == 4) {
                    drawerDraw = m;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[VulkanReplayCompat] Could not get Drawer: " + e.getMessage());
            e.printStackTrace();
        }

        Tessellator tessellator = Tessellator.getInstance();
        ImVec4 clipRect = new ImVec4();

        try {
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            
            try {
                Class<?> vrsClass = Class.forName("net.vulkanmod.vulkan.VRenderSystem");
                vrsClass.getMethod("setShaderColor", float.class, float.class, float.class, float.class).invoke(null, 1.0f, 1.0f, 1.0f, 1.0f);
            } catch (Exception e) {}

            try {
                Class<?> rendererClass = Class.forName("net.vulkanmod.vulkan.Renderer");
                int physWidth = net.minecraft.client.MinecraftClient.getInstance().getWindow().getFramebufferWidth();
                int physHeight = net.minecraft.client.MinecraftClient.getInstance().getWindow().getFramebufferHeight();
                rendererClass.getMethod("setViewportState", int.class, int.class, int.class, int.class).invoke(null, 0, 0, physWidth, physHeight);
                rendererClass.getMethod("setScissor", int.class, int.class, int.class, int.class).invoke(null, 0, 0, physWidth, physHeight);
            } catch (Exception e) {
                System.err.println("[VulkanReplayCompat] Failed to setup viewport/scissor: " + e.getMessage());
            }

            for (int n = 0; n < drawData.getCmdListsCount(); n++) {
                ByteBuffer rawVtx = drawData.getCmdListVtxBufferData(n);
                rawVtx.rewind();
                rawVtx.rewind();
                ByteBuffer vtxBuffer = ByteBuffer.allocateDirect(rawVtx.remaining()).order(ByteOrder.nativeOrder());
                vtxBuffer.put(rawVtx);
                vtxBuffer.flip();
                
                ByteBuffer idxBuffer = drawData.getCmdListIdxBufferData(n).order(ByteOrder.nativeOrder());

                final int vtxStride = 20; // ImDrawVert: xy(8) + uv(8) + col(4)
                boolean isIdx16 = (imgui.moulberry90.ImDrawData.sizeOfImDrawIdx() == 2);

                for (int cmdIdx = 0; cmdIdx < drawData.getCmdListCmdBufferSize(n); cmdIdx++) {
                    drawData.getCmdListCmdBufferClipRect(clipRect, n, cmdIdx);

                    float clipMinX = clipRect.x - clipOffX;
                    float clipMinY = clipRect.y - clipOffY;
                    float clipMaxX = clipRect.z - clipOffX;
                    float clipMaxY = clipRect.w - clipOffY;

                    if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) continue;

                    // RenderSystem.enableScissorForRenderTypeDraws(
                    //         (int) clipMinX,
                    //         (int) clipMinY,
                    //         (int) (clipMaxX - clipMinX),
                    //         (int) (clipMaxY - clipMinY)
                    // );
                    // RenderSystem.disableScissor();

                    int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);
                    int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
                    int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);

                    ensureFontTexture();
                    if (fontTextureId != null) {
                        try {
                            Object texture = net.minecraft.client.MinecraftClient.getInstance().getTextureManager().getTexture(fontTextureId);
                            if (texture != null) {
                                Class<?> vrsClass = Class.forName("net.vulkanmod.vulkan.VRenderSystem");
                                for (java.lang.reflect.Method m : vrsClass.getMethods()) {
                                    if (m.getName().equals("setShaderTexture")) {
                                        m.invoke(null, 0, texture);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {}
                    }
                    BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
                    boolean anyVertices = false;

                    for (int i = 0; i < elemCount; i++) {
                        int idxByteOffset = (idxOffset + i) * (isIdx16 ? 2 : 4);
                        if (idxByteOffset < 0 || idxByteOffset + (isIdx16 ? 2 : 4) > idxBuffer.limit()) continue;

                        int index;
                        if (isIdx16) {
                            index = idxBuffer.getShort(idxByteOffset) & 0xFFFF;
                        } else {
                            index = idxBuffer.getInt(idxByteOffset);
                        }
                        int base = (vtxOffset + index) * vtxStride;
                        if (base < 0 || base + vtxStride > vtxBuffer.limit()) continue;

                        float x = vtxBuffer.getFloat(base);
                        float y = vtxBuffer.getFloat(base + 4);
                        float u = vtxBuffer.getFloat(base + 8);
                        float v = vtxBuffer.getFloat(base + 12);
                        int color = vtxBuffer.getInt(base + 16);

                        int r = color & 0xFF;
                        int g = (color >> 8) & 0xFF;
                        int b = (color >> 16) & 0xFF;
                        int a = (color >> 24) & 0xFF;

                        // Log removed

                        // Order MUST match format: POSITION -> TEXTURE -> COLOR
                        bufferBuilder.vertex(x, y, 400.0f).texture(u, v).color(r, g, b, a);
                        anyVertices = true;
                    }

                    if (!anyVertices) {
                        BuiltBuffer empty = bufferBuilder.endNullable();
                        if (empty != null) empty.close();
                        continue;
                    }

                    BuiltBuffer builtBuffer = bufferBuilder.endNullable();
                    if (builtBuffer == null) continue;

                    try {
                        ByteBuffer vertexData = builtBuffer.getBuffer();
                        if (vkPipeline != null && rendererInst != null) {
                            try {
                                bindPipeline.invoke(rendererInst, vkPipeline);
                                
                                try {
                                    Class<?> vtsClass = Class.forName("net.vulkanmod.vulkan.texture.VTextureSelector");
                                    
                                    // Manually bind font texture to slot 0
                                    net.minecraft.client.texture.AbstractTexture tex = 
                                        net.minecraft.client.MinecraftClient.getInstance().getTextureManager().getTexture(fontTextureId);
                                    if (tex != null) {
                                        try {
                                            if (tex instanceof net.minecraft.client.texture.NativeImageBackedTexture) {
                                                ((net.minecraft.client.texture.NativeImageBackedTexture)tex).upload();
                                            }
                                            Object texView = tex.getClass().getMethod("getGlTextureView").invoke(tex);
                                            Class<?> vrsClass = Class.forName("net.vulkanmod.vulkan.VRenderSystem");
                                            vrsClass.getMethod("setShaderTexture", int.class, Class.forName("com.mojang.blaze3d.textures.GpuTextureView")).invoke(null, 0, texView);
                                            
                                            // Then tell VTextureSelector to update descriptor sets
                                            vtsClass.getMethod("bindShaderTextures", Class.forName("net.vulkanmod.vulkan.shader.Pipeline")).invoke(null, vkPipeline);
                                        } catch (Exception e) {}
                                    }
                                    Class<?> rendererClass = Class.forName("net.vulkanmod.vulkan.Renderer");
                                    Object commandBuffer = rendererClass.getMethod("getCommandBuffer").invoke(null);
                                    int currentFrame = (int) rendererClass.getMethod("getCurrentFrame").invoke(null);
                                    
                                    // Vulkan requires drawing to happen inside an active RenderPass.
                                    // We create one using Vanilla's API which VulkanMod implements and translates to vkCmdBeginRenderPass.
                                    Class<?> renderSystemClass = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
                                    Object device = renderSystemClass.getMethod("getDevice").invoke(null);
                                    Object commandEncoder = device.getClass().getMethod("createCommandEncoder").invoke(device);
                                    
                                    Object framebuffer = net.minecraft.client.MinecraftClient.getInstance().getFramebuffer();
                                    Object colorAttachmentView = framebuffer.getClass().getMethod("method_71639").invoke(framebuffer);
                                    Object depthAttachmentView = framebuffer.getClass().getMethod("method_71640").invoke(framebuffer);
                                    
                                    java.util.function.Supplier<String> nameSupplier = () -> "ImGui RenderPass";
                                    Object renderPass = commandEncoder.getClass().getMethod("createRenderPass", 
            ByteBuffer idxBuffer = drawData.getCmdListIdxBufferData(n).order(ByteOrder.nativeOrder());

            final int vtxStride = 20; // ImDrawVert: xy(8) + uv(8) + col(4)
            boolean isIdx16 = (imgui.moulberry90.ImDrawData.sizeOfImDrawIdx() == 2);

            for (int cmdIdx = 0; cmdIdx < drawData.getCmdListCmdBufferSize(n); cmdIdx++) {
                drawData.getCmdListCmdBufferClipRect(clipRect, n, cmdIdx);

                float clipMinX = clipRect.x - clipOffX;
                float clipMinY = clipRect.y - clipOffY;
                float clipMaxX = clipRect.z - clipOffX;
                float clipMaxY = clipRect.w - clipOffY;

                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) continue;

                int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);
                int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
                int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);

                // Resolve the texture identifier securely
                Identifier texIdentifier = Identifier.of("flashback", "font");
                Object textureIdObj = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
                if (textureIdObj instanceof Integer && (Integer) textureIdObj == 999999) {
                    texIdentifier = Identifier.of("flashback", "font");
                } else if (textureIdObj instanceof Identifier) {
                    texIdentifier = (Identifier) textureIdObj;
                }

                // Enable native scissoring mapped to Minecraft's GUI scaled expectations
                // Temporarily commented out for blackout sanity check!
                /*
                context.enableScissor(
                    (int) (clipMinX / scale),
                    (int) (clipMinY / scale),
                    (int) (clipMaxX / scale),
                    (int) (clipMaxY / scale)
                );
                */

                // Fetch a native VertexConsumer mapped to GUI_TEXTURED rendering layouts
                VertexConsumer consumer = context.getVertexConsumers().getBuffer(RenderLayer.getGuiTextured(texIdentifier));

                for (int i = 0; i < elemCount; i++) {
                    int idxByteOffset = (idxOffset + i) * (isIdx16 ? 2 : 4);
                    if (idxByteOffset < 0 || idxByteOffset + (isIdx16 ? 2 : 4) > idxBuffer.limit()) continue;

                    int index = isIdx16 ? (idxBuffer.getShort(idxByteOffset) & 0xFFFF) : idxBuffer.getInt(idxByteOffset);
                    int base = (vtxOffset + index) * vtxStride;
                    if (base < 0 || base + vtxStride > vtxBuffer.limit()) continue;

                    float x = vtxBuffer.getFloat(base);
                    float y = vtxBuffer.getFloat(base + 4);
                    float u = vtxBuffer.getFloat(base + 8);
                    float v = vtxBuffer.getFloat(base + 12);
                    int   r = vtxBuffer.get(base + 16) & 0xFF;
                    int   g = vtxBuffer.get(base + 17) & 0xFF;
                    int   b = vtxBuffer.get(base + 18) & 0xFF;
                    int   a = vtxBuffer.get(base + 19) & 0xFF;

                    // Stream directly into the consumer
                    consumer.vertex(matrix, x, y, 500.0f).texture(u, v).color(r, g, b, a);
                }

                // Flush this specific scissor batch immediately before moving to next command lists
                context.getVertexConsumers().draw();
                // context.disableScissor(); // Disabled for now along with enableScissor
            }
        }

        context.getMatrices().pop();
    }

    @Inject(method = "destroyDeviceObjects", at = @At("HEAD"), cancellable = true, remap = false)
    public void destroyDeviceObjects(CallbackInfo ci) { ci.cancel(); }

    @Inject(method = "destroyFontsTexture", at = @At("HEAD"), cancellable = true, remap = false)
    public void destroyFontsTexture(CallbackInfo ci) { ci.cancel(); }

    @Inject(method = "createDeviceObjects", at = @At("HEAD"), cancellable = true, remap = false)
    public void createDeviceObjects(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        try {
            java.lang.reflect.Field dataField = this.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            Object dataObj = dataField.get(this);
            if (dataObj != null) {
                java.lang.reflect.Field shaderField = dataObj.getClass().getDeclaredField("shaderHandle");
                shaderField.setAccessible(true);
                shaderField.set(dataObj, 1);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Inject(method = "updateFontsTexture", at = @At("HEAD"), cancellable = true, remap = false)
    public void updateFontsTexture(CallbackInfo ci) {
        ci.cancel();
        try {
            Class<?> replayUIClass = Class.forName("com.moulberry.flashback.editor.ui.ReplayUI");
            Object io = replayUIClass.getMethod("getIO").invoke(null);
            imgui.moulberry90.ImFontAtlas fonts =
                (imgui.moulberry90.ImFontAtlas) io.getClass().getMethod("getFonts").invoke(io);

            imgui.moulberry90.type.ImInt widthOut  = new imgui.moulberry90.type.ImInt();
            imgui.moulberry90.type.ImInt heightOut = new imgui.moulberry90.type.ImInt();
            ByteBuffer texData = fonts.getTexDataAsRGBA32(widthOut, heightOut);
            int w = widthOut.get();
            int h = heightOut.get();

            net.minecraft.client.texture.NativeImage img =
                new net.minecraft.client.texture.NativeImage(net.minecraft.client.texture.NativeImage.Format.RGBA, w, h, false);
            org.lwjgl.system.MemoryUtil.memCopy(
                org.lwjgl.system.MemoryUtil.memAddress(texData),
                img.imageId(),
                (long) texData.limit()
            );

            net.minecraft.client.texture.NativeImageBackedTexture tex =
                new net.minecraft.client.texture.NativeImageBackedTexture(() -> "flashback_font", img);
            Identifier id = Identifier.of("flashback", "font");
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

            fonts.setTexID(999999);

            java.lang.reflect.Field dataField = this.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            Object dataObj = dataField.get(this);
            if (dataObj != null) {
                try {
                    java.lang.reflect.Field fontTextureField = dataObj.getClass().getDeclaredField("fontTexture");
                    fontTextureField.setAccessible(true);
                    fontTextureField.set(dataObj, 999999);
                } catch (NoSuchFieldException ignored) {}
            }
            System.out.println("[VulkanReplayCompat] Font atlas registered to asset manager successfully.");
        } catch (Exception e) {
            System.err.println("[VulkanReplayCompat] updateFontsTexture failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
