/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1923
 *  net.minecraft.class_2540
 *  net.minecraft.class_2818
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_631
 *  net.minecraft.class_6603$class_6605
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.vulkanmod.mixin.chunk;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_1923;
import net.minecraft.class_2540;
import net.minecraft.class_2818;
import net.minecraft.class_2902;
import net.minecraft.class_631;
import net.minecraft.class_6603;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_631.class})
public class ClientChunkCacheM {
    @Inject(method={"method_16020"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_23782(Lnet/minecraft/class_1923;)V")})
    private void setChunkStatus(int x, int z, class_2540 friendlyByteBuf, Map<class_2902.class_2903, long[]> map, Consumer<class_6603.class_6605> consumer, CallbackInfoReturnable<class_2818> cir) {
        ChunkStatusMap.INSTANCE.setChunkStatus(x, z, (byte)1);
    }

    @Inject(method={"method_2859"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_631$class_3681;method_62893(ILnet/minecraft/class_2818;)V")})
    private void resetChunkStatus(class_1923 chunkPos, CallbackInfo ci) {
        ChunkStatusMap.INSTANCE.resetChunkStatus(chunkPos.field_9181, chunkPos.field_9180, (byte)1);
    }
}

