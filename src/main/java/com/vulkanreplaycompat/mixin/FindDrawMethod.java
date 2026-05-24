package com.vulkanreplaycompat.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;

public class FindDrawMethod {
    public static void main(String[] args) throws Exception {
        for (Class<?> c : VertexFormat.class.getDeclaredClasses()) {
            System.out.println("Inner class: " + c.getName());
        }
    }
}
