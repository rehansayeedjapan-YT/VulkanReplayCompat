/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.SystemUtils
 *  org.lwjgl.glfw.GLFW
 */
package net.vulkanmod.config;

import net.vulkanmod.Initializer;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.glfw.GLFW;

public abstract class Platform {
    private static final int activePlat = Platform.getSupportedPlat();
    private static final String activeDE = Platform.determineDE();

    public static void init() {
        GLFW.glfwInitHint((int)327683, (int)activePlat);
        Initializer.LOGGER.info("Selecting Platform: {}", (Object)Platform.getStringFromPlat());
        Initializer.LOGGER.info("GLFW: {}", (Object)GLFW.glfwGetVersionString());
        GLFW.glfwInit();
    }

    private static int determineDisplayServer() {
        String xdgSessionType = System.getenv("XDG_SESSION_TYPE");
        if (xdgSessionType == null) {
            return 393216;
        }
        return switch (xdgSessionType) {
            case "wayland" -> 393219;
            case "x11" -> 393220;
            default -> 393216;
        };
    }

    private static int getSupportedPlat() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return 393217;
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            return 393218;
        }
        if (SystemUtils.IS_OS_LINUX) {
            return Platform.determineDisplayServer();
        }
        return 393216;
    }

    private static String getStringFromPlat() {
        return switch (activePlat) {
            case 393217 -> "WIN32";
            case 393219 -> "WAYLAND";
            case 393220 -> "X11";
            case 393218 -> "MACOS";
            case 393216 -> "ANDROID";
            default -> throw new IllegalStateException("Unexpected value: " + activePlat);
        };
    }

    private static String determineDE() {
        String xdgSessionDesktop = System.getenv("XDG_SESSION_DESKTOP");
        String xdgCurrentDesktop = System.getenv("XDG_CURRENT_DESKTOP");
        if (xdgSessionDesktop != null) {
            return xdgSessionDesktop.toLowerCase();
        }
        if (xdgCurrentDesktop != null) {
            return xdgCurrentDesktop.toLowerCase();
        }
        return "N/A";
    }

    public static int getActivePlat() {
        return activePlat;
    }

    public static boolean isWayLand() {
        return activePlat == 393219;
    }

    public static boolean isX11() {
        return activePlat == 393220;
    }

    public static boolean isWindows() {
        return activePlat == 393217;
    }

    public static boolean isMacOS() {
        return activePlat == 393218;
    }

    public static boolean isAndroid() {
        return activePlat == 393216;
    }

    public static boolean isGnome() {
        return activeDE.contains("gnome");
    }

    public static boolean isWeston() {
        return activeDE.contains("weston");
    }

    public static boolean isGeneric() {
        return activeDE.contains("generic");
    }
}

