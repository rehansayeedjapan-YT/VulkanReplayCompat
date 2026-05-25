
package com.vulkanreplaycompat.mixin;
import net.minecraft.client.render.RenderLayer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
public class Test5 {
    public static void main(String[] args) {
        System.out.println("FIELDS:");
        for (Field f : RenderLayer.class.getFields()) {
            System.out.println("RLF: " + f.getName());
        }
        System.out.println("METHODS:");
        for (Method m : RenderLayer.class.getMethods()) {
            System.out.println("RLM: " + m.getName());
        }
    }
}

