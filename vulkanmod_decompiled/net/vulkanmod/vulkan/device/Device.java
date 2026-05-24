/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VK11
 *  org.lwjgl.vulkan.VkExtensionProperties
 *  org.lwjgl.vulkan.VkExtensionProperties$Buffer
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 *  org.lwjgl.vulkan.VkPhysicalDeviceProperties
 *  org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features
 */
package net.vulkanmod.vulkan.device;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;

public class Device {
    final VkPhysicalDevice physicalDevice;
    final VkPhysicalDeviceProperties properties;
    private final int vendorId;
    public final String vendorIdString;
    public final String deviceName;
    public final String driverVersion;
    public final String vkVersion;
    public final VkPhysicalDeviceFeatures2 availableFeatures;
    public final VkPhysicalDeviceVulkan11Features availableFeatures11;
    private boolean drawIndirectSupported;

    public Device(VkPhysicalDevice device) {
        this.physicalDevice = device;
        this.properties = VkPhysicalDeviceProperties.malloc();
        VK10.vkGetPhysicalDeviceProperties((VkPhysicalDevice)this.physicalDevice, (VkPhysicalDeviceProperties)this.properties);
        this.vendorId = this.properties.vendorID();
        this.vendorIdString = Device.decodeVendor(this.properties.vendorID());
        this.deviceName = this.properties.deviceNameString();
        this.driverVersion = Device.decodeDvrVersion(this.properties.driverVersion(), this.properties.vendorID());
        this.vkVersion = Device.decDefVersion(this.properties.apiVersion());
        this.availableFeatures = VkPhysicalDeviceFeatures2.calloc();
        this.availableFeatures.sType$Default();
        this.availableFeatures11 = VkPhysicalDeviceVulkan11Features.malloc();
        this.availableFeatures11.sType$Default();
        this.availableFeatures.pNext(this.availableFeatures11);
        VK11.vkGetPhysicalDeviceFeatures2((VkPhysicalDevice)this.physicalDevice, (VkPhysicalDeviceFeatures2)this.availableFeatures);
        if (this.availableFeatures.features().multiDrawIndirect() && this.availableFeatures11.shaderDrawParameters()) {
            this.drawIndirectSupported = true;
        }
    }

    private static String decodeVendor(int i) {
        return switch (i) {
            case 4318 -> "Nvidia";
            case 4098, 4130 -> "AMD";
            case 32902 -> "Intel";
            case 4112 -> "Imagination Technologies";
            case 5045 -> "ARM";
            case 20803 -> "Qualcomm";
            case 4203 -> "Apple";
            case 5348 -> "Broadcom";
            case 6880 -> "Google";
            case 65541 -> "Mesa";
            default -> "undef";
        };
    }

    static String decDefVersion(int v) {
        return VK10.VK_VERSION_MAJOR((int)v) + "." + VK10.VK_VERSION_MINOR((int)v) + "." + VK10.VK_VERSION_PATCH((int)v);
    }

    private static String decodeDvrVersion(int v, int i) {
        return switch (i) {
            case 4318 -> Device.decodeNvidia(v);
            case 4098, 4130 -> Device.decDefVersion(v);
            case 32902 -> Device.decIntelVersion(v);
            default -> Device.decDefVersion(v);
        };
    }

    private static String decIntelVersion(int v) {
        return GLFW.glfwGetPlatform() == 393217 ? (v >>> 14) + "." + (v & 0x3FFF) : Device.decDefVersion(v);
    }

    private static String decodeNvidia(int v) {
        return (v >>> 22 & 0x3FF) + "." + (v >>> 14 & 0xFF) + "." + (v >>> 6 & 0xFF) + "." + (v & 0xFF);
    }

    static int getVkVer() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer a = stack.mallocInt(1);
            VK11.vkEnumerateInstanceVersion((IntBuffer)a);
            int vkVer1 = a.get(0);
            if (VK10.VK_VERSION_MINOR((int)vkVer1) < 2) {
                throw new RuntimeException("Vulkan 1.2 not supported: Only Has: %s".formatted(Device.decDefVersion(vkVer1)));
            }
            int n = vkVer1;
            return n;
        }
    }

    public Set<String> getUnsupportedExtensions(Set<String> requiredExtensions) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer extensionCount = stack.ints(0);
            VK10.vkEnumerateDeviceExtensionProperties((VkPhysicalDevice)this.physicalDevice, (CharSequence)null, (IntBuffer)extensionCount, null);
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc((int)extensionCount.get(0), (MemoryStack)stack);
            VK10.vkEnumerateDeviceExtensionProperties((VkPhysicalDevice)this.physicalDevice, (CharSequence)null, (IntBuffer)extensionCount, (VkExtensionProperties.Buffer)availableExtensions);
            Set extensions = availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(Collectors.toSet());
            HashSet<String> unsupportedExtensions = new HashSet<String>(requiredExtensions);
            unsupportedExtensions.removeAll(extensions);
            HashSet<String> hashSet = unsupportedExtensions;
            return hashSet;
        }
    }

    public boolean isDrawIndirectSupported() {
        return this.drawIndirectSupported;
    }

    public boolean isAMD() {
        return this.vendorId == 4130 || this.vendorId == 4098;
    }

    public boolean isNvidia() {
        return this.vendorId == 4318;
    }

    public boolean isIntel() {
        return this.vendorId == 32902;
    }
}

