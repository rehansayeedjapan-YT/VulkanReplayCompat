/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.render.texture;

import net.vulkanmod.render.texture.SpriteUpdateUtil;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.Queue;

public class ImageUploadHelper {
    public static final ImageUploadHelper INSTANCE = new ImageUploadHelper();
    final Queue queue = DeviceManager.getGraphicsQueue();
    private CommandPool.CommandBuffer currentCmdBuffer;

    public void submitCommands() {
        if (this.currentCmdBuffer == null) {
            return;
        }
        SpriteUpdateUtil.transitionLayouts();
        this.queue.submitCommands(this.currentCmdBuffer, true);
        Synchronization.INSTANCE.addCommandBuffer(this.currentCmdBuffer, true);
        this.currentCmdBuffer = null;
    }

    public CommandPool.CommandBuffer getOrStartCommandBuffer() {
        if (this.currentCmdBuffer == null) {
            this.currentCmdBuffer = this.queue.beginCommands();
        }
        return this.currentCmdBuffer;
    }

    public CommandPool.CommandBuffer getCommandBuffer() {
        return this.currentCmdBuffer;
    }
}

