/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_10219
 *  net.minecraft.class_1041
 *  net.minecraft.class_323
 *  net.minecraft.class_3678
 *  net.minecraft.class_543
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.window;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10219;
import net.minecraft.class_1041;
import net.minecraft.class_323;
import net.minecraft.class_3678;
import net.minecraft.class_543;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1041.class})
public abstract class WindowMixin {
    @Final
    @Shadow
    private long field_5187;
    @Shadow
    private boolean field_16517;
    @Shadow
    private boolean field_5191;
    @Shadow
    @Final
    private static Logger field_5178;
    @Shadow
    private int field_5175;
    @Shadow
    private int field_5185;
    @Shadow
    private int field_5174;
    @Shadow
    private int field_5184;
    @Shadow
    private int field_5183;
    @Shadow
    private int field_5198;
    @Shadow
    private int field_5182;
    @Shadow
    private int field_5197;
    @Shadow
    private int field_5181;
    @Shadow
    private int field_5196;
    @Unique
    private boolean wasOnFullscreen = false;

    @Shadow
    public abstract int method_4489();

    @Shadow
    public abstract int method_4506();

    @Shadow
    protected abstract void method_4485(boolean var1, @Nullable class_10219 var2);

    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"))
    private void redirect(int hint, int value) {
    }

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J")})
    private void vulkanHint(class_3678 windowEventHandler, class_323 screenManager, class_543 displayData, String string, String string2, CallbackInfo ci) {
        GLFW.glfwWindowHint((int)139265, (int)0);
        boolean b = Platform.isGnome() | Platform.isWeston() | Platform.isGeneric() && Platform.isWayLand();
        GLFW.glfwWindowHint((int)131077, (int)(b ? 0 : 1));
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void getHandle(class_3678 windowEventHandler, class_323 screenManager, class_543 displayData, String string, String string2, CallbackInfo ci) {
        VRenderSystem.setWindow(this.field_5187);
    }

    @Overwrite
    public void method_4497(boolean vsync) {
        this.field_16517 = vsync;
        Vulkan.setVsync(vsync);
    }

    @Overwrite
    public void method_4500() {
        this.field_5191 = !this.field_5191;
        Options.fullscreenDirty = true;
        if (!this.field_5191) {
            Config config = Initializer.CONFIG;
            config.windowMode = WindowMode.WINDOWED.mode;
        }
    }

    @Overwrite
    public void method_15998(@Nullable class_10219 tracyFrameCapture) {
        RenderSystem.flipFrame((class_1041)((class_1041)this), (class_10219)tracyFrameCapture);
        if (Options.fullscreenDirty) {
            Options.fullscreenDirty = false;
            this.method_4485(this.field_16517, tracyFrameCapture);
        }
    }

    @Overwrite
    private void method_4479() {
        Config config = Initializer.CONFIG;
        if (this.field_5191) {
            config.windowMode = WindowMode.EXCLUSIVE_FULLSCREEN.mode;
        }
        if (this.field_5191) {
            VideoModeManager.selectBestMonitor((class_1041)this);
            long monitor = VideoModeManager.selectedMonitor;
            VideoModeSet.VideoMode videoMode = config.videoMode;
            VideoModeSet set = VideoModeManager.getVideoModeSet(videoMode);
            boolean supported = set != null ? set.hasRefreshRate(videoMode.refreshRate) : false;
            if (!supported) {
                field_5178.error("Resolution not supported, using first available as fallback");
                videoMode = VideoModeManager.getFirstAvailable().getVideoMode();
            }
            if (!this.wasOnFullscreen) {
                this.field_5175 = this.field_5183;
                this.field_5185 = this.field_5198;
                this.field_5174 = this.field_5182;
                this.field_5184 = this.field_5197;
            }
            this.field_5183 = 0;
            this.field_5198 = 0;
            this.field_5182 = videoMode.width;
            this.field_5197 = videoMode.height;
            GLFW.glfwSetWindowMonitor((long)this.field_5187, (long)monitor, (int)this.field_5183, (int)this.field_5198, (int)this.field_5182, (int)this.field_5197, (int)videoMode.refreshRate);
            this.wasOnFullscreen = true;
        } else if (config.windowMode == WindowMode.WINDOWED_FULLSCREEN.mode) {
            VideoModeManager.selectBestMonitor((class_1041)this);
            VideoModeSet.VideoMode videoMode = VideoModeManager.getOsVideoMode();
            if (!this.wasOnFullscreen) {
                this.field_5175 = this.field_5183;
                this.field_5185 = this.field_5198;
                this.field_5174 = this.field_5182;
                this.field_5184 = this.field_5197;
            }
            int width = videoMode.width;
            int height = videoMode.height;
            GLFW.glfwSetWindowAttrib((long)this.field_5187, (int)131077, (int)0);
            GLFW.glfwSetWindowMonitor((long)this.field_5187, (long)0L, (int)0, (int)0, (int)width, (int)height, (int)-1);
            this.field_5182 = width;
            this.field_5197 = height;
            this.wasOnFullscreen = true;
        } else {
            this.field_5183 = this.field_5175;
            this.field_5198 = this.field_5185;
            this.field_5182 = this.field_5174;
            this.field_5197 = this.field_5184;
            GLFW.glfwSetWindowMonitor((long)this.field_5187, (long)0L, (int)this.field_5183, (int)this.field_5198, (int)this.field_5182, (int)this.field_5197, (int)-1);
            GLFW.glfwSetWindowAttrib((long)this.field_5187, (int)131077, (int)1);
            this.wasOnFullscreen = false;
        }
    }

    @Overwrite
    private void method_4504(long window, int width, int height) {
        if (window == this.field_5187) {
            int prevWidth = this.method_4489();
            int prevHeight = this.method_4506();
            if (width > 0 && height > 0) {
                this.field_5181 = width;
                this.field_5196 = height;
                Renderer.scheduleSwapChainUpdate();
            }
        }
    }

    @Overwrite
    private void method_4488(long window, int width, int height) {
        this.field_5182 = width;
        this.field_5197 = height;
        if (width > 0 && height > 0) {
            Renderer.scheduleSwapChainUpdate();
        }
    }
}

