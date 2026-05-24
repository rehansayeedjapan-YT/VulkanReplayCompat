/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1923
 *  net.minecraft.class_2666
 *  net.minecraft.class_634
 *  net.minecraft.class_6606
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_1923;
import net.minecraft.class_2666;
import net.minecraft.class_634;
import net.minecraft.class_6606;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_634.class})
public class ClientPacketListenerM {
    @Inject(method={"method_38543"}, at={@At(value="RETURN")})
    private void setChunkStatus(int x, int z, class_6606 clientboundLightUpdatePacketData, boolean bl, CallbackInfo ci) {
        ChunkStatusMap.INSTANCE.setChunkStatus(x, z, (byte)2);
    }

    @Inject(method={"method_11107"}, at={@At(value="RETURN")})
    private void resetChunkStatus(class_2666 clientboundForgetLevelChunkPacket, CallbackInfo ci) {
        class_1923 chunkPos = clientboundForgetLevelChunkPacket.comp_1726();
        ChunkStatusMap.INSTANCE.resetChunkStatus(chunkPos.field_9181, chunkPos.field_9180, (byte)2);
    }
}

