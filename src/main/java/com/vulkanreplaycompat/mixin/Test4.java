
package com.vulkanreplaycompat.mixin;
import net.minecraft.client.render.RenderLayer;
import java.lang.reflect.Method;
public class Test4 {
    public static void main(String[] args) {
        System.out.println("TESTING");
        for (Method m : RenderLayer.class.getMethods()) {
            if (m.getName().toLowerCase().contains("gui")) {
                System.out.println("RLM: " + m.getName());
            }
        }
    }
}

