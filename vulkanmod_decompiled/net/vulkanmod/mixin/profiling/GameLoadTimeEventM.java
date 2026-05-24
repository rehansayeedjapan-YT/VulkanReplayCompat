/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  net.minecraft.class_7965
 *  net.minecraft.class_7966
 *  net.minecraft.class_7969
 *  net.minecraft.class_8561
 *  net.minecraft.class_8561$class_8562
 *  org.slf4j.Logger
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.profiling;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_7965;
import net.minecraft.class_7966;
import net.minecraft.class_7969;
import net.minecraft.class_8561;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_8561.class})
public class GameLoadTimeEventM {
    @Shadow
    @Final
    private Map<class_7969<class_8561.class_8562>, Stopwatch> field_44845;
    @Shadow
    @Final
    private static Logger field_44843;
    @Shadow
    private OptionalLong field_44846;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void send(class_7965 telemetryEventSender) {
        Reference2ReferenceOpenHashMap measurements = new Reference2ReferenceOpenHashMap();
        GameLoadTimeEventM gameLoadTimeEventM = this;
        synchronized (gameLoadTimeEventM) {
            this.field_44845.forEach((arg_0, arg_1) -> GameLoadTimeEventM.lambda$send$0((Map)measurements, arg_0, arg_1));
            this.field_44846.ifPresent(arg_0 -> GameLoadTimeEventM.lambda$send$1((Map)measurements, arg_0));
            this.field_44845.clear();
        }
        StringBuilder stringBuilder = new StringBuilder("\n");
        for (class_7969 property : measurements.keySet()) {
            class_8561.class_8562 measurement = (class_8561.class_8562)measurements.get(property);
            stringBuilder.append("%s: %sms\n".formatted(property.comp_1171(), measurement.comp_1531()));
        }
        field_44843.info(stringBuilder.toString());
    }

    private static /* synthetic */ void lambda$send$1(Map measurements, long l) {
        measurements.put(class_7969.field_44835, new class_8561.class_8562((int)l));
    }

    private static /* synthetic */ void lambda$send$0(Map measurements, class_7969 telemetryProperty, Stopwatch stopwatch) {
        if (!stopwatch.isRunning()) {
            long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            measurements.put(telemetryProperty, new class_8561.class_8562((int)l));
        } else {
            field_44843.warn("Measurement {} was discarded since it was still ongoing when the event {} was sent.", (Object)telemetryProperty.comp_1171(), (Object)class_7966.field_44833.method_47720());
        }
    }
}

