/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.glfw.GLFWVulkan
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.util.vma.Vma
 *  org.lwjgl.util.vma.VmaAllocatorCreateInfo
 *  org.lwjgl.util.vma.VmaVulkanFunctions
 *  org.lwjgl.vulkan.EXTDebugUtils
 *  org.lwjgl.vulkan.KHRSurface
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VK12
 *  org.lwjgl.vulkan.VkAllocationCallbacks
 *  org.lwjgl.vulkan.VkApplicationInfo
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkCommandPoolCreateInfo
 *  org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
 *  org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkInstance
 *  org.lwjgl.vulkan.VkInstanceCreateInfo
 *  org.lwjgl.vulkan.VkLayerProperties
 *  org.lwjgl.vulkan.VkLayerProperties$Buffer
 */
package net.vulkanmod.vulkan;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.device.Device;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.queue.Queue;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.util.VkResult;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;

public class Vulkan {
    public static final boolean ENABLE_VALIDATION_LAYERS = false;
    public static final boolean DYNAMIC_RENDERING = true;
    public static final Set<String> VALIDATION_LAYERS = null;
    public static final Set<String> REQUIRED_EXTENSION = Vulkan.getRequiredExtensionSet();
    public static long window;
    private static VkInstance instance;
    private static long debugMessenger;
    private static long surface;
    private static long commandPool;
    private static VkCommandBuffer immediateCmdBuffer;
    private static long immediateFence;
    private static long allocator;
    private static StagingBuffer[] stagingBuffers;
    public static boolean use24BitsDepthFormat;
    private static int DEFAULT_DEPTH_FORMAT;

    private static Set<String> getRequiredExtensionSet() {
        ArrayList<String> extensions = new ArrayList<String>(List.of("VK_KHR_swapchain"));
        extensions.add("VK_KHR_dynamic_rendering");
        return new HashSet<String>(extensions);
    }

    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create((long)pCallbackData);
        Object s = (messageSeverity & 0x1000) != 0 ? "\u001b[31m" + callbackData.pMessageString() : callbackData.pMessageString();
        System.err.println((String)s);
        if ((messageSeverity & 0x1000) != 0) {
            System.nanoTime();
        }
        return 0;
    }

    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo, VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger) {
        if (VK10.vkGetInstanceProcAddr((VkInstance)instance, (CharSequence)"vkCreateDebugUtilsMessengerEXT") != 0L) {
            return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT((VkInstance)instance, (VkDebugUtilsMessengerCreateInfoEXT)createInfo, (VkAllocationCallbacks)allocationCallbacks, (LongBuffer)pDebugMessenger);
        }
        return -7;
    }

    private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {
        if (VK10.vkGetInstanceProcAddr((VkInstance)instance, (CharSequence)"vkDestroyDebugUtilsMessengerEXT") != 0L) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT((VkInstance)instance, (long)debugMessenger, (VkAllocationCallbacks)allocationCallbacks);
        }
    }

    public static VkDevice getVkDevice() {
        return DeviceManager.vkDevice;
    }

    public static long getAllocator() {
        return allocator;
    }

    public static void initVulkan(long window) {
        Vulkan.createInstance();
        Vulkan.setupDebugMessenger();
        Vulkan.createSurface(window);
        DeviceManager.init(instance);
        Vulkan.createVma();
        MemoryTypes.createMemoryTypes();
        Vulkan.createCommandPool();
        Vulkan.setupDepthFormat();
    }

    static void createStagingBuffers() {
        if (stagingBuffers != null) {
            Vulkan.freeStagingBuffers();
        }
        stagingBuffers = new StagingBuffer[Renderer.getFramesNum()];
        for (int i = 0; i < stagingBuffers.length; ++i) {
            Vulkan.stagingBuffers[i] = new StagingBuffer();
        }
    }

    static void setupDepthFormat() {
        DEFAULT_DEPTH_FORMAT = DeviceManager.findDepthFormat(use24BitsDepthFormat);
    }

    public static void waitIdle() {
        VK10.vkDeviceWaitIdle((VkDevice)DeviceManager.vkDevice);
    }

    public static void cleanUp() {
        VK10.vkDeviceWaitIdle((VkDevice)DeviceManager.vkDevice);
        VK10.vkDestroyCommandPool((VkDevice)DeviceManager.vkDevice, (long)commandPool, null);
        VK10.vkDestroyFence((VkDevice)DeviceManager.vkDevice, (long)immediateFence, null);
        Pipeline.destroyPipelineCache();
        Renderer.getInstance().cleanUpResources();
        Vulkan.freeStagingBuffers();
        try {
            MemoryManager.getInstance().freeAllBuffers();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Vma.vmaDestroyAllocator((long)allocator);
        SamplerManager.cleanUp();
        DeviceManager.destroy();
        Vulkan.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        KHRSurface.vkDestroySurfaceKHR((VkInstance)instance, (long)surface, null);
        VK10.vkDestroyInstance((VkInstance)instance, null);
    }

    private static void freeStagingBuffers() {
        Arrays.stream(stagingBuffers).forEach(Buffer::scheduleFree);
    }

    private static void createInstance() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkApplicationInfo appInfo = VkApplicationInfo.calloc((MemoryStack)stack);
            appInfo.sType(0);
            appInfo.pApplicationName(stack.UTF8Safe((CharSequence)"VulkanMod"));
            appInfo.applicationVersion(VK10.VK_MAKE_VERSION((int)1, (int)0, (int)0));
            appInfo.pEngineName(stack.UTF8Safe((CharSequence)"VulkanMod Engine"));
            appInfo.engineVersion(VK10.VK_MAKE_VERSION((int)1, (int)0, (int)0));
            appInfo.apiVersion(VK12.VK_API_VERSION_1_2);
            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc((MemoryStack)stack);
            createInfo.sType(1);
            createInfo.pApplicationInfo(appInfo);
            createInfo.ppEnabledExtensionNames(Vulkan.getRequiredInstanceExtensions());
            PointerBuffer instancePtr = stack.mallocPointer(1);
            int result = VK10.vkCreateInstance((VkInstanceCreateInfo)createInfo, null, (PointerBuffer)instancePtr);
            Vulkan.checkResult(result, "Failed to create instance");
            instance = new VkInstance(instancePtr.get(0), createInfo);
        }
    }

    static boolean checkValidationLayerSupport() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer layerCount = stack.ints(0);
            VK10.vkEnumerateInstanceLayerProperties((IntBuffer)layerCount, null);
            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc((int)layerCount.get(0), (MemoryStack)stack);
            VK10.vkEnumerateInstanceLayerProperties((IntBuffer)layerCount, (VkLayerProperties.Buffer)availableLayers);
            Set availableLayerNames = availableLayers.stream().map(VkLayerProperties::layerNameString).collect(Collectors.toSet());
            boolean bl = availableLayerNames.containsAll(VALIDATION_LAYERS);
            return bl;
        }
    }

    private static void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
        debugCreateInfo.sType(1000128004);
        debugCreateInfo.messageSeverity(4352);
        debugCreateInfo.messageType(7);
        debugCreateInfo.pfnUserCallback(Vulkan::debugCallback);
    }

    private static void setupDebugMessenger() {
    }

    public static void setDebugLabel(MemoryStack stack, int objectType, long handle, String label) {
    }

    private static void createSurface(long handle) {
        window = handle;
        try (MemoryStack stack = MemoryStack.stackPush();){
            LongBuffer pSurface = stack.longs(0L);
            Vulkan.checkResult(GLFWVulkan.glfwCreateWindowSurface((VkInstance)instance, (long)window, null, (LongBuffer)pSurface), "Failed to create window surface");
            surface = pSurface.get(0);
        }
    }

    private static void createVma() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VmaVulkanFunctions vulkanFunctions = VmaVulkanFunctions.calloc((MemoryStack)stack);
            vulkanFunctions.set(instance, DeviceManager.vkDevice);
            VmaAllocatorCreateInfo allocatorCreateInfo = VmaAllocatorCreateInfo.calloc((MemoryStack)stack);
            allocatorCreateInfo.physicalDevice(DeviceManager.physicalDevice);
            allocatorCreateInfo.device(DeviceManager.vkDevice);
            allocatorCreateInfo.pVulkanFunctions(vulkanFunctions);
            allocatorCreateInfo.instance(instance);
            allocatorCreateInfo.vulkanApiVersion(VK12.VK_API_VERSION_1_2);
            PointerBuffer pAllocator = stack.pointers(0L);
            Vulkan.checkResult(Vma.vmaCreateAllocator((VmaAllocatorCreateInfo)allocatorCreateInfo, (PointerBuffer)pAllocator), "Failed to create Allocator");
            allocator = pAllocator.get(0);
        }
    }

    private static void createCommandPool() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            Queue.QueueFamilyIndices queueFamilyIndices = Queue.getQueueFamilies();
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc((MemoryStack)stack);
            poolInfo.sType(39);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);
            poolInfo.flags(2);
            LongBuffer pCommandPool = stack.mallocLong(1);
            Vulkan.checkResult(VK10.vkCreateCommandPool((VkDevice)DeviceManager.vkDevice, (VkCommandPoolCreateInfo)poolInfo, null, (LongBuffer)pCommandPool), "Failed to create command pool");
            commandPool = pCommandPool.get(0);
        }
    }

    private static PointerBuffer getRequiredInstanceExtensions() {
        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        return glfwExtensions;
    }

    public static void checkResult(int result, String errorMessage) {
        if (result != 0) {
            throw new RuntimeException(String.format("%s: %s", errorMessage, VkResult.decode(result)));
        }
    }

    public static void setVsync(boolean b) {
        SwapChain swapChain = Renderer.getInstance().getSwapChain();
        if (swapChain.isVsync() != b) {
            Renderer.scheduleSwapChainUpdate();
            swapChain.setVsync(b);
        }
    }

    public static int getDefaultDepthFormat() {
        return DEFAULT_DEPTH_FORMAT;
    }

    public static long getSurface() {
        return surface;
    }

    public static long getCommandPool() {
        return commandPool;
    }

    public static StagingBuffer getStagingBuffer() {
        return stagingBuffers[Renderer.getCurrentFrame()];
    }

    public static Device getDevice() {
        return DeviceManager.device;
    }

    static {
        use24BitsDepthFormat = true;
        DEFAULT_DEPTH_FORMAT = 0;
    }
}

