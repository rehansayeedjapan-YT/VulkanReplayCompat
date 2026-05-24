/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkSamplerCreateInfo
 *  org.lwjgl.vulkan.VkSamplerReductionModeCreateInfo
 */
package net.vulkanmod.vulkan.texture;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.nio.LongBuffer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSamplerReductionModeCreateInfo;

public abstract class SamplerManager {
    public static final int ADDRESS_MODE_BITS = 2;
    public static final int REDUCTION_MODE_BITS = 2;
    public static final int ADDRESS_MODE_U_OFFSET = 0;
    public static final int ADDRESS_MODE_V_OFFSET = 2;
    public static final int MIN_FILTER_OFFSET = 4;
    public static final int MAG_FILTER_OFFSET = 5;
    public static final int MIPMAP_MODE_OFFSET = 6;
    public static final int ANISOTROPY_OFFSET = 7;
    public static final int REDUCTION_MODE_ENABLE_OFFSET = 8;
    public static final int REDUCTION_MODE_OFFSET = 9;
    static final float MIP_BIAS = -0.5f;
    static final Object2LongMap<SamplerInfo> SAMPLERS = new Object2LongOpenHashMap();

    public static long getSampler(boolean clamp, boolean linearFiltering, int maxLod) {
        return SamplerManager.getSampler(clamp, linearFiltering, maxLod, false, 0);
    }

    public static long getSampler(boolean clamp, boolean linearFiltering, int maxLod, boolean anisotropy, int maxAnisotropy) {
        int addressMode = clamp ? 2 : 0;
        int filter = linearFiltering ? 1 : 0;
        int mipmapMode = linearFiltering ? 1 : 0;
        return SamplerManager.getSampler(addressMode, addressMode, filter, filter, mipmapMode, maxLod, anisotropy, maxAnisotropy, -1);
    }

    public static long getSampler(int addressModeU, int addressModeV, int minFilter, int magFilter, int mipmapMode, float maxLod, boolean anisotropy, float maxAnisotropy, int reductionMode) {
        SamplerInfo samplerInfo = new SamplerInfo(addressModeU, addressModeV, minFilter, magFilter, mipmapMode, maxLod, anisotropy, maxAnisotropy, reductionMode);
        long sampler = SAMPLERS.getOrDefault((Object)samplerInfo, 0L);
        if (sampler == 0L) {
            sampler = SamplerManager.createTextureSampler(samplerInfo);
            SAMPLERS.put((Object)samplerInfo, sampler);
        }
        return sampler;
    }

    public static long getDefaultSampler() {
        return SamplerManager.getSampler(false, false, 0);
    }

    private static long createTextureSampler(SamplerInfo sampler) {
        int state = sampler.encodedState;
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc((MemoryStack)stack);
            samplerInfo.sType(31);
            samplerInfo.magFilter(sampler.getMagFilter());
            samplerInfo.minFilter(sampler.getMinFilter());
            samplerInfo.addressModeU(sampler.getAddressModeU());
            samplerInfo.addressModeV(sampler.getAddressModeV());
            samplerInfo.addressModeW(0);
            samplerInfo.anisotropyEnable(sampler.getAnisotropy());
            samplerInfo.maxAnisotropy((float)sampler.getMaxAnisotropy());
            samplerInfo.borderColor(5);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(7);
            samplerInfo.mipmapMode(sampler.getMipmapMode());
            samplerInfo.maxLod((float)sampler.getMaxLod());
            samplerInfo.minLod(0.0f);
            samplerInfo.mipLodBias(0.0f);
            if (sampler.hasReductionMode()) {
                VkSamplerReductionModeCreateInfo reductionModeInfo = VkSamplerReductionModeCreateInfo.calloc((MemoryStack)stack);
                reductionModeInfo.sType$Default();
                reductionModeInfo.reductionMode(sampler.getReductionMode());
                samplerInfo.pNext(reductionModeInfo.address());
            }
            LongBuffer pTextureSampler = stack.mallocLong(1);
            if (VK10.vkCreateSampler((VkDevice)Vulkan.getVkDevice(), (VkSamplerCreateInfo)samplerInfo, null, (LongBuffer)pTextureSampler) != 0) {
                throw new RuntimeException("Failed to create texture sampler");
            }
            long l = pTextureSampler.get(0);
            return l;
        }
    }

    public static void cleanUp() {
        LongIterator longIterator = SAMPLERS.values().iterator();
        while (longIterator.hasNext()) {
            long id = (Long)longIterator.next();
            VK10.vkDestroySampler((VkDevice)DeviceManager.vkDevice, (long)id, null);
        }
    }

    static int getEncodedState(int addressModeU, int addressModeV, int minFilter, int magFilter, int mipmapMode, boolean anisotropy, int reductionMode) {
        int encodedState = (addressModeU & 2) << 0;
        encodedState |= (addressModeV & 2) << 2;
        encodedState |= (minFilter & 1) << 4;
        encodedState |= (magFilter & 1) << 5;
        encodedState |= (mipmapMode & 1) << 6;
        encodedState |= ((anisotropy ? 1 : 0) & 1) << 7;
        encodedState |= (reductionMode != -1 ? 1 : 0) << 8;
        return encodedState |= (reductionMode & 2) << 9;
    }

    public static class SamplerInfo {
        final int encodedState;
        final int maxLod;
        final int maxAnisotropy;

        public SamplerInfo() {
            this(0, 0, 0, 0, 0, 0.0f, false, 0.0f, -1);
        }

        public SamplerInfo(int addressModeU, int addressModeV, int minFilter, int magFilter, int mipmapMode, float maxLod, boolean anisotropy, float maxAnisotropy, int reductionMode) {
            this.maxLod = (int)maxLod;
            this.maxAnisotropy = (int)maxAnisotropy;
            this.encodedState = SamplerManager.getEncodedState(addressModeU, addressModeV, minFilter, magFilter, mipmapMode, anisotropy, reductionMode);
        }

        public int getAddressModeU() {
            return this.encodedState >> 0 & 2;
        }

        public int getAddressModeV() {
            return this.encodedState >> 2 & 2;
        }

        public int getMinFilter() {
            return this.encodedState >> 4 & 1;
        }

        public int getMagFilter() {
            return this.encodedState >> 5 & 1;
        }

        public int getMipmapMode() {
            return this.encodedState >> 6 & 1;
        }

        public boolean getAnisotropy() {
            return (this.encodedState >> 7 & 1) != 0;
        }

        public boolean hasReductionMode() {
            return (this.encodedState >> 8 & 1) != 0;
        }

        public int getReductionMode() {
            return this.encodedState >> 9 & 2;
        }

        public int getMaxAnisotropy() {
            return this.maxAnisotropy;
        }

        public int getMaxLod() {
            return this.maxLod;
        }

        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            SamplerInfo samplerInfo = (SamplerInfo)o;
            return this.maxLod == samplerInfo.maxLod && this.maxAnisotropy == samplerInfo.maxAnisotropy && this.encodedState == samplerInfo.encodedState;
        }

        public int hashCode() {
            int result = this.encodedState;
            result = 31 * result + this.maxLod;
            result = 31 * result + this.maxAnisotropy;
            return result;
        }
    }
}

