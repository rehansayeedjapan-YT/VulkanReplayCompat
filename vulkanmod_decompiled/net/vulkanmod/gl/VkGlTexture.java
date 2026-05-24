/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.gl;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.vulkanmod.Initializer;
import net.vulkanmod.gl.GlUtil;
import net.vulkanmod.gl.VkGlBuffer;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class VkGlTexture {
    private static int ID_COUNTER = 1;
    private static final Int2ReferenceOpenHashMap<VkGlTexture> map = new Int2ReferenceOpenHashMap();
    private static int boundTextureId = 0;
    private static VkGlTexture boundTexture;
    private static int activeTexture;
    private static int unpackRowLength;
    private static int unpackSkipRows;
    private static int unpackSkipPixels;
    public final int id;
    VulkanImage vulkanImage;
    int width;
    int height;
    int vkFormat;
    boolean needsUpdate = false;
    int maxLevel = 0;
    int maxLod = 0;
    int minFilter = 9728;
    int magFilter = 9728;
    boolean clamp = true;

    public static void bindIdToImage(int id, VulkanImage vulkanImage) {
        VkGlTexture texture = (VkGlTexture)map.get(id);
        texture.vulkanImage = vulkanImage;
    }

    public static int genTextureId() {
        int id = ID_COUNTER++;
        map.put(id, (Object)new VkGlTexture(id));
        return id;
    }

    public static void bindTexture(int id) {
        boundTextureId = id;
        boundTexture = (VkGlTexture)map.get(id);
        if (id <= 0) {
            return;
        }
        if (boundTexture == null) {
            Initializer.LOGGER.error("Invalid id({}) value", (Object)id);
            return;
        }
        VulkanImage vulkanImage = VkGlTexture.boundTexture.vulkanImage;
        if (vulkanImage != null) {
            VTextureSelector.bindTexture(activeTexture, vulkanImage);
        }
    }

    public static void glDeleteTextures(IntBuffer intBuffer) {
        for (int i = intBuffer.position(); i < intBuffer.limit(); ++i) {
            VkGlTexture.glDeleteTextures(intBuffer.get(i));
        }
    }

    public static void glDeleteTextures(int i) {
        VulkanImage image;
        VkGlTexture glTexture = (VkGlTexture)map.remove(i);
        VulkanImage vulkanImage = image = glTexture != null ? glTexture.vulkanImage : null;
        if (image != null) {
            MemoryManager.getInstance().addToFreeable(image);
        }
    }

    public static VkGlTexture getTexture(int id) {
        if (id == 0) {
            return null;
        }
        return (VkGlTexture)map.get(id);
    }

    public static void activeTexture(int i) {
        activeTexture = i - 33984;
        VTextureSelector.setActiveTexture(activeTexture);
    }

    public static void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, long pixels) {
        if (VkGlTexture.checkParams(level, width, height)) {
            return;
        }
        boundTexture.updateParams(level, width, height, internalFormat, type);
        boundTexture.allocateIfNeeded();
        VTextureSelector.bindTexture(activeTexture, VkGlTexture.boundTexture.vulkanImage);
        VkGlTexture.texSubImage2D(target, level, 0, 0, width, height, format, type, pixels);
    }

    public static void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
        if (VkGlTexture.checkParams(level, width, height)) {
            return;
        }
        boundTexture.updateParams(level, width, height, internalFormat, type);
        boundTexture.allocateIfNeeded();
        VTextureSelector.bindTexture(activeTexture, VkGlTexture.boundTexture.vulkanImage);
        VkGlTexture.texSubImage2D(target, level, 0, 0, width, height, format, type, pixels);
    }

    private static boolean checkParams(int level, int width, int height) {
        return width == 0 || height == 0;
    }

    public static void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, long pixels) {
        ByteBuffer src;
        if (width == 0 || height == 0) {
            return;
        }
        VkGlBuffer glBuffer = VkGlBuffer.getPixelUnpackBufferBound();
        if (glBuffer != null) {
            glBuffer.data.position((int)pixels);
            src = glBuffer.data;
        } else {
            src = pixels != 0L ? VkGlTexture.getByteBuffer(width, height, pixels) : null;
        }
        if (src != null) {
            boundTexture.uploadSubImage(level, xOffset, yOffset, width, height, format, src);
        }
    }

    private static ByteBuffer getByteBuffer(int width, int height, long pixels) {
        int formatSize = 4;
        int rowLength = unpackRowLength != 0 ? unpackRowLength : width;
        int offset = (unpackSkipRows * rowLength + unpackSkipPixels) * formatSize;
        ByteBuffer src = MemoryUtil.memByteBuffer((long)(pixels + (long)offset), (int)((rowLength * height - unpackSkipPixels) * formatSize));
        return src;
    }

    public static void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, @Nullable ByteBuffer pixels) {
        ByteBuffer src;
        if (width == 0 || height == 0) {
            return;
        }
        VkGlBuffer glBuffer = VkGlBuffer.getPixelUnpackBufferBound();
        if (glBuffer != null) {
            if (pixels != null) {
                throw new IllegalStateException("Trying to use pixel buffer when there is a Pixel Unpack Buffer bound.");
            }
            glBuffer.data.position(0);
            src = glBuffer.data;
        } else {
            src = pixels;
        }
        if (src != null) {
            boundTexture.uploadSubImage(level, xOffset, yOffset, width, height, format, src);
        }
    }

    public static void texParameteri(int target, int pName, int param) {
        if (target != 3553) {
            throw new UnsupportedOperationException("target != GL_TEXTURE_2D not supported");
        }
        if (boundTexture == null) {
            return;
        }
        switch (pName) {
            case 33085: {
                boundTexture.setMaxLevel(param);
                break;
            }
            case 33083: {
                boundTexture.setMaxLod(param);
                break;
            }
            case 33082: {
                break;
            }
            case 34049: {
                break;
            }
            case 10240: {
                boundTexture.setMagFilter(param);
                break;
            }
            case 10241: {
                boundTexture.setMinFilter(param);
                break;
            }
            case 10242: 
            case 10243: {
                boundTexture.setClamp(param);
                break;
            }
        }
    }

    public static int getTexParameteri(int target, int pName) {
        if (target != 3553) {
            throw new UnsupportedOperationException("target != GL_TEXTURE_2D not supported");
        }
        if (boundTexture == null) {
            return -1;
        }
        return switch (pName) {
            case 4099 -> GlUtil.getGlFormat(VkGlTexture.boundTexture.vulkanImage.format);
            case 4096 -> VkGlTexture.boundTexture.vulkanImage.width;
            case 4097 -> VkGlTexture.boundTexture.vulkanImage.height;
            case 33085 -> VkGlTexture.boundTexture.maxLevel;
            case 33083 -> VkGlTexture.boundTexture.maxLod;
            case 10240 -> VkGlTexture.boundTexture.magFilter;
            case 10241 -> VkGlTexture.boundTexture.minFilter;
            default -> -1;
        };
    }

    public static int getTexLevelParameter(int target, int level, int pName) {
        if (target != 3553) {
            throw new UnsupportedOperationException("target != GL_TEXTURE_2D not supported");
        }
        if (boundTexture == null) {
            return -1;
        }
        return switch (pName) {
            case 4099 -> GlUtil.getGlFormat(VkGlTexture.boundTexture.vulkanImage.format);
            case 4096 -> VkGlTexture.boundTexture.vulkanImage.width;
            case 4097 -> VkGlTexture.boundTexture.vulkanImage.height;
            default -> -1;
        };
    }

    public static void pixelStoreI(int pName, int value) {
        switch (pName) {
            case 3314: {
                unpackRowLength = value;
                break;
            }
            case 3315: {
                unpackSkipRows = value;
                break;
            }
            case 3316: {
                unpackSkipPixels = value;
            }
        }
    }

    public static void generateMipmap(int target) {
        if (target != 3553) {
            throw new UnsupportedOperationException("target != GL_TEXTURE_2D not supported");
        }
    }

    public static void getTexImage(int tex, int level, int format, int type, long pixels) {
        long ptr;
        VulkanImage image = VkGlTexture.boundTexture.vulkanImage;
        VkGlBuffer buffer = VkGlBuffer.getPixelPackBufferBound();
        if (buffer != null) {
            buffer.data.position((int)pixels);
            ptr = MemoryUtil.memAddress((ByteBuffer)buffer.data);
        } else {
            ptr = pixels;
        }
        ImageUtil.downloadTexture(image, ptr);
    }

    public static void setVulkanImage(int id, VulkanImage vulkanImage) {
        VkGlTexture texture = (VkGlTexture)map.get(id);
        texture.vulkanImage = vulkanImage;
    }

    public static VkGlTexture getBoundTexture() {
        return boundTexture;
    }

    public VkGlTexture(int id) {
        this.id = id;
    }

    void updateParams(int level, int width, int height, int internalFormat, int type) {
        if (level > this.maxLevel) {
            this.maxLevel = level;
            this.needsUpdate = true;
        }
        if (level == 0) {
            int vkFormat = GlUtil.vulkanFormat(internalFormat, type);
            if (this.vulkanImage == null || this.width != width || this.height != height || vkFormat != this.vulkanImage.format) {
                this.width = width;
                this.height = height;
                this.vkFormat = vkFormat;
                this.needsUpdate = true;
            }
        }
    }

    void allocateIfNeeded() {
        if (this.needsUpdate) {
            this.allocateImage(this.width, this.height, this.vkFormat);
            this.updateSampler();
            this.needsUpdate = false;
        }
    }

    void allocateImage(int width, int height, int vkFormat) {
        if (this.vulkanImage != null) {
            this.vulkanImage.free();
        }
        this.vulkanImage = VulkanImage.isDepthFormat(vkFormat) ? VulkanImage.createDepthImage(vkFormat, width, height, 37, false, true) : new VulkanImage.Builder(width, height).setName(String.format("GlTexture %d", this.id)).setMipLevels(this.maxLevel + 1).setFormat(vkFormat).addUsage(16).createVulkanImage();
    }

    void updateSampler() {
        int vkMinFilter;
        if (this.vulkanImage == null) {
            return;
        }
        int addressMode = this.clamp ? 2 : 0;
        int vkMagFilter = switch (this.magFilter) {
            case 9729 -> 1;
            case 9728 -> 0;
            default -> throw new IllegalStateException("Unexpected mag filter value: %d".formatted(this.magFilter));
        };
        long sampler = SamplerManager.getSampler(addressMode, addressMode, vkMinFilter, vkMagFilter, mipmapMode, this.maxLod, false, 0.0f, -1);
        this.vulkanImage.setSampler(sampler);
    }

    private void uploadSubImage(int level, int xOffset, int yOffset, int width, int height, int format, ByteBuffer pixels) {
        ByteBuffer src = format == 6407 && this.vulkanImage.format == 37 ? GlUtil.RGBtoRGBA_buffer(pixels) : (format == 32993 && this.vulkanImage.format == 37 ? GlUtil.BGRAtoRGBA_buffer(pixels) : pixels);
        this.vulkanImage.uploadSubTextureAsync(level, width, height, xOffset, yOffset, 0, 0, unpackRowLength, src);
        if (src != pixels) {
            MemoryUtil.memFree((Buffer)src);
        }
    }

    void generateMipmaps() {
        ImageUtil.generateMipmaps(this.vulkanImage);
    }

    void setMaxLevel(int l) {
        if (l < 0) {
            throw new IllegalStateException("max level cannot be < 0.");
        }
        if (this.maxLevel != l) {
            this.maxLevel = l;
            this.needsUpdate = true;
        }
    }

    void setMaxLod(int l) {
        if (l < 0) {
            throw new IllegalStateException("max level cannot be < 0.");
        }
        if (this.maxLod != l) {
            this.maxLod = l;
            this.updateSampler();
        }
    }

    void setMagFilter(int v) {
        switch (v) {
            case 9728: 
            case 9729: {
                break;
            }
            default: {
                throw new IllegalArgumentException("illegal mag filter value: " + v);
            }
        }
        this.magFilter = v;
        this.updateSampler();
    }

    void setMinFilter(int v) {
        switch (v) {
            case 9728: 
            case 9729: 
            case 9984: 
            case 9985: 
            case 9986: 
            case 9987: {
                break;
            }
            default: {
                throw new IllegalArgumentException("illegal min filter value: " + v);
            }
        }
        this.minFilter = v;
        this.updateSampler();
    }

    void setClamp(int v) {
        this.clamp = v == 33071;
        this.updateSampler();
    }

    public VulkanImage getVulkanImage() {
        return this.vulkanImage;
    }

    public void setVulkanImage(VulkanImage vulkanImage) {
        this.vulkanImage = vulkanImage;
        this.width = vulkanImage.width;
        this.height = vulkanImage.height;
        this.maxLevel = vulkanImage.mipLevels;
        this.vkFormat = vulkanImage.format;
    }

    static {
        activeTexture = 0;
    }
}

