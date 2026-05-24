/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  net.minecraft.class_630$class_628
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.entity.model;

import java.util.Set;
import net.minecraft.class_2350;
import net.minecraft.class_630;
import net.vulkanmod.interfaces.ModelPartCubeMixed;
import net.vulkanmod.render.model.CubeModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_630.class_628.class})
public class ModelPartCubeM
implements ModelPartCubeMixed {
    @Unique
    CubeModel cube;

    @Inject(method={"<init>"}, at={@At(value="FIELD", target="Lnet/minecraft/class_630$class_628;field_3649:[Lnet/minecraft/class_630$class_593;", ordinal=0, shift=At.Shift.AFTER)})
    private void getVertices(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, float q, float r, Set<class_2350> set, CallbackInfo ci) {
        CubeModel cube = new CubeModel();
        cube.setVertices(i, j, f, g, h, k, l, m, n, o, p, bl, q, r, set);
        this.cube = cube;
    }

    @Override
    public CubeModel getCubeModel() {
        return this.cube;
    }
}

