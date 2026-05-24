/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_276
 *  net.minecraft.class_318
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.util;

import java.util.function.Consumer;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import net.minecraft.class_318;
import net.vulkanmod.vulkan.util.ScreenshotUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={class_318.class})
public class ScreenshotMixin {
    @Overwrite
    public static void method_71641(class_276 renderTarget, int mipLevel, Consumer<class_1011> consumer) {
        ScreenshotUtil.takeScreenshot(renderTarget, mipLevel, consumer);
    }
}

