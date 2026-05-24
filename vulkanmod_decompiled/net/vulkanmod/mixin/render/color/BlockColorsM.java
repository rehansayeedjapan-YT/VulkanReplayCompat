/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_322
 *  net.minecraft.class_324
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.color;

import net.minecraft.class_2248;
import net.minecraft.class_322;
import net.minecraft.class_324;
import net.vulkanmod.interfaces.color.BlockColorsExtended;
import net.vulkanmod.render.chunk.build.color.BlockColorRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_324.class})
public class BlockColorsM
implements BlockColorsExtended {
    @Unique
    private BlockColorRegistry colorResolvers = new BlockColorRegistry();

    @Inject(method={"method_1690"}, at={@At(value="RETURN")})
    private void onRegister(class_322 blockColor, class_2248[] blocks, CallbackInfo ci) {
        this.colorResolvers.register(blockColor, blocks);
    }

    @Override
    public BlockColorRegistry getColorResolverMap() {
        return this.colorResolvers;
    }
}

