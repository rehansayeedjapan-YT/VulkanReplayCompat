/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_3532
 */
package net.vulkanmod.config.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_3532;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.RangeOptionWidget;
import net.vulkanmod.config.option.Option;

public class RangeOption
extends Option<Integer> {
    int min;
    int max;
    int step;
    float scaledNewValue;

    public RangeOption(class_2561 name, int min, int max, int step, Function<Integer, class_2561> translator, Consumer<Integer> setter, Supplier<Integer> getter) {
        super(name, setter, getter, translator);
        this.min = min;
        this.max = max;
        this.step = step;
        this.scaledNewValue = this.computeScaledValue(((Integer)this.newValue).intValue());
    }

    public RangeOption(class_2561 name, int min, int max, int step, Consumer<Integer> setter, Supplier<Integer> getter) {
        this(name, min, max, step, i -> class_2561.method_43470((String)String.valueOf(i)), setter, getter);
    }

    @Override
    protected OptionWidget<?> createWidget() {
        RangeOptionWidget widget = new RangeOptionWidget(this, this.name);
        this.widget = widget;
        return widget;
    }

    @Override
    public class_2561 getName() {
        return class_2561.method_30163((String)(this.name.getString() + ": " + ((Integer)this.getNewValue()).toString()));
    }

    public float getScaledValue() {
        return this.scaledNewValue;
    }

    public void setNewValueFromScaledFloat(float f) {
        double n = class_3532.method_16439((float)f, (float)this.min, (float)this.max);
        n = (long)this.step * Math.round(n / (double)this.step);
        this.setNewValue((int)n);
    }

    @Override
    public void setNewValue(Integer newValue) {
        super.setNewValue(newValue);
        this.scaledNewValue = this.computeScaledValue(((Integer)this.newValue).intValue());
    }

    public float getScaledNewValue() {
        return this.scaledNewValue;
    }

    private float computeScaledValue(float value) {
        return (value - (float)this.min) / (float)(this.max - this.min);
    }
}

