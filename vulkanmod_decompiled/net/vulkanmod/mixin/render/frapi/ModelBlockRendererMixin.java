/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer
 *  net.minecraft.class_1087
 *  net.minecraft.class_1920
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_778
 *  net.minecraft.class_9891
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.render.frapi;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.minecraft.class_1087;
import net.minecraft.class_1920;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_778;
import net.minecraft.class_9891;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={class_778.class})
abstract class ModelBlockRendererMixin {
    ModelBlockRendererMixin() {
    }

    @Overwrite
    public static void method_3367(class_4587.class_4665 entry, class_4588 vertexConsumer, class_1087 model, float red, float green, float blue, int light, int overlay) {
        FabricBlockModelRenderer.render((class_4587.class_4665)entry, layer -> vertexConsumer, (class_1087)model, (float)red, (float)green, (float)blue, (int)light, (int)overlay, (class_1920)class_9891.field_52611, (class_2338)class_2338.field_10980, (class_2680)class_2246.field_10124.method_9564());
    }
}

