/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package net.vulkanmod.render.profiling;

import net.minecraft.class_310;

public abstract class BuildTimeProfiler {
    private static boolean bench = false;
    private static long startTime;
    private static float deltaTime;

    public static void runBench(boolean building) {
        if (bench) {
            if (startTime == 0L) {
                startTime = System.nanoTime();
            }
            if (!building) {
                deltaTime = (float)(System.nanoTime() - startTime) * 1.0E-6f;
                bench = false;
                startTime = 0L;
            }
        }
    }

    public static void startBench() {
        bench = true;
        class_310.method_1551().field_1769.method_3279();
    }

    public static float getDeltaTime() {
        return deltaTime;
    }
}

