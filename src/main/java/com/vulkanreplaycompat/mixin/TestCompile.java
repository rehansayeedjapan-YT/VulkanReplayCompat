package com.vulkanreplaycompat.mixin;

public class TestCompile {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("com.mojang.blaze3d.vertex.DefaultVertexFormat");
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            System.out.println(f.getName());
        }
    }
}
