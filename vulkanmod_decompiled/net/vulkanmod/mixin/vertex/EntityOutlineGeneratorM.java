/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4588
 *  net.minecraft.class_4618$class_4586
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.vertex;

import net.minecraft.class_4588;
import net.minecraft.class_4618;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4618.class_4586.class})
public class EntityOutlineGeneratorM
implements ExtendedVertexBuilder {
    @Shadow
    @Final
    private int comp_2852;
    @Unique
    private ExtendedVertexBuilder extDelegate;
    @Unique
    private boolean canUseFastVertex = false;

    @Inject(method={"<init>*"}, at={@At(value="RETURN")})
    private void getExtBuilder(class_4588 vertexConsumer, int i, CallbackInfo ci) {
        if (vertexConsumer instanceof ExtendedVertexBuilder) {
            this.extDelegate = (ExtendedVertexBuilder)vertexConsumer;
            this.canUseFastVertex = true;
        }
    }

    @Override
    public boolean canUseFastVertex() {
        return this.canUseFastVertex;
    }

    @Override
    public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
        this.extDelegate.vertex(x, y, z, this.comp_2852, u, v, overlay, light, packedNormal);
    }
}

