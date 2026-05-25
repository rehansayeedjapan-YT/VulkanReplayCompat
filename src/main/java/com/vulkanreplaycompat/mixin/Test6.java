
package com.vulkanreplaycompat.mixin;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
public class Test6 {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("C:\\Users\\rehan\\.gradle\\caches\\fabric-loom\\1.21.11\\net.fabricmc.yarn.1_21_11.1.21.11+build.1-v2\\minecraft-mapped.jar");
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class") && entry.getName().startsWith("net/minecraft/client/render/")) {
                String className = entry.getName().replace("/", ".").replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    for (Method m : clazz.getDeclaredMethods()) {
                        if (m.getName().toLowerCase().contains("gui") && m.getReturnType() == RenderLayer.class) {
                            System.out.println("FOUND: " + className + "." + m.getName());
                        }
                    }
                } catch (Throwable t) {}
            }
        }
        zip.close();
    }
}

