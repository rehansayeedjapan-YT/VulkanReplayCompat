/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.AddressMode
 *  com.mojang.blaze3d.textures.FilterMode
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12137
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12137;
import net.vulkanmod.render.engine.VkConst;
import net.vulkanmod.vulkan.texture.SamplerManager;

@Environment(value=EnvType.CLIENT)
public class VkSampler
extends class_12137 {
    private final AddressMode addressModeU;
    private final AddressMode addressModeV;
    private final FilterMode minFilter;
    private final FilterMode magFilter;
    private final int maxAnisotropy;
    private final float maxLod;
    private boolean closed;
    private final long id;

    public VkSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        this.addressModeU = addressModeU;
        this.addressModeV = addressModeV;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.maxAnisotropy = maxAnisotropy;
        this.maxLod = maxLod.isPresent() ? (float)((byte)maxLod.getAsDouble()) : 1000.0f;
        this.id = SamplerManager.getSampler(VkConst.of(addressModeU), VkConst.of(addressModeV), VkConst.of(minFilter), VkConst.of(magFilter), 1, this.maxLod, maxAnisotropy > 1, maxAnisotropy, -1);
    }

    public long getId() {
        return this.id;
    }

    public AddressMode method_75286() {
        return this.addressModeU;
    }

    public AddressMode method_75287() {
        return this.addressModeV;
    }

    public FilterMode method_75288() {
        return this.minFilter;
    }

    public FilterMode method_75289() {
        return this.magFilter;
    }

    public int method_76230() {
        return this.maxAnisotropy;
    }

    public OptionalDouble method_76519() {
        return null;
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
        }
    }

    public boolean isClosed() {
        return this.closed;
    }
}

