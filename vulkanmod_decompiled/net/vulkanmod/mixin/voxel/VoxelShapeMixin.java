/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_249
 *  net.minecraft.class_251
 *  net.minecraft.class_265
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.voxel;

import net.minecraft.class_249;
import net.minecraft.class_251;
import net.minecraft.class_265;
import net.vulkanmod.interfaces.VoxelShapeExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_265.class})
public class VoxelShapeMixin
implements VoxelShapeExtended {
    @Shadow
    @Final
    protected class_251 field_1401;
    int co;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initCornerOcclusion(class_251 discreteVoxelShape, CallbackInfo ci) {
        class_251 disShape = this.field_1401;
        class_265 shape = (class_265)this;
        if (!(shape instanceof class_249) || disShape == null) {
            this.co = 0;
            return;
        }
        int xSize = Math.max(disShape.method_1050(), 1);
        int ySize = Math.max(disShape.method_1047(), 1);
        int zSize = Math.max(disShape.method_1048(), 1);
        int co = 0;
        int s = 0;
        for (int y1 = 0; y1 <= 1; ++y1) {
            for (int z1 = 0; z1 <= 1; ++z1) {
                for (int x1 = 0; x1 <= 1; ++x1) {
                    int x2 = x1 * (xSize - 1);
                    int y2 = y1 * (ySize - 1);
                    int z2 = z1 * (zSize - 1);
                    co |= (disShape.method_1063(x2, y2, z2) ? 1 : 0) << s;
                    ++s;
                }
            }
        }
        this.co = co;
    }

    @Override
    public int getCornerOcclusion() {
        return this.co;
    }
}

