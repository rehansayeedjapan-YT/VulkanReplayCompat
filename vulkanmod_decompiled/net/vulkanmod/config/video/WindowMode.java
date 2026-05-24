/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.config.video;

public enum WindowMode {
    WINDOWED(0),
    WINDOWED_FULLSCREEN(1),
    EXCLUSIVE_FULLSCREEN(2);

    public final int mode;

    private WindowMode(int mode) {
        this.mode = mode;
    }

    public static WindowMode fromValue(int value) {
        return switch (value) {
            case 0 -> WINDOWED;
            case 1 -> WINDOWED_FULLSCREEN;
            case 2 -> EXCLUSIVE_FULLSCREEN;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public static String getComponentName(WindowMode windowMode) {
        return switch (windowMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "vulkanmod.options.windowMode.windowed";
            case 1 -> "vulkanmod.options.windowMode.windowedFullscreen";
            case 2 -> "options.fullscreen";
        };
    }
}

