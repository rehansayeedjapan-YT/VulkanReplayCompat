/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11515
 *  net.minecraft.class_1921
 */
package net.vulkanmod.render.vertex;

import java.util.EnumSet;
import java.util.function.Function;
import net.minecraft.class_11515;
import net.minecraft.class_1921;
import net.vulkanmod.Initializer;
import net.vulkanmod.interfaces.ExtendedRenderType;
import net.vulkanmod.vulkan.VRenderSystem;

public enum TerrainRenderType {
    SOLID(0.0f),
    CUTOUT(0.5f),
    TRANSLUCENT(0.01f),
    TRIPWIRE(0.1f);

    public static final TerrainRenderType[] VALUES;
    public static final EnumSet<TerrainRenderType> COMPACT_RENDER_TYPES;
    public static final EnumSet<TerrainRenderType> SEMI_COMPACT_RENDER_TYPES;
    private static Function<TerrainRenderType, TerrainRenderType> remapper;
    public final float alphaCutout;

    private TerrainRenderType(float alphaCutout) {
        this.alphaCutout = alphaCutout;
    }

    public void setCutoutUniform() {
        VRenderSystem.alphaCutout = this.alphaCutout;
    }

    public static TerrainRenderType get(class_1921 renderType) {
        return ((ExtendedRenderType)renderType).getTerrainRenderType();
    }

    public static TerrainRenderType get(class_11515 layer) {
        return switch (layer) {
            default -> throw new MatchException(null, null);
            case class_11515.field_60923 -> SOLID;
            case class_11515.field_60925 -> CUTOUT;
            case class_11515.field_60926 -> TRANSLUCENT;
            case class_11515.field_60927 -> TRIPWIRE;
        };
    }

    public static TerrainRenderType get(String name) {
        return switch (name) {
            case "solid" -> SOLID;
            case "cutout" -> CUTOUT;
            case "translucent" -> TRANSLUCENT;
            case "tripwire" -> TRIPWIRE;
            default -> null;
        };
    }

    public static class_11515 getLayer(TerrainRenderType renderType) {
        return switch (renderType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_11515.field_60923;
            case 1 -> class_11515.field_60925;
            case 2 -> class_11515.field_60926;
            case 3 -> class_11515.field_60927;
        };
    }

    public static void updateMapping() {
        remapper = Initializer.CONFIG.uniqueOpaqueLayer ? renderType -> switch (renderType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> CUTOUT;
            case 2, 3 -> TRANSLUCENT;
        } : renderType -> switch (renderType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> SOLID;
            case 1 -> CUTOUT;
            case 2, 3 -> TRANSLUCENT;
        };
    }

    public static TerrainRenderType getRemapped(TerrainRenderType renderType) {
        return remapper.apply(renderType);
    }

    static {
        VALUES = TerrainRenderType.values();
        COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT, TRANSLUCENT);
        SEMI_COMPACT_RENDER_TYPES = EnumSet.of(SOLID, CUTOUT, TRANSLUCENT);
        SEMI_COMPACT_RENDER_TYPES.add(CUTOUT);
        SEMI_COMPACT_RENDER_TYPES.add(TRANSLUCENT);
        COMPACT_RENDER_TYPES.add(TRANSLUCENT);
    }
}

