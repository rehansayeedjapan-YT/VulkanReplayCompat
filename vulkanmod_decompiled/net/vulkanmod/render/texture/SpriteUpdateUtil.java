/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VkCommandBuffer
 */
package net.vulkanmod.render.texture;

import java.util.HashSet;
import java.util.Set;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public abstract class SpriteUpdateUtil {
    private static boolean doUpload = true;
    private static final Set<VulkanImage> transitionedLayouts = new HashSet<VulkanImage>();

    public static void setDoUpload(boolean b) {
        doUpload = b;
    }

    public static boolean doUploadFrame() {
        return doUpload;
    }

    public static void addTransitionedLayout(VulkanImage image) {
        transitionedLayouts.add(image);
    }

    public static void transitionLayouts() {
        if (transitionedLayouts.isEmpty()) {
            return;
        }
        VkCommandBuffer commandBuffer = ImageUploadHelper.INSTANCE.getOrStartCommandBuffer().handle;
        transitionedLayouts.forEach(image -> {
            try (MemoryStack stack = MemoryStack.stackPush();){
                image.readOnlyLayout(stack, commandBuffer);
            }
        });
        transitionedLayouts.clear();
    }
}

