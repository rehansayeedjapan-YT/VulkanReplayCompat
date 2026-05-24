/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10141
 *  net.minecraft.class_10869$class_10872
 *  net.minecraft.class_5944
 *  org.slf4j.Logger
 */
package net.vulkanmod.render.engine;

import com.mojang.logging.LogUtils;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10141;
import net.minecraft.class_10869;
import net.minecraft.class_5944;
import net.vulkanmod.render.engine.VkGpuBuffer;
import net.vulkanmod.render.engine.VkGpuTexture;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class VkDebugLabel {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void applyLabel(VkGpuBuffer glBuffer) {
    }

    public void applyLabel(VkGpuTexture glTexture) {
    }

    public void applyLabel(class_10141 glShaderModule) {
    }

    public void applyLabel(class_5944 glProgram) {
    }

    public void applyLabel(class_10869.class_10872 vertexArray) {
    }

    public static VkDebugLabel create(boolean bl, Set<String> set) {
        return new VkDebugLabel();
    }

    public boolean exists() {
        return true;
    }
}

