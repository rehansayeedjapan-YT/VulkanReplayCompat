/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  net.minecraft.class_3518
 *  org.apache.commons.lang3.Validate
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutBinding$Buffer
 *  org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkPipelineCacheCreateInfo
 *  org.lwjgl.vulkan.VkPipelineLayoutCreateInfo
 *  org.lwjgl.vulkan.VkPushConstantRange
 *  org.lwjgl.vulkan.VkPushConstantRange$Buffer
 *  org.lwjgl.vulkan.VkShaderModuleCreateInfo
 */
package net.vulkanmod.vulkan.shader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.class_3518;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.buffer.UniformBuffer;
import net.vulkanmod.vulkan.shader.DescriptorSets;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.ManualUBO;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.shader.layout.AlignedStruct;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.shader.layout.Uniform;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.util.MappedBuffer;
import org.apache.commons.lang3.Validate;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public abstract class Pipeline {
    private static final VkDevice DEVICE = Vulkan.getVkDevice();
    protected static final long PIPELINE_CACHE = Pipeline.createPipelineCache();
    protected static final List<Pipeline> PIPELINES = new LinkedList<Pipeline>();
    public final String name;
    protected long descriptorSetLayout;
    protected long pipelineLayout;
    protected DescriptorSets[] descriptorSets;
    protected List<UBO> buffers;
    protected ManualUBO manualUBO;
    protected List<ImageDescriptor> imageDescriptors;
    protected PushConstants pushConstants;

    private static long createPipelineCache() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkPipelineCacheCreateInfo cacheCreateInfo = VkPipelineCacheCreateInfo.calloc((MemoryStack)stack);
            cacheCreateInfo.sType(17);
            LongBuffer pPipelineCache = stack.mallocLong(1);
            if (VK10.vkCreatePipelineCache((VkDevice)DEVICE, (VkPipelineCacheCreateInfo)cacheCreateInfo, null, (LongBuffer)pPipelineCache) != 0) {
                throw new RuntimeException("Failed to create graphics pipeline");
            }
            long l = pPipelineCache.get(0);
            return l;
        }
    }

    public static void destroyPipelineCache() {
        VK10.vkDestroyPipelineCache((VkDevice)DEVICE, (long)PIPELINE_CACHE, null);
    }

    public static void recreateDescriptorSets(int frames) {
        PIPELINES.forEach(pipeline -> {
            pipeline.destroyDescriptorSets();
            pipeline.createDescriptorSets(frames);
        });
    }

    public Pipeline(String name) {
        this.name = name;
    }

    protected void createDescriptorSetLayout() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            int bindingsSize = this.buffers.size() + this.imageDescriptors.size();
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc((int)bindingsSize, (MemoryStack)stack);
            for (UBO ubo : this.buffers) {
                VkDescriptorSetLayoutBinding uboLayoutBinding = (VkDescriptorSetLayoutBinding)bindings.get(ubo.getBinding());
                uboLayoutBinding.binding(ubo.getBinding());
                uboLayoutBinding.descriptorCount(1);
                uboLayoutBinding.descriptorType(ubo.getType());
                uboLayoutBinding.pImmutableSamplers(null);
                uboLayoutBinding.stageFlags(ubo.getStages());
            }
            for (ImageDescriptor imageDescriptor : this.imageDescriptors) {
                VkDescriptorSetLayoutBinding samplerLayoutBinding = (VkDescriptorSetLayoutBinding)bindings.get(imageDescriptor.getBinding());
                samplerLayoutBinding.binding(imageDescriptor.getBinding());
                samplerLayoutBinding.descriptorCount(1);
                samplerLayoutBinding.descriptorType(imageDescriptor.getType());
                samplerLayoutBinding.pImmutableSamplers(null);
                samplerLayoutBinding.stageFlags(imageDescriptor.getStages());
            }
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc((MemoryStack)stack);
            layoutInfo.sType(32);
            layoutInfo.pBindings(bindings);
            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (VK10.vkCreateDescriptorSetLayout((VkDevice)DeviceManager.vkDevice, (VkDescriptorSetLayoutCreateInfo)layoutInfo, null, (LongBuffer)pDescriptorSetLayout) != 0) {
                throw new RuntimeException("Failed to create descriptor set layout");
            }
            this.descriptorSetLayout = pDescriptorSetLayout.get(0);
        }
    }

    protected void createPipelineLayout() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            LongBuffer pPipelineLayout;
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc((MemoryStack)stack);
            pipelineLayoutInfo.sType(30);
            pipelineLayoutInfo.pSetLayouts(stack.longs(this.descriptorSetLayout));
            if (this.pushConstants != null) {
                VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc((int)1, (MemoryStack)stack);
                pushConstantRange.size(this.pushConstants.getSize());
                pushConstantRange.offset(0);
                pushConstantRange.stageFlags(1);
                pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
            }
            if (VK10.vkCreatePipelineLayout((VkDevice)DEVICE, (VkPipelineLayoutCreateInfo)pipelineLayoutInfo, null, (LongBuffer)(pPipelineLayout = stack.longs(0L))) != 0) {
                throw new RuntimeException("Failed to create pipeline layout");
            }
            this.pipelineLayout = pPipelineLayout.get(0);
        }
    }

    protected void createDescriptorSets(int frames) {
        this.descriptorSets = new DescriptorSets[frames];
        for (int i = 0; i < frames; ++i) {
            this.descriptorSets[i] = new DescriptorSets(this);
        }
    }

    public void scheduleCleanUp() {
        MemoryManager.getInstance().addFrameOp(this::cleanUp);
    }

    public abstract void cleanUp();

    protected void destroyDescriptorSets() {
        for (DescriptorSets descriptorSets : this.descriptorSets) {
            descriptorSets.cleanUp();
        }
        this.descriptorSets = null;
    }

    public ManualUBO getManualUBO() {
        return this.manualUBO;
    }

    public void resetDescriptorPool(int i) {
        if (this.descriptorSets != null) {
            this.descriptorSets[i].resetIdx();
        }
    }

    public PushConstants getPushConstants() {
        return this.pushConstants;
    }

    public long getLayout() {
        return this.pipelineLayout;
    }

    public List<UBO> getBuffers() {
        return this.buffers;
    }

    public UBO getUBO(int binding) {
        return this.getUBO((UBO ubo) -> ubo.binding == binding);
    }

    public UBO getUBO(String name) {
        return this.getUBO((UBO ubo) -> ubo.name.equals(name));
    }

    public UBO getUBO(Predicate<UBO> fn) {
        UBO ubo = null;
        for (UBO ubo1 : this.buffers) {
            if (!fn.test(ubo1)) continue;
            ubo = ubo1;
        }
        return ubo;
    }

    public ImageDescriptor getImageDescriptor(String name) {
        return this.getImageDescriptor((ImageDescriptor imageDescriptor) -> imageDescriptor.name.equals(name));
    }

    public ImageDescriptor getImageDescriptor(Predicate<ImageDescriptor> fn) {
        ImageDescriptor descriptor = null;
        for (ImageDescriptor descriptor1 : this.imageDescriptors) {
            if (!fn.test(descriptor1)) continue;
            descriptor = descriptor1;
        }
        return descriptor;
    }

    public List<ImageDescriptor> getImageDescriptors() {
        return this.imageDescriptors;
    }

    public void bindDescriptorSets(VkCommandBuffer commandBuffer, int frame) {
        UniformBuffer uniformBuffer = Renderer.getDrawer().getUniformBuffer();
        this.descriptorSets[frame].bindSets(commandBuffer, uniformBuffer, 0);
    }

    public void bindDescriptorSets(VkCommandBuffer commandBuffer, UniformBuffer uniformBuffer, int frame) {
        this.descriptorSets[frame].bindSets(commandBuffer, uniformBuffer, 0);
    }

    protected static long createShaderModule(ByteBuffer spirvCode) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc((MemoryStack)stack);
            createInfo.sType(16);
            createInfo.pCode(spirvCode);
            LongBuffer pShaderModule = stack.mallocLong(1);
            if (VK10.vkCreateShaderModule((VkDevice)DEVICE, (VkShaderModuleCreateInfo)createInfo, null, (LongBuffer)pShaderModule) != 0) {
                throw new RuntimeException("Failed to create shader module");
            }
            long l = pShaderModule.get(0);
            return l;
        }
    }

    public static class Builder {
        final VertexFormat vertexFormat;
        final String shaderPath;
        List<UBO> UBOs;
        ManualUBO manualUBO;
        PushConstants pushConstants;
        List<ImageDescriptor> imageDescriptors;
        int nextBinding;
        SPIRVUtils.SPIRV vertShaderSPIRV;
        SPIRVUtils.SPIRV fragShaderSPIRV;
        RenderPass renderPass;
        Function<Uniform.Info, Supplier<MappedBuffer>> uniformSupplierGetter;

        public Builder(VertexFormat vertexFormat, String path) {
            this.vertexFormat = vertexFormat;
            this.shaderPath = path;
        }

        public Builder(VertexFormat vertexFormat) {
            this(vertexFormat, null);
        }

        public Builder() {
            this(null, null);
        }

        public GraphicsPipeline createGraphicsPipeline() {
            Validate.isTrue((this.imageDescriptors != null && this.UBOs != null && this.vertShaderSPIRV != null && this.fragShaderSPIRV != null ? 1 : 0) != 0, (String)"Cannot create Pipeline: resources missing", (Object[])new Object[0]);
            if (this.manualUBO != null) {
                this.UBOs.add(this.manualUBO);
            }
            return new GraphicsPipeline(this);
        }

        public void setUniforms(List<UBO> UBOs, List<ImageDescriptor> imageDescriptors) {
            this.UBOs = UBOs;
            this.imageDescriptors = imageDescriptors;
        }

        public void setSPIRVs(SPIRVUtils.SPIRV vertShaderSPIRV, SPIRVUtils.SPIRV fragShaderSPIRV) {
            this.vertShaderSPIRV = vertShaderSPIRV;
            this.fragShaderSPIRV = fragShaderSPIRV;
        }

        public void compileShaders(String name, String vsh, String fsh) {
            this.vertShaderSPIRV = SPIRVUtils.compileShader(String.format("%s.vsh", name), vsh, SPIRVUtils.ShaderKind.VERTEX_SHADER);
            this.fragShaderSPIRV = SPIRVUtils.compileShader(String.format("%s.fsh", name), fsh, SPIRVUtils.ShaderKind.FRAGMENT_SHADER);
        }

        public void setVertShaderSPIRV(SPIRVUtils.SPIRV vertShaderSPIRV) {
            this.vertShaderSPIRV = vertShaderSPIRV;
        }

        public void setFragShaderSPIRV(SPIRVUtils.SPIRV fragShaderSPIRV) {
            this.fragShaderSPIRV = fragShaderSPIRV;
        }

        public void parseBindings(JsonObject jsonObject) {
            this.UBOs = new ArrayList<UBO>();
            this.imageDescriptors = new ArrayList<ImageDescriptor>();
            JsonArray jsonUbos = class_3518.method_15292((JsonObject)jsonObject, (String)"UBOs", null);
            JsonArray jsonManualUbos = class_3518.method_15292((JsonObject)jsonObject, (String)"ManualUBOs", null);
            JsonArray jsonSamplers = class_3518.method_15292((JsonObject)jsonObject, (String)"samplers", null);
            JsonArray jsonPushConstants = class_3518.method_15292((JsonObject)jsonObject, (String)"PushConstants", null);
            if (jsonUbos != null) {
                for (JsonElement jsonelement : jsonUbos) {
                    this.parseUboNode(jsonelement);
                }
            }
            if (jsonManualUbos != null) {
                this.parseManualUboNode(jsonManualUbos.get(0));
            }
            if (jsonSamplers != null) {
                for (JsonElement jsonelement : jsonSamplers) {
                    this.parseSamplerNode(jsonelement);
                }
            }
            if (jsonPushConstants != null) {
                this.parsePushConstantNode(jsonPushConstants);
            }
        }

        public void setUniformSupplierGetter(Function<Uniform.Info, Supplier<MappedBuffer>> uniformSupplierGetter) {
            this.uniformSupplierGetter = uniformSupplierGetter;
        }

        private void parseUboNode(JsonElement jsonelement) {
            UBO ubo;
            JsonObject uboJson = class_3518.method_15295((JsonElement)jsonelement, (String)"UBO");
            int binding = class_3518.method_15260((JsonObject)uboJson, (String)"binding");
            int type = Builder.getStageFromString(class_3518.method_15265((JsonObject)uboJson, (String)"type"));
            if (class_3518.method_15264((JsonObject)uboJson, (String)"fields")) {
                JsonArray fields = class_3518.method_15261((JsonObject)uboJson, (String)"fields");
                AlignedStruct.Builder builder = new AlignedStruct.Builder();
                for (JsonElement field : fields) {
                    JsonObject fieldObject = class_3518.method_15295((JsonElement)field, (String)"uniform");
                    String name = class_3518.method_15265((JsonObject)fieldObject, (String)"name");
                    String type2 = class_3518.method_15265((JsonObject)fieldObject, (String)"type");
                    int count = class_3518.method_15260((JsonObject)fieldObject, (String)"count");
                    Uniform.Info uniformInfo = Uniform.createUniformInfo(type2, name, count);
                    uniformInfo.setupSupplier();
                    if (!uniformInfo.hasSupplier()) {
                        if (this.uniformSupplierGetter != null) {
                            Supplier<MappedBuffer> uniformSupplier = this.uniformSupplierGetter.apply(uniformInfo);
                            if (uniformSupplier == null) {
                                throw new IllegalStateException("No uniform supplier found for uniform: (%s:%s)".formatted(type2, name));
                            }
                            uniformInfo.setBufferSupplier(uniformSupplier);
                        } else {
                            throw new IllegalStateException("No uniform supplier found for uniform: (%s:%s)".formatted(type2, name));
                        }
                    }
                    builder.addUniformInfo(uniformInfo);
                }
                ubo = builder.buildUBO(binding, type);
            } else {
                int size = class_3518.method_15260((JsonObject)uboJson, (String)"size");
                ubo = new UBO("UBO %d".formatted(binding), binding, type, size, null);
                ubo.setUseGlobalBuffer(false);
            }
            if (binding >= this.nextBinding) {
                this.nextBinding = binding + 1;
            }
            this.UBOs.add(ubo);
        }

        private void parseManualUboNode(JsonElement jsonelement) {
            JsonObject jsonobject = class_3518.method_15295((JsonElement)jsonelement, (String)"ManualUBO");
            int binding = class_3518.method_15260((JsonObject)jsonobject, (String)"binding");
            int stage = Builder.getStageFromString(class_3518.method_15265((JsonObject)jsonobject, (String)"type"));
            int size = class_3518.method_15260((JsonObject)jsonobject, (String)"size");
            if (binding >= this.nextBinding) {
                this.nextBinding = binding + 1;
            }
            this.manualUBO = new ManualUBO(binding, stage, size);
        }

        private void parseSamplerNode(JsonElement jsonelement) {
            JsonObject jsonobject = class_3518.method_15295((JsonElement)jsonelement, (String)"Sampler");
            String name = class_3518.method_15265((JsonObject)jsonobject, (String)"name");
            int imageIdx = VTextureSelector.getTextureIdx(name);
            this.imageDescriptors.add(new ImageDescriptor(this.nextBinding, "sampler2D", name, imageIdx));
            ++this.nextBinding;
        }

        private void parsePushConstantNode(JsonArray jsonArray) {
            AlignedStruct.Builder builder = new AlignedStruct.Builder();
            for (JsonElement jsonelement : jsonArray) {
                JsonObject jsonobject2 = class_3518.method_15295((JsonElement)jsonelement, (String)"PushConstants");
                String name = class_3518.method_15265((JsonObject)jsonobject2, (String)"name");
                String type2 = class_3518.method_15265((JsonObject)jsonobject2, (String)"type");
                int count = class_3518.method_15260((JsonObject)jsonobject2, (String)"count");
                Uniform.Info uniformInfo = Uniform.createUniformInfo(type2, name, count);
                uniformInfo.setupSupplier();
                builder.addUniformInfo(uniformInfo);
            }
            this.pushConstants = builder.buildPushConstant();
        }

        public static int getStageFromString(String s) {
            return switch (s) {
                case "vertex" -> 1;
                case "fragment" -> 16;
                case "all" -> 31;
                case "compute" -> 32;
                default -> throw new RuntimeException("cannot identify type..");
            };
        }
    }
}

