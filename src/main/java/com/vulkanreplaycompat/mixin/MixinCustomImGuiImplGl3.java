package com.vulkanreplaycompat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import imgui.moulberry90.ImDrawData;
import imgui.moulberry90.ImVec4;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(targets = "com.moulberry.flashback.editor.ui.CustomImGuiImplGl3")
public class MixinCustomImGuiImplGl3 {

    @Inject(method = "renderDrawData", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderDrawData(ImDrawData drawData, CallbackInfo ci) {
        ci.cancel();

        if (System.currentTimeMillis() % 1000 < 50) {
            System.out.println("[VulkanReplayCompat] HUD Hook Fired! CmdLists: " + drawData.getCmdListsCount());
        }

        if (drawData.getCmdListsCount() <= 0) {
            return;
        }

        DrawContext context = com.vulkanreplaycompat.BridgeState.currentContext;
        if (context == null) {
            return; // Safety fallback if context wasn't captured
        }

        int fbWidth  = (int) drawData.getDisplaySizeX();
        int fbHeight = (int) drawData.getDisplaySizeY();
        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        float clipOffX = drawData.getDisplayPosX();
        float clipOffY = drawData.getDisplayPosY();
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();

        context.getMatrices().push();
        context.getMatrices().scale(1.0f / scale, 1.0f / scale, 1.0f);
        
        org.joml.Matrix4f m = context.getMatrices().peek().getPositionMatrix();

        ImVec4 clipRect = new ImVec4();
        
        RenderSystem.setShader(net.minecraft.client.render.GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int n = 0; n < drawData.getCmdListsCount(); n++) {
            ByteBuffer rawVtx = drawData.getCmdListVtxBufferData(n);
            rawVtx.rewind();
            ByteBuffer vtxBuffer = ByteBuffer.allocateDirect(rawVtx.remaining()).order(ByteOrder.nativeOrder());
            vtxBuffer.put(rawVtx);
            vtxBuffer.flip();
            
            ByteBuffer idxBuffer = drawData.getCmdListIdxBufferData(n).order(ByteOrder.nativeOrder());

            final int vtxStride = 20; 
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

                Identifier texIdentifier = Identifier.of("flashback", "font");
                Object textureIdObj = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
                if (textureIdObj instanceof Identifier) {
                    texIdentifier = (Identifier) textureIdObj;
                }

                RenderSystem.setShaderTexture(0, texIdentifier);

                net.minecraft.client.render.Tessellator tessellator = net.minecraft.client.render.Tessellator.getInstance();
                net.minecraft.client.render.BufferBuilder bufferBuilder = tessellator.begin(net.minecraft.client.render.VertexFormat.DrawMode.TRIANGLES, net.minecraft.client.render.VertexFormats.POSITION_TEXTURE_COLOR);

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

                    float x1 = x * m.m00() + y * m.m10() + m.m30();
                    float y1 = x * m.m01() + y * m.m11() + m.m31();

                    bufferBuilder.vertex(x1, y1, 500.0f).texture(u, v).color(r, g, b, a);
                }

                net.minecraft.client.render.BuiltBuffer builtBuffer = bufferBuilder.endNullable();
                if (builtBuffer != null) {
                    net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(builtBuffer);
                }
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
            imgui.moulberry90.ImFontAtlas fonts = (imgui.moulberry90.ImFontAtlas) io.getClass().getMethod("getFonts").invoke(io);

            imgui.moulberry90.type.ImInt widthOut  = new imgui.moulberry90.type.ImInt();
            imgui.moulberry90.type.ImInt heightOut = new imgui.moulberry90.type.ImInt();
            ByteBuffer texData = fonts.getTexDataAsRGBA32(widthOut, heightOut);
            int w = widthOut.get();
            int h = heightOut.get();

            net.minecraft.client.texture.NativeImage img = new net.minecraft.client.texture.NativeImage(net.minecraft.client.texture.NativeImage.Format.RGBA, w, h, false);
            texData.rewind();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int r = texData.get() & 0xFF;
                    int g = texData.get() & 0xFF;
                    int b = texData.get() & 0xFF;
                    int a = texData.get() & 0xFF;
                    img.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }

            net.minecraft.client.texture.NativeImageBackedTexture tex = new net.minecraft.client.texture.NativeImageBackedTexture(img);
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
