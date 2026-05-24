/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4543
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render.biome;

import net.minecraft.class_4543;
import net.vulkanmod.interfaces.biome.BiomeManagerExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_4543.class})
public class BiomeManagerM
implements BiomeManagerExtended {
    @Shadow
    @Final
    private long field_20641;

    @Override
    public long getBiomeZoomSeed() {
        return this.field_20641;
    }
}

