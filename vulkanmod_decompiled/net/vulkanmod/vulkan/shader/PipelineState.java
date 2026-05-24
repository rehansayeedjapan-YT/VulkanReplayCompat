/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader;

import java.util.Objects;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.framebuffer.RenderPass;

public class PipelineState {
    private static final int DEFAULT_DEPTH_OP = 515;
    public static BlendInfo blendInfo = PipelineState.defaultBlendInfo();
    public static final PipelineState DEFAULT;
    public static PipelineState currentState;
    final RenderPass renderPass;
    int assemblyRasterState;
    int blendState_i;
    int depthState_i;
    int colorMask_i;
    int logicOp_i;

    public static PipelineState getCurrentPipelineState(RenderPass renderPass) {
        int logicOp;
        int assemblyRasterState = PipelineState.getAssemblyRasterState();
        int blendState = PipelineState.getBlendState();
        int currentColorMask = VRenderSystem.getColorMask();
        int depthState = PipelineState.getDepthState();
        if (currentState.checkEquals(assemblyRasterState, blendState, depthState, logicOp = PipelineState.getLogicOpState(), currentColorMask, renderPass)) {
            return currentState;
        }
        currentState = new PipelineState(assemblyRasterState, blendState, depthState, logicOp, currentColorMask, renderPass);
        return currentState;
    }

    public static int getBlendState() {
        return BlendState.getState(blendInfo);
    }

    public static int getAssemblyRasterState() {
        return AssemblyRasterState.encode(VRenderSystem.cull, VRenderSystem.topology, VRenderSystem.polygonMode);
    }

    public static int getDepthState() {
        int depthState = 0;
        depthState |= VRenderSystem.depthTest ? 1 : 0;
        depthState |= VRenderSystem.depthMask ? 2 : 0;
        return depthState |= DepthState.encodeDepthFun(VRenderSystem.depthFun);
    }

    public static int getLogicOpState() {
        int logicOpState = 0;
        logicOpState |= VRenderSystem.logicOp ? 1 : 0;
        return logicOpState |= LogicOpState.encodeLogicOpFun(VRenderSystem.logicOpFun);
    }

    public PipelineState(int assemblyRasterState, int blendState, int depthState, int logicOp, int colorMask, RenderPass renderPass) {
        this.renderPass = renderPass;
        this.assemblyRasterState = assemblyRasterState;
        this.blendState_i = blendState;
        this.depthState_i = depthState;
        this.colorMask_i = colorMask;
        this.logicOp_i = logicOp;
    }

    private boolean checkEquals(int assemblyRasterState, int blendState, int depthState, int logicOp, int colorMask, RenderPass renderPass) {
        return blendState == this.blendState_i && depthState == this.depthState_i && renderPass == this.renderPass && logicOp == this.logicOp_i && assemblyRasterState == this.assemblyRasterState && colorMask == this.colorMask_i;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PipelineState that = (PipelineState)o;
        return this.blendState_i == that.blendState_i && this.depthState_i == that.depthState_i && this.renderPass == that.renderPass && this.logicOp_i == that.logicOp_i && this.assemblyRasterState == that.assemblyRasterState && this.colorMask_i == that.colorMask_i;
    }

    public int hashCode() {
        return Objects.hash(this.blendState_i, this.depthState_i, this.logicOp_i, this.assemblyRasterState, this.colorMask_i, this.renderPass);
    }

    public static BlendInfo defaultBlendInfo() {
        return new BlendInfo(true, 6, 7, 1, 0, 0);
    }

    static {
        currentState = DEFAULT = new PipelineState(PipelineState.getAssemblyRasterState(), PipelineState.getBlendState(), PipelineState.getDepthState(), PipelineState.getLogicOpState(), VRenderSystem.getColorMask(), null);
    }

    public static class BlendInfo {
        public boolean enabled;
        public int srcRgbFactor;
        public int dstRgbFactor;
        public int srcAlphaFactor;
        public int dstAlphaFactor;
        public int blendOp;

        public BlendInfo(boolean enabled, int srcRgbFactor, int dstRgbFactor, int srcAlphaFactor, int dstAlphaFactor, int blendOp) {
            this.enabled = enabled;
            this.srcRgbFactor = srcRgbFactor;
            this.dstRgbFactor = dstRgbFactor;
            this.srcAlphaFactor = srcAlphaFactor;
            this.dstAlphaFactor = dstAlphaFactor;
            this.blendOp = blendOp;
        }

        public void setBlendFunction(int sourceFactor, int destFactor) {
            this.srcRgbFactor = BlendInfo.glToVulkanBlendFactor(sourceFactor);
            this.srcAlphaFactor = BlendInfo.glToVulkanBlendFactor(sourceFactor);
            this.dstRgbFactor = BlendInfo.glToVulkanBlendFactor(destFactor);
            this.dstAlphaFactor = BlendInfo.glToVulkanBlendFactor(destFactor);
        }

        public void setBlendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
            this.srcRgbFactor = BlendInfo.glToVulkanBlendFactor(srcRgb);
            this.srcAlphaFactor = BlendInfo.glToVulkanBlendFactor(srcAlpha);
            this.dstRgbFactor = BlendInfo.glToVulkanBlendFactor(dstRgb);
            this.dstAlphaFactor = BlendInfo.glToVulkanBlendFactor(dstAlpha);
        }

        public void setBlendOp(int i) {
            this.blendOp = BlendInfo.glToVulkanBlendOp(i);
        }

        public int createBlendState() {
            return BlendState.getState(this);
        }

        private static int glToVulkanBlendOp(int value) {
            return switch (value) {
                case 32774 -> 0;
                case 32775 -> 3;
                case 32776 -> 4;
                case 32778 -> 1;
                case 32779 -> 2;
                default -> throw new RuntimeException("unknown blend factor: " + value);
            };
        }

        private static int glToVulkanBlendFactor(int value) {
            return switch (value) {
                case 1 -> 1;
                case 0 -> 0;
                case 771 -> 7;
                case 770 -> 6;
                case 775 -> 5;
                case 769 -> 3;
                case 774 -> 4;
                case 768 -> 2;
                default -> throw new RuntimeException("unknown blend factor: " + value);
            };
        }
    }

    public static class BlendState {
        public static final int SRC_RGB_OFFSET = 0;
        public static final int DST_RGB_OFFSET = 5;
        public static final int SRC_A_OFFSET = 10;
        public static final int DST_A_OFFSET = 15;
        public static final int FUN_OFFSET = 20;
        public static final int ENABLE_BIT = 0x1000000;
        public static final int OP_MASK = 15;
        public static final int FACTOR_MASK = 31;

        public static int getState(BlendInfo blendInfo) {
            int s = 0;
            s |= blendInfo.enabled ? 0x1000000 : 0;
            s |= BlendState.encode(blendInfo.srcRgbFactor, 0, 31);
            s |= BlendState.encode(blendInfo.dstRgbFactor, 5, 31);
            s |= BlendState.encode(blendInfo.srcAlphaFactor, 10, 31);
            s |= BlendState.encode(blendInfo.dstAlphaFactor, 15, 31);
            return s |= BlendState.encode(blendInfo.blendOp, 20, 15);
        }

        public static boolean enable(int i) {
            return (i & 0x1000000) != 0;
        }

        public static int encode(int i, int offset, int mask) {
            return (i & mask) << offset;
        }

        public static int decode(int i, int offset, int bits) {
            return i >>> offset & bits;
        }

        public static int getSrcRgbFactor(int s) {
            return BlendState.decode(s, 0, 31);
        }

        public static int getDstRgbFactor(int s) {
            return BlendState.decode(s, 5, 31);
        }

        public static int getSrcAlphaFactor(int s) {
            return BlendState.decode(s, 10, 31);
        }

        public static int getDstAlphaFactor(int s) {
            return BlendState.decode(s, 15, 31);
        }

        public static int blendOp(int state) {
            return BlendState.decode(state, 20, 15);
        }
    }

    public static abstract class AssemblyRasterState {
        public static final int POLYGON_MODE_MASK = 7;
        public static final int TOPOLOGY_OFFSET = 3;
        public static final int TOPOLOGY_BITS = 4;
        public static final int TOPOLOGY_MASK = 31;
        public static final int CULL_MODE_OFFSET = 7;
        public static final int CULL_MODE_BITS = 2;
        public static final int CULL_MODE_MASK = 3;

        public static int encode(boolean cull, int topology, int polygonMode) {
            int state = polygonMode | topology << 3;
            return state |= (cull ? 2 : 0) << 7;
        }

        public static int decodeTopology(int state) {
            return state >>> 3 & 0x1F;
        }

        public static int decodePolygonMode(int state) {
            return state & 7;
        }

        public static int decodeCullMode(int state) {
            return state >>> 7 & 3;
        }
    }

    public static abstract class DepthState {
        public static final int DEPTH_TEST_BIT = 1;
        public static final int DEPTH_MASK_BIT = 2;
        public static final int DEPTH_FUN_OFFSET = 2;
        public static final int DEPTH_FUN_BITS = 4;

        public static boolean depthTest(int i) {
            return (i & 1) != 0;
        }

        public static boolean depthMask(int i) {
            return (i & 2) != 0;
        }

        public static int encodeDepthFun(int glFun) {
            int fun = DepthState.glToVulkan(glFun);
            return fun << 2;
        }

        public static int decodeDepthFun(int state) {
            return state >>> 2;
        }

        private static int glToVulkan(int value) {
            return switch (value) {
                case 515 -> 3;
                case 519 -> 7;
                case 516 -> 4;
                case 518 -> 6;
                case 514 -> 2;
                default -> throw new RuntimeException("unknown blend factor..");
            };
        }
    }

    public static abstract class LogicOpState {
        public static final int ENABLE_BIT = 1;
        public static final int FUN_OFFSET = 1;
        public static final int FUN_BITS = 5;

        public static boolean enable(int i) {
            return (i & 1) != 0;
        }

        public static int encodeLogicOpFun(int glFun) {
            int fun = LogicOpState.glToVulkan(glFun);
            return fun << 1;
        }

        public static int decodeFun(int state) {
            return state >>> 1;
        }

        public static int glToVulkan(int f) {
            return switch (f) {
                case 5387 -> 11;
                default -> 1;
            };
        }
    }

    public static abstract class ColorMask {
        public static int getColorMask(boolean r, boolean g, boolean b, boolean a) {
            return (r ? 1 : 0) | (g ? 2 : 0) | (b ? 4 : 0) | (a ? 8 : 0);
        }
    }
}

