/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_12245
 *  net.minecraft.class_12246
 *  net.minecraft.class_12247
 *  net.minecraft.class_12250
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_12245;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_12250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_12247.class})
public interface RenderSetupAccessor {
    @Accessor(value="field_63986")
    public RenderPipeline pipeline();

    @Accessor(value="field_63996")
    public class_12245 layeringTransform();

    @Accessor(value="field_63989")
    public class_12246 outputTarget();

    @Accessor(value="field_63988")
    public class_12250 textureTransform();
}

