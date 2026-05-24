/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 */
package net.vulkanmod.config.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.PerformanceImpact;

public abstract class Option<T> {
    protected final class_2561 name;
    protected class_2561 tooltip;
    protected PerformanceImpact impact;
    protected Consumer<T> onApply;
    protected Supplier<T> valueSupplier;
    protected T value;
    protected T newValue;
    protected Function<T, class_2561> translator;
    protected Function<T, class_2561> tooltipTranslator;
    OptionWidget<?> widget;
    protected boolean active;
    protected Runnable onChange;
    protected Supplier<Boolean> activationFn;

    public Option(class_2561 name, Consumer<T> setter, Supplier<T> getter, Function<T, class_2561> translator, Function<T, class_2561> tooltip) {
        this.name = name;
        this.onApply = setter;
        this.valueSupplier = getter;
        this.translator = translator;
        this.tooltipTranslator = tooltip;
        this.value = this.valueSupplier.get();
        this.newValue = this.value;
    }

    public Option(class_2561 name, Consumer<T> setter, Supplier<T> getter, Function<T, class_2561> translator) {
        this.name = name;
        this.onApply = setter;
        this.valueSupplier = getter;
        this.value = this.valueSupplier.get();
        this.newValue = this.value;
        this.translator = translator;
    }

    public Option(class_2561 name, Consumer<T> setter, Supplier<T> getter) {
        this.name = name;
        this.onApply = setter;
        this.valueSupplier = getter;
        this.value = this.valueSupplier.get();
        this.newValue = this.value;
    }

    public Option<T> setOnApply(Consumer<T> onApply) {
        this.onApply = onApply;
        return this;
    }

    public Option<T> setValueSupplier(Supplier<T> supplier) {
        this.valueSupplier = supplier;
        return this;
    }

    public Option<T> setTranslator(Function<T, class_2561> translator) {
        this.translator = translator;
        return this;
    }

    public Function<T, class_2561> getTranslator() {
        return this.translator;
    }

    public Option<T> setTooltip(Function<T, class_2561> tooltipTranslator) {
        this.tooltipTranslator = tooltipTranslator;
        return this;
    }

    public PerformanceImpact getImpact() {
        return this.impact;
    }

    public Option<T> setImpact(PerformanceImpact impact) {
        this.impact = impact;
        return this;
    }

    public Option<T> setActive(boolean active) {
        this.active = active;
        this.widget.active = active;
        return this;
    }

    protected abstract OptionWidget<?> createWidget();

    public OptionWidget<?> getWidget() {
        if (this.widget == null) {
            this.widget = this.createWidget();
        }
        return this.widget;
    }

    public void setNewValue(T t) {
        this.newValue = t;
        if (this.onChange != null) {
            this.onChange.run();
        }
    }

    public void updateActiveState() {
        this.active = this.activationFn != null ? this.activationFn.get() : true;
        this.widget.setActive(this.active);
    }

    public class_2561 getName() {
        return this.name;
    }

    public Option<T> setOnChange(Runnable runnable) {
        this.onChange = runnable;
        return this;
    }

    public Option<T> setActivationFn(Supplier<Boolean> activationFn) {
        this.activationFn = activationFn;
        return this;
    }

    public boolean isChanged() {
        return !this.newValue.equals(this.value);
    }

    public void apply() {
        this.onApply.accept(this.newValue);
        this.value = this.newValue;
    }

    public void resetValue() {
        this.setNewValue(this.value);
    }

    public T getNewValue() {
        return this.newValue;
    }

    public class_2561 getDisplayedValue() {
        return this.translator.apply(this.newValue);
    }

    public class_2561 getTooltip() {
        if (this.tooltipTranslator != null) {
            return this.tooltipTranslator.apply(this.newValue);
        }
        return null;
    }
}

