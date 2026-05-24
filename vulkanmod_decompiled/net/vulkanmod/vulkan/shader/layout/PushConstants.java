/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.layout;

import java.util.List;
import net.vulkanmod.vulkan.shader.layout.AlignedStruct;
import net.vulkanmod.vulkan.shader.layout.Uniform;

public class PushConstants
extends AlignedStruct {
    protected PushConstants(List<Uniform.Info> infoList, int size) {
        super(infoList, size);
    }
}

