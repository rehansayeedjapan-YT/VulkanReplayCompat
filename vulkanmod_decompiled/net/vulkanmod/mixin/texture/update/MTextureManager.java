/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1060
 *  net.minecraft.class_1061
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.texture.update;

import java.util.Set;
import net.minecraft.class_1060;
import net.minecraft.class_1061;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_1060.class})
public abstract class MTextureManager {
    @Shadow
    @Final
    private Set<class_1061> field_5284;

    @Overwrite
    public void method_76322() {
        if (!Initializer.CONFIG.textureAnimations) {
            return;
        }
        for (class_1061 tickable : this.field_5284) {
            tickable.method_4622();
        }
        SpriteUpdateUtil.transitionLayouts();
    }
}

