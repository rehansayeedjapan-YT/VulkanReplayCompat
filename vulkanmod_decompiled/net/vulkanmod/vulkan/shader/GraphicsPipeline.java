/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormatElement
 *  com.mojang.blaze3d.vertex.VertexFormatElement$Type
 *  com.mojang.blaze3d.vertex.VertexFormatElement$Usage
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  net.minecraft.class_290
 *  org.lwjgl.system.CustomBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo
 *  org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState
 *  org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState$Buffer
 *  org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR
 *  org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo
 *  org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo$Buffer
 *  org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo
 *  org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo
 *  org.lwjgl.vulkan.VkVertexInputAttributeDescription
 *  org.lwjgl.vulkan.VkVertexInputAttributeDescription$Buffer
 *  org.lwjgl.vulkan.VkVertexInputBindingDescription
 *  org.lwjgl.vulkan.VkVertexInputBindingDescription$Buffer
 */
package net.vulkanmod.vulkan.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import net.minecraft.class_290;
import net.vulkanmod.interfaces.VertexFormatMixed;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class GraphicsPipeline
extends Pipeline {
    private final Object2LongMap<PipelineState> graphicsPipelines = new Object2LongOpenHashMap();
    private final VertexFormat vertexFormat;
    private final VertexInputDescription vertexInputDescription;
    private long vertShaderModule = 0L;
    private long fragShaderModule = 0L;

    GraphicsPipeline(Pipeline.Builder builder) {
        super(builder.shaderPath);
        this.buffers = builder.UBOs;
        this.manualUBO = builder.manualUBO;
        this.imageDescriptors = builder.imageDescriptors;
        this.pushConstants = builder.pushConstants;
        this.vertexFormat = builder.vertexFormat;
        this.vertexInputDescription = new VertexInputDescription(this.vertexFormat);
        this.createDescriptorSetLayout();
        this.createPipelineLayout();
        this.createShaderModules(builder.vertShaderSPIRV, builder.fragShaderSPIRV);
        if (builder.renderPass != null) {
            this.graphicsPipelines.computeIfAbsent((Object)PipelineState.DEFAULT, this::createGraphicsPipeline);
        }
        this.createDescriptorSets(Renderer.getFramesNum());
        PIPELINES.add(this);
    }

    public long getHandle(PipelineState state) {
        return this.graphicsPipelines.computeIfAbsent((Object)state, this::createGraphicsPipeline);
    }

    private long createGraphicsPipeline(PipelineState state) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer entryPoint = stack.UTF8((CharSequence)"main");
            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc((int)2, (MemoryStack)stack);
            VkPipelineShaderStageCreateInfo vertShaderStageInfo = (VkPipelineShaderStageCreateInfo)shaderStages.get(0);
            vertShaderStageInfo.sType(18);
            vertShaderStageInfo.stage(1);
            vertShaderStageInfo.module(this.vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);
            VkPipelineShaderStageCreateInfo fragShaderStageInfo = (VkPipelineShaderStageCreateInfo)shaderStages.get(1);
            fragShaderStageInfo.sType(18);
            fragShaderStageInfo.stage(16);
            fragShaderStageInfo.module(this.fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc((MemoryStack)stack);
            vertexInputInfo.sType(19);
            if (this.vertexInputDescription != null) {
                vertexInputInfo.pVertexBindingDescriptions(this.vertexInputDescription.bindingDescriptions);
                vertexInputInfo.pVertexAttributeDescriptions(this.vertexInputDescription.attributeDescriptions);
            }
            int topology = PipelineState.AssemblyRasterState.decodeTopology(state.assemblyRasterState);
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc((MemoryStack)stack);
            inputAssembly.sType(20);
            inputAssembly.topology(topology);
            inputAssembly.primitiveRestartEnable(false);
            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc((MemoryStack)stack);
            viewportState.sType(22);
            viewportState.viewportCount(1);
            viewportState.scissorCount(1);
            int polygonMode = PipelineState.AssemblyRasterState.decodePolygonMode(state.assemblyRasterState);
            int cullMode = PipelineState.AssemblyRasterState.decodeCullMode(state.assemblyRasterState);
            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc((MemoryStack)stack);
            rasterizer.sType(23);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(polygonMode);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(cullMode);
            rasterizer.frontFace(0);
            rasterizer.depthBiasEnable(true);
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc((MemoryStack)stack);
            multisampling.sType(24);
            multisampling.sampleShadingEnable(false);
            multisampling.rasterizationSamples(1);
            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc((MemoryStack)stack);
            depthStencil.sType(25);
            depthStencil.depthTestEnable(PipelineState.DepthState.depthTest(state.depthState_i));
            depthStencil.depthWriteEnable(PipelineState.DepthState.depthMask(state.depthState_i));
            depthStencil.depthCompareOp(PipelineState.DepthState.decodeDepthFun(state.depthState_i));
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f);
            depthStencil.maxDepthBounds(1.0f);
            depthStencil.stencilTestEnable(false);
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc((int)1, (MemoryStack)stack);
            colorBlendAttachment.colorWriteMask(state.colorMask_i);
            if (PipelineState.BlendState.enable(state.blendState_i)) {
                colorBlendAttachment.blendEnable(true);
                colorBlendAttachment.srcColorBlendFactor(PipelineState.BlendState.getSrcRgbFactor(state.blendState_i));
                colorBlendAttachment.dstColorBlendFactor(PipelineState.BlendState.getDstRgbFactor(state.blendState_i));
                colorBlendAttachment.colorBlendOp(PipelineState.BlendState.blendOp(state.blendState_i));
                colorBlendAttachment.srcAlphaBlendFactor(PipelineState.BlendState.getSrcAlphaFactor(state.blendState_i));
                colorBlendAttachment.dstAlphaBlendFactor(PipelineState.BlendState.getDstAlphaFactor(state.blendState_i));
                colorBlendAttachment.alphaBlendOp(PipelineState.BlendState.blendOp(state.blendState_i));
            } else {
                colorBlendAttachment.blendEnable(false);
            }
            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc((MemoryStack)stack);
            colorBlending.sType(26);
            colorBlending.logicOpEnable(PipelineState.LogicOpState.enable(state.logicOp_i));
            colorBlending.logicOp(PipelineState.LogicOpState.decodeFun(state.logicOp_i));
            colorBlending.pAttachments(colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            VkPipelineDynamicStateCreateInfo dynamicStates = VkPipelineDynamicStateCreateInfo.calloc((MemoryStack)stack);
            dynamicStates.sType(27);
            if (topology == 1 || polygonMode == 1) {
                dynamicStates.pDynamicStates(stack.ints(3, 0, 1, 2));
            } else {
                dynamicStates.pDynamicStates(stack.ints(3, 0, 1));
            }
            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc((int)1, (MemoryStack)stack);
            pipelineInfo.sType(28);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.pDynamicState(dynamicStates);
            pipelineInfo.layout(this.pipelineLayout);
            pipelineInfo.basePipelineHandle(0L);
            pipelineInfo.basePipelineIndex(-1);
            VkPipelineRenderingCreateInfoKHR renderingInfo = VkPipelineRenderingCreateInfoKHR.calloc((MemoryStack)stack);
            renderingInfo.sType(1000044002);
            Framebuffer framebuffer = state.renderPass != null ? state.renderPass.getFramebuffer() : Renderer.getInstance().getMainPass().getMainFramebuffer();
            renderingInfo.pColorAttachmentFormats(stack.ints(framebuffer.getFormat()));
            renderingInfo.depthAttachmentFormat(framebuffer.getDepthFormat());
            pipelineInfo.pNext(renderingInfo);
            LongBuffer pGraphicsPipeline = stack.mallocLong(1);
            Vulkan.checkResult(VK10.vkCreateGraphicsPipelines((VkDevice)DeviceManager.vkDevice, (long)PIPELINE_CACHE, (VkGraphicsPipelineCreateInfo.Buffer)pipelineInfo, null, (LongBuffer)pGraphicsPipeline), "Failed to create graphics pipeline " + this.name);
            long l = pGraphicsPipeline.get(0);
            return l;
        }
    }

    private void createShaderModules(SPIRVUtils.SPIRV vertSpirv, SPIRVUtils.SPIRV fragSpirv) {
        this.vertShaderModule = GraphicsPipeline.createShaderModule(vertSpirv.bytecode());
        this.fragShaderModule = GraphicsPipeline.createShaderModule(fragSpirv.bytecode());
    }

    @Override
    public void cleanUp() {
        VK10.vkDestroyShaderModule((VkDevice)DeviceManager.vkDevice, (long)this.vertShaderModule, null);
        VK10.vkDestroyShaderModule((VkDevice)DeviceManager.vkDevice, (long)this.fragShaderModule, null);
        this.vertexInputDescription.cleanUp();
        this.destroyDescriptorSets();
        this.graphicsPipelines.forEach((state, pipeline) -> VK10.vkDestroyPipeline((VkDevice)DeviceManager.vkDevice, (long)pipeline, null));
        this.graphicsPipelines.clear();
        VK10.vkDestroyDescriptorSetLayout((VkDevice)DeviceManager.vkDevice, (long)this.descriptorSetLayout, null);
        VK10.vkDestroyPipelineLayout((VkDevice)DeviceManager.vkDevice, (long)this.pipelineLayout, null);
        PIPELINES.remove(this);
        Renderer.getInstance().removeUsedPipeline(this);
    }

    private static VkVertexInputBindingDescription.Buffer getBindingDescription(VertexFormat vertexFormat) {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc((int)1);
        bindingDescription.binding(0);
        bindingDescription.stride(vertexFormat.getVertexSize());
        bindingDescription.inputRate(0);
        return bindingDescription;
    }

    private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(VertexFormat vertexFormat) {
        List elements = vertexFormat.getElements();
        int size = elements.size();
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc((int)size);
        int offset = 0;
        for (int i = 0; i < size; ++i) {
            VkVertexInputAttributeDescription posDescription = (VkVertexInputAttributeDescription)attributeDescriptions.get(i);
            posDescription.binding(0);
            posDescription.location(i);
            VertexFormatElement formatElement = (VertexFormatElement)elements.get(i);
            VertexFormatElement.Usage usage = formatElement.usage();
            VertexFormatElement.Type type = formatElement.type();
            int elementCount = formatElement.count();
            switch (usage) {
                case POSITION: {
                    switch (type) {
                        case FLOAT: {
                            posDescription.format(106);
                            posDescription.offset(offset);
                            offset += 12;
                            break;
                        }
                        case SHORT: {
                            posDescription.format(96);
                            posDescription.offset(offset);
                            offset += 8;
                            break;
                        }
                        case BYTE: {
                            posDescription.format(42);
                            posDescription.offset(offset);
                            offset += 4;
                        }
                    }
                    break;
                }
                case COLOR: {
                    switch (type) {
                        case UBYTE: {
                            posDescription.format(37);
                            posDescription.offset(offset);
                            offset += 4;
                            break;
                        }
                        case UINT: {
                            posDescription.format(98);
                            posDescription.offset(offset);
                            offset += 4;
                        }
                    }
                    break;
                }
                case UV: {
                    switch (type) {
                        case FLOAT: {
                            posDescription.format(103);
                            posDescription.offset(offset);
                            offset += 8;
                            break;
                        }
                        case SHORT: {
                            posDescription.format(82);
                            posDescription.offset(offset);
                            offset += 4;
                            break;
                        }
                        case USHORT: {
                            posDescription.format(81);
                            posDescription.offset(offset);
                            offset += 4;
                            break;
                        }
                        case UINT: {
                            posDescription.format(98);
                            posDescription.offset(offset);
                            offset += 4;
                        }
                    }
                    break;
                }
                case NORMAL: {
                    posDescription.format(38);
                    posDescription.offset(offset);
                    offset += 4;
                    break;
                }
                case GENERIC: {
                    if (type == VertexFormatElement.Type.SHORT && elementCount == 1) {
                        posDescription.format(75);
                        posDescription.offset(offset);
                        offset += 2;
                        break;
                    }
                    if (type == VertexFormatElement.Type.INT && elementCount == 1) {
                        posDescription.format(99);
                        posDescription.offset(offset);
                        offset += 4;
                        break;
                    }
                    if (type == VertexFormatElement.Type.FLOAT && elementCount == 1) {
                        posDescription.format(100);
                        posDescription.offset(offset);
                        offset += 4;
                        break;
                    }
                    throw new RuntimeException(String.format("Unknown type: %s", type));
                }
                default: {
                    throw new RuntimeException(String.format("Unknown format: %s", usage));
                }
            }
            posDescription.offset(((VertexFormatMixed)vertexFormat).getOffset(i));
        }
        return (VkVertexInputAttributeDescription.Buffer)attributeDescriptions.rewind();
    }

    static class VertexInputDescription {
        final VkVertexInputAttributeDescription.Buffer attributeDescriptions;
        final VkVertexInputBindingDescription.Buffer bindingDescriptions;

        VertexInputDescription(VertexFormat vertexFormat) {
            if (vertexFormat != class_290.field_60033) {
                this.bindingDescriptions = GraphicsPipeline.getBindingDescription(vertexFormat);
                this.attributeDescriptions = GraphicsPipeline.getAttributeDescriptions(vertexFormat);
            } else {
                this.bindingDescriptions = null;
                this.attributeDescriptions = null;
            }
        }

        void cleanUp() {
            if (this.bindingDescriptions != null) {
                MemoryUtil.memFree((CustomBuffer)this.bindingDescriptions);
                MemoryUtil.memFree((CustomBuffer)this.attributeDescriptions);
            }
        }
    }
}

