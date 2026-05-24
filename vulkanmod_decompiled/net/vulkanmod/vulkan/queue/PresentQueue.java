/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 */
package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.queue.Queue;
import org.lwjgl.system.MemoryStack;

public class PresentQueue
extends Queue {
    public PresentQueue(MemoryStack stack, int familyIndex) {
        super(stack, familyIndex, false);
    }
}

