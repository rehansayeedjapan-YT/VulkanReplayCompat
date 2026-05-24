/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1058
 *  net.minecraft.class_2350
 *  net.minecraft.class_5611
 *  net.minecraft.class_777
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.block;

import net.minecraft.class_1058;
import net.minecraft.class_2350;
import net.minecraft.class_5611;
import net.minecraft.class_777;
import net.vulkanmod.render.chunk.build.frapi.helper.NormalHelper;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.model.quad.ModelQuadFlags;
import net.vulkanmod.render.model.quad.ModelQuadView;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_777.class})
public abstract class BakedQuadM
implements ModelQuadView {
    @Shadow
    @Final
    protected class_2350 comp_3723;
    @Shadow
    @Final
    protected int comp_3722;
    private int flags;
    private int normal;
    private QuadFacing facing;

    @Shadow
    public abstract Vector3fc method_76648(int var1);

    @Shadow
    public abstract long method_76649(int var1);

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onInit(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, int tintIndex, class_2350 direction, class_1058 sprite, boolean shade, int lightEmission, CallbackInfo ci) {
        int packedNormal;
        this.flags = ModelQuadFlags.getQuadFlags(this, direction);
        this.normal = packedNormal = NormalHelper.computePackedNormal(this);
        this.facing = QuadFacing.fromNormal(packedNormal);
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public float getX(int idx) {
        return this.method_76648(idx).x();
    }

    @Override
    public float getY(int idx) {
        return this.method_76648(idx).y();
    }

    @Override
    public float getZ(int idx) {
        return this.method_76648(idx).z();
    }

    @Override
    public int getColor(int idx) {
        return -1;
    }

    @Override
    public float getU(int idx) {
        return class_5611.method_76641((long)this.method_76649(idx));
    }

    @Override
    public float getV(int idx) {
        return class_5611.method_76642((long)this.method_76649(idx));
    }

    @Override
    public int getColorIndex() {
        return this.comp_3722;
    }

    @Override
    public class_2350 lightFace() {
        return this.comp_3723;
    }

    @Override
    public class_2350 getFacingDirection() {
        return this.comp_3723;
    }

    @Override
    public QuadFacing getQuadFacing() {
        return this.facing;
    }

    @Override
    public int getNormal() {
        return this.normal;
    }

    @Override
    public boolean isTinted() {
        return this.comp_3722 != -1;
    }

    private static int vertexOffset(int vertexIndex) {
        return vertexIndex * 8;
    }
}

