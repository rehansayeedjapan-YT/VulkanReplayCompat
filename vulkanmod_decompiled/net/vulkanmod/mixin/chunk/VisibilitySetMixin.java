/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  net.minecraft.class_854
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_2350;
import net.minecraft.class_854;
import net.vulkanmod.interfaces.VisibilitySetExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={class_854.class})
public class VisibilitySetMixin
implements VisibilitySetExtended {
    private long vis = 0L;

    @Overwrite
    public void method_3692(class_2350 dir1, class_2350 dir2, boolean p_112989_) {
        this.vis |= 1L << (dir1.ordinal() << 3) + dir2.ordinal() | 1L << (dir2.ordinal() << 3) + dir1.ordinal();
    }

    @Overwrite
    public void method_3694(boolean bl) {
        if (bl) {
            this.vis = -1L;
        }
    }

    @Overwrite
    public boolean method_3695(class_2350 dir1, class_2350 dir2) {
        return (this.vis & 1L << (dir1.ordinal() << 3) + dir2.ordinal()) != 0L;
    }

    @Override
    public long getVisibility() {
        return this.vis;
    }
}

