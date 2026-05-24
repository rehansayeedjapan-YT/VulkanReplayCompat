package com.vulkanreplaycompat.mixin;
import net.minecraft.client.render.GameRenderer;
import java.lang.reflect.Method;
public class TestShader {
    public static void main(String[] args) {
        for (Method m : GameRenderer.class.getDeclaredMethods()) {
            System.out.println(m.getReturnType().getSimpleName() + " " + m.getName());
        }
    }
}
