/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.shader.descriptor;

import net.vulkanmod.vulkan.shader.descriptor.UBO;
import org.lwjgl.system.MemoryUtil;

public class ManualUBO
extends UBO {
    private long srcPtr;
    private int srcSize;
    private boolean update = true;

    public ManualUBO(int binding, int type, int size) {
        super("manual UBO: %d".formatted(binding), binding, type, size * 4, null);
    }

    @Override
    public void update(long ptr) {
        if (this.update) {
            MemoryUtil.memCopy((long)this.srcPtr, (long)ptr, (long)this.srcSize);
        }
    }

    public void setSrc(long ptr, int size) {
        this.srcPtr = ptr;
        this.srcSize = size;
    }

    @Override
    public void setUpdate(boolean update) {
        this.update = update;
    }
}

