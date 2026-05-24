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

public class Vec1f
extends Uniform {
    private Supplier<Float> floatSupplier;

    public Vec1f(Uniform.Info info) {
        super(info);
    }

    @Override
    protected void setupSupplier() {
        if (this.info.floatSupplier != null) {
            this.floatSupplier = this.info.floatSupplier;
        } else {
            this.setSupplier(this.info.bufferSupplier);
        }
    }

    @Override
    public void setSupplier(Supplier<MappedBuffer> supplier) {
        this.floatSupplier = () -> Float.valueOf(((MappedBuffer)supplier.get()).getFloat(0));
    }

    @Override
    void update(long ptr) {
        float f = this.floatSupplier.get().floatValue();
        MemoryUtil.memPutFloat((long)(ptr + this.offset), (float)f);
    }
}

