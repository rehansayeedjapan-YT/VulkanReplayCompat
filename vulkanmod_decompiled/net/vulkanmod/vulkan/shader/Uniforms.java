/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
 *  net.minecraft.class_12393
 *  net.minecraft.class_310
 */
package net.vulkanmod.vulkan.shader;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.function.Supplier;
import net.minecraft.class_12393;
import net.minecraft.class_310;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.util.MappedBuffer;

public class Uniforms {
    public static Object2ReferenceOpenHashMap<String, Supplier<Integer>> vec1i_uniformMap = new Object2ReferenceOpenHashMap();
    public static Object2ReferenceOpenHashMap<String, Supplier<Float>> vec1f_uniformMap = new Object2ReferenceOpenHashMap();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec2f_uniformMap = new Object2ReferenceOpenHashMap();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec3f_uniformMap = new Object2ReferenceOpenHashMap();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec4f_uniformMap = new Object2ReferenceOpenHashMap();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> mat4f_uniformMap = new Object2ReferenceOpenHashMap();

    public static void setupDefaultUniforms() {
        mat4f_uniformMap.put((Object)"ModelViewMat", VRenderSystem::getModelViewMatrix);
        mat4f_uniformMap.put((Object)"ProjMat", VRenderSystem::getProjectionMatrix);
        mat4f_uniformMap.put((Object)"MVP", VRenderSystem::getMVP);
        mat4f_uniformMap.put((Object)"TextureMat", VRenderSystem::getTextureMatrix);
        vec1i_uniformMap.put((Object)"EndPortalLayers", () -> 15);
        vec1i_uniformMap.put((Object)"UseRgss", () -> class_310.method_1551().field_1690.method_76747().method_41753() == class_12393.field_64664 ? 1 : 0);
        vec1i_uniformMap.put((Object)"CurrentTime", VRenderSystem::getCurrentTime);
        vec1f_uniformMap.put((Object)"FogStart", () -> Float.valueOf(VRenderSystem.getFogData().field_60583));
        vec1f_uniformMap.put((Object)"FogEnd", () -> Float.valueOf(VRenderSystem.getFogData().field_60585));
        vec1f_uniformMap.put((Object)"FogEnvironmentalStart", () -> Float.valueOf(VRenderSystem.getFogData().field_60582));
        vec1f_uniformMap.put((Object)"FogEnvironmentalEnd", () -> Float.valueOf(VRenderSystem.getFogData().field_60584));
        vec1f_uniformMap.put((Object)"FogRenderDistanceStart", () -> Float.valueOf(VRenderSystem.getFogData().field_60583));
        vec1f_uniformMap.put((Object)"FogRenderDistanceEnd", () -> Float.valueOf(VRenderSystem.getFogData().field_60585));
        vec1f_uniformMap.put((Object)"FogSkyEnd", () -> Float.valueOf(VRenderSystem.getFogData().field_60099));
        vec1f_uniformMap.put((Object)"FogCloudsEnd", () -> Float.valueOf(VRenderSystem.getFogData().field_60100));
        vec1f_uniformMap.put((Object)"AlphaCutout", () -> Float.valueOf(VRenderSystem.alphaCutout));
        vec2f_uniformMap.put((Object)"ScreenSize", VRenderSystem::getScreenSize);
        vec2f_uniformMap.put((Object)"TextureSize", VRenderSystem::getTextureSize);
        vec2f_uniformMap.put((Object)"TexelSize", VRenderSystem::getTexelSize);
        vec3f_uniformMap.put((Object)"Light0_Direction", () -> VRenderSystem.lightDirection0);
        vec3f_uniformMap.put((Object)"Light1_Direction", () -> VRenderSystem.lightDirection1);
        vec3f_uniformMap.put((Object)"ModelOffset", () -> VRenderSystem.modelOffset);
        vec3f_uniformMap.put((Object)"ChunkOffset", () -> VRenderSystem.modelOffset);
        vec4f_uniformMap.put((Object)"ColorModulator", VRenderSystem::getShaderColor);
        vec4f_uniformMap.put((Object)"FogColor", VRenderSystem::getShaderFogColor);
    }

    public static Supplier<MappedBuffer> getUniformSupplier(String type, String name) {
        return switch (type) {
            case "mat4" -> (Supplier)mat4f_uniformMap.get((Object)name);
            case "vec4" -> (Supplier)vec4f_uniformMap.get((Object)name);
            case "vec3" -> (Supplier)vec3f_uniformMap.get((Object)name);
            case "vec2", "ivec2" -> (Supplier)vec2f_uniformMap.get((Object)name);
            default -> null;
        };
    }
}

