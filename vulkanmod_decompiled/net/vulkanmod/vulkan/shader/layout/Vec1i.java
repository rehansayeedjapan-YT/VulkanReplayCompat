/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.shader.layout;

import java.util.function.Supplier;
import net.vulkanmod.vulkan.shader.layout.Uniform;
import net.vulkanmod.vulkan.util.MappedBuffer;
import org.lwjgl.system.MemoryUtil;

public class Vec1i
extends Uniform {
    private Supplier<Integer> intSupplier;

    public Vec1i(Uniform.Info info) {
        super(info);
    }

    @Override
    protected void setupSupplier() {
        if (this.info.intSupplier != null) {
            this.intSupplier = this.info.intSupplier;
        } else {
            this.setSupplier(this.info.bufferSupplier);
        }
    }

    @Override
    public void setSupplier(Supplier<MappedBuffer> supplier) {
        this.intSupplier = () -> ((MappedBuffer)supplier.get()).getInt(0);
    }

    @Override
    void update(long ptr) {
        int i = this.intSupplier.get();
        MemoryUtil.memPutInt((long)(ptr + this.offset), (int)i);
    }
}

