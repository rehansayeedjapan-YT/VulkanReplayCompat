/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.glfw.GLFWVulkan
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.KHRSurface
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkDeviceCreateInfo
 *  org.lwjgl.vulkan.VkDeviceQueueCreateInfo
 *  org.lwjgl.vulkan.VkDeviceQueueCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkExtensionProperties
 *  org.lwjgl.vulkan.VkExtensionProperties$Buffer
 *  org.lwjgl.vulkan.VkFormatProperties
 *  org.lwjgl.vulkan.VkInstance
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkPhysicalDeviceDynamicRenderingFeaturesKHR
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures
 *  org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
 *  org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties
 *  org.lwjgl.vulkan.VkPhysicalDeviceProperties
 *  org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features
 *  org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
 *  org.lwjgl.vulkan.VkSurfaceFormatKHR
 *  org.lwjgl.vulkan.VkSurfaceFormatKHR$Buffer
 */
package net.vulkanmod.vulkan.device;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.Device;
import net.vulkanmod.vulkan.queue.ComputeQueue;
import net.vulkanmod.vulkan.queue.GraphicsQueue;
import net.vulkanmod.vulkan.queue.PresentQueue;
import net.vulkanmod.vulkan.queue.Queue;
import net.vulkanmod.vulkan.queue.TransferQueue;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceDynamicRenderingFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public abstract class DeviceManager {
    public static List<Device> availableDevices;
    public static List<Device> suitableDevices;
    public static VkPhysicalDevice physicalDevice;
    public static VkDevice vkDevice;
    public static Device device;
    public static VkPhysicalDeviceProperties deviceProperties;
    public static VkPhysicalDeviceMemoryProperties memoryProperties;
    public static SurfaceProperties surfaceProperties;
    static GraphicsQueue graphicsQueue;
    static PresentQueue presentQueue;
    static TransferQueue transferQueue;
    static ComputeQueue computeQueue;

    public static void init(VkInstance instance) {
        try {
            DeviceManager.getSuitableDevices(instance);
            DeviceManager.pickPhysicalDevice();
            DeviceManager.createLogicalDevice();
        }
        catch (Exception e) {
            Initializer.LOGGER.info(DeviceManager.getAvailableDevicesInfo());
            throw new RuntimeException(e);
        }
    }

    static List<Device> getAvailableDevices(VkInstance instance) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            ObjectArrayList devices = new ObjectArrayList();
            IntBuffer deviceCount = stack.ints(0);
            VK10.vkEnumeratePhysicalDevices((VkInstance)instance, (IntBuffer)deviceCount, null);
            if (deviceCount.get(0) == 0) {
                List<Device> list = List.of();
                return list;
            }
            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            VK10.vkEnumeratePhysicalDevices((VkInstance)instance, (IntBuffer)deviceCount, (PointerBuffer)ppPhysicalDevices);
            for (int i = 0; i < ppPhysicalDevices.capacity(); ++i) {
                VkPhysicalDevice currentDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);
                Device device = new Device(currentDevice);
                devices.add(device);
            }
            ObjectArrayList objectArrayList = devices;
            return objectArrayList;
        }
    }

    static void getSuitableDevices(VkInstance instance) {
        availableDevices = DeviceManager.getAvailableDevices(instance);
        ObjectArrayList devices = new ObjectArrayList();
        for (Device device : availableDevices) {
            if (!DeviceManager.isDeviceSuitable(device.physicalDevice)) continue;
            devices.add(device);
        }
        suitableDevices = devices;
    }

    public static void pickPhysicalDevice() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            int deviceIdx = Initializer.CONFIG.device;
            if (deviceIdx >= 0 && deviceIdx < suitableDevices.size()) {
                device = suitableDevices.get(deviceIdx);
            } else {
                device = DeviceManager.autoPickDevice();
                Initializer.CONFIG.device = -1;
            }
            physicalDevice = DeviceManager.device.physicalDevice;
            deviceProperties = DeviceManager.device.properties;
            memoryProperties = VkPhysicalDeviceMemoryProperties.malloc();
            VK10.vkGetPhysicalDeviceMemoryProperties((VkPhysicalDevice)physicalDevice, (VkPhysicalDeviceMemoryProperties)memoryProperties);
            surfaceProperties = DeviceManager.querySurfaceProperties(physicalDevice, stack);
        }
    }

    static Device autoPickDevice() {
        ArrayList<Device> integratedGPUs = new ArrayList<Device>();
        ArrayList<Device> otherDevices = new ArrayList<Device>();
        boolean flag = false;
        Device currentDevice = null;
        Iterator<Device> iterator = suitableDevices.iterator();
        while (iterator.hasNext()) {
            Device device;
            currentDevice = device = iterator.next();
            int deviceType = device.properties.deviceType();
            if (deviceType == 2) {
                flag = true;
                break;
            }
            if (deviceType == 1) {
                integratedGPUs.add(device);
                continue;
            }
            otherDevices.add(device);
        }
        if (!flag) {
            if (!integratedGPUs.isEmpty()) {
                currentDevice = (Device)integratedGPUs.get(0);
            } else if (!otherDevices.isEmpty()) {
                currentDevice = (Device)otherDevices.get(0);
            } else {
                throw new IllegalStateException("Failed to find a suitable GPU");
            }
        }
        return currentDevice;
    }

    public static void createLogicalDevice() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            Queue.QueueFamilyIndices indices = Queue.findQueueFamilies(physicalDevice);
            int[] uniqueQueueFamilies = indices.unique();
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc((int)uniqueQueueFamilies.length, (MemoryStack)stack);
            for (int i = 0; i < uniqueQueueFamilies.length; ++i) {
                VkDeviceQueueCreateInfo queueCreateInfo = (VkDeviceQueueCreateInfo)queueCreateInfos.get(i);
                queueCreateInfo.sType(2);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }
            VkPhysicalDeviceVulkan11Features deviceVulkan11Features = VkPhysicalDeviceVulkan11Features.calloc((MemoryStack)stack);
            deviceVulkan11Features.sType$Default();
            deviceVulkan11Features.shaderDrawParameters(device.isDrawIndirectSupported());
            VkPhysicalDeviceFeatures2 deviceFeatures = VkPhysicalDeviceFeatures2.calloc((MemoryStack)stack);
            deviceFeatures.sType$Default();
            deviceFeatures.features().samplerAnisotropy(DeviceManager.device.availableFeatures.features().samplerAnisotropy());
            deviceFeatures.features().logicOp(DeviceManager.device.availableFeatures.features().logicOp());
            deviceFeatures.features().multiDrawIndirect(device.isDrawIndirectSupported());
            if (DeviceManager.device.availableFeatures.features().wideLines()) {
                deviceFeatures.features().wideLines(true);
                VRenderSystem.canSetLineWidth = true;
            }
            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc((MemoryStack)stack);
            createInfo.sType$Default();
            createInfo.sType(3);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            createInfo.pEnabledFeatures(deviceFeatures.features());
            createInfo.pNext(deviceVulkan11Features);
            VkPhysicalDeviceDynamicRenderingFeaturesKHR dynamicRenderingFeaturesKHR = VkPhysicalDeviceDynamicRenderingFeaturesKHR.calloc((MemoryStack)stack);
            dynamicRenderingFeaturesKHR.sType$Default();
            dynamicRenderingFeaturesKHR.dynamicRendering(true);
            deviceVulkan11Features.pNext(dynamicRenderingFeaturesKHR.address());
            createInfo.ppEnabledExtensionNames(VUtil.asPointerBuffer(Vulkan.REQUIRED_EXTENSION));
            PointerBuffer pDevice = stack.pointers(0L);
            int res = VK10.vkCreateDevice((VkPhysicalDevice)physicalDevice, (VkDeviceCreateInfo)createInfo, null, (PointerBuffer)pDevice);
            Vulkan.checkResult(res, "Failed to create logical device");
            vkDevice = new VkDevice(pDevice.get(0), physicalDevice, createInfo, VK12.VK_API_VERSION_1_2);
            graphicsQueue = new GraphicsQueue(stack, indices.graphicsFamily);
            transferQueue = new TransferQueue(stack, indices.transferFamily);
            presentQueue = new PresentQueue(stack, indices.presentFamily);
            computeQueue = new ComputeQueue(stack, indices.computeFamily);
        }
    }

    private static PointerBuffer getRequiredExtensions() {
        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        return glfwExtensions;
    }

    private static boolean isDeviceSuitable(VkPhysicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            Queue.QueueFamilyIndices indices = Queue.findQueueFamilies(device);
            VkExtensionProperties.Buffer availableExtensions = DeviceManager.getAvailableExtension(stack, device);
            boolean extensionsSupported = availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(Collectors.toSet()).containsAll(Vulkan.REQUIRED_EXTENSION);
            boolean swapChainAdequate = false;
            if (extensionsSupported) {
                SurfaceProperties surfaceProperties = DeviceManager.querySurfaceProperties(device, stack);
                swapChainAdequate = surfaceProperties.formats.hasRemaining() && surfaceProperties.presentModes.hasRemaining();
            }
            VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.malloc((MemoryStack)stack);
            VK10.vkGetPhysicalDeviceFeatures((VkPhysicalDevice)device, (VkPhysicalDeviceFeatures)supportedFeatures);
            boolean anisotropicFilterSupported = supportedFeatures.samplerAnisotropy();
            boolean bl = indices.isSuitable() && extensionsSupported && swapChainAdequate;
            return bl;
        }
    }

    private static VkExtensionProperties.Buffer getAvailableExtension(MemoryStack stack, VkPhysicalDevice device) {
        IntBuffer extensionCount = stack.ints(0);
        VK10.vkEnumerateDeviceExtensionProperties((VkPhysicalDevice)device, (CharSequence)null, (IntBuffer)extensionCount, null);
        VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc((int)extensionCount.get(0), (MemoryStack)stack);
        VK10.vkEnumerateDeviceExtensionProperties((VkPhysicalDevice)device, (CharSequence)null, (IntBuffer)extensionCount, (VkExtensionProperties.Buffer)availableExtensions);
        return availableExtensions;
    }

    public static int findDepthFormat(boolean use24BitsDepthFormat) {
        int[] nArray;
        if (use24BitsDepthFormat) {
            int[] nArray2 = new int[4];
            nArray2[0] = 129;
            nArray2[1] = 125;
            nArray2[2] = 126;
            nArray = nArray2;
            nArray2[3] = 130;
        } else {
            int[] nArray3 = new int[2];
            nArray3[0] = 126;
            nArray = nArray3;
            nArray3[1] = 130;
        }
        int[] formats = nArray;
        return DeviceManager.findSupportedFormat(0, 512, formats);
    }

    private static int findSupportedFormat(int tiling, int features, int ... formatCandidates) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkFormatProperties props = VkFormatProperties.calloc((MemoryStack)stack);
            for (int format : formatCandidates) {
                VK10.vkGetPhysicalDeviceFormatProperties((VkPhysicalDevice)physicalDevice, (int)format, (VkFormatProperties)props);
                if (tiling == 1 && (props.linearTilingFeatures() & features) == features) {
                    int n = format;
                    return n;
                }
                if (tiling != 0 || (props.optimalTilingFeatures() & features) != features) continue;
                int n = format;
                return n;
            }
        }
        throw new RuntimeException("Failed to find supported format");
    }

    public static String getAvailableDevicesInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        if (availableDevices == null) {
            stringBuilder.append("\tDevice Manager not initialized");
            return stringBuilder.toString();
        }
        if (availableDevices.isEmpty()) {
            stringBuilder.append("\tNo available device found");
        }
        for (Device device : availableDevices) {
            stringBuilder.append("\tDevice: %s\n".formatted(device.deviceName));
            stringBuilder.append("\t\tVulkan Version: %s\n".formatted(device.vkVersion));
            stringBuilder.append("\t\t");
            Set<String> unsupportedExtensions = device.getUnsupportedExtensions(Vulkan.REQUIRED_EXTENSION);
            if (unsupportedExtensions.isEmpty()) {
                stringBuilder.append("All required extensions are supported\n");
                continue;
            }
            stringBuilder.append("Unsupported extension: %s\n".formatted(unsupportedExtensions));
        }
        return stringBuilder.toString();
    }

    public static void destroy() {
        graphicsQueue.cleanUp();
        transferQueue.cleanUp();
        computeQueue.cleanUp();
        VK10.vkDestroyDevice((VkDevice)vkDevice, null);
    }

    public static GraphicsQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public static PresentQueue getPresentQueue() {
        return presentQueue;
    }

    public static TransferQueue getTransferQueue() {
        return transferQueue;
    }

    public static ComputeQueue getComputeQueue() {
        return computeQueue;
    }

    public static SurfaceProperties querySurfaceProperties(VkPhysicalDevice device, MemoryStack stack) {
        long surface = Vulkan.getSurface();
        SurfaceProperties details = new SurfaceProperties();
        details.capabilities = VkSurfaceCapabilitiesKHR.malloc((MemoryStack)stack);
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR((VkPhysicalDevice)device, (long)surface, (VkSurfaceCapabilitiesKHR)details.capabilities);
        IntBuffer count = stack.ints(0);
        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR((VkPhysicalDevice)device, (long)surface, (IntBuffer)count, null);
        if (count.get(0) != 0) {
            details.formats = VkSurfaceFormatKHR.malloc((int)count.get(0), (MemoryStack)stack);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR((VkPhysicalDevice)device, (long)surface, (IntBuffer)count, (VkSurfaceFormatKHR.Buffer)details.formats);
        }
        KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR((VkPhysicalDevice)device, (long)surface, (IntBuffer)count, null);
        if (count.get(0) != 0) {
            details.presentModes = stack.mallocInt(count.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR((VkPhysicalDevice)device, (long)surface, (IntBuffer)count, (IntBuffer)details.presentModes);
        }
        return details;
    }

    public static class SurfaceProperties {
        public VkSurfaceCapabilitiesKHR capabilities;
        public VkSurfaceFormatKHR.Buffer formats;
        public IntBuffer presentModes;
    }
}

