/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.shader.layout;

import java.util.function.Supplier;
import net.vulkanmod.vulkan.shader.Uniforms;
import net.vulkanmod.vulkan.shader.layout.Mat3;
import net.vulkanmod.vulkan.shader.layout.Vec1f;
import net.vulkanmod.vulkan.shader.layout.Vec1i;
import net.vulkanmod.vulkan.util.MappedBuffer;
import org.lwjgl.system.MemoryUtil;

public class Uniform {
    protected Supplier<MappedBuffer> values;
    Info info;
    protected long offset;
    protected int size;

    Uniform(Info info) {
        this.info = info;
        this.offset = (long)info.offset * 4L;
        this.size = info.size * 4;
        this.setupSupplier();
    }

    protected void setupSupplier() {
        this.values = this.info.bufferSupplier;
    }

    public void setSupplier(Supplier<MappedBuffer> supplier) {
        this.values = supplier;
    }

    public String getName() {
        return this.info.name;
    }

    void update(long ptr) {
        if (this.values == null) {
            return;
        }
        MappedBuffer src = this.values.get();
        MemoryUtil.memCopy((long)src.ptr, (long)(ptr + this.offset), (long)this.size);
    }

    public static Uniform createField(Info info) {
        return switch (info.type) {
            case "mat4", "vec3", "vec4", "vec2", "ivec3", "ivec2" -> new Uniform(info);
            case "mat3" -> new Mat3(info);
            case "float" -> new Vec1f(info);
            case "int" -> new Vec1i(info);
            default -> throw new RuntimeException("not admitted type: " + info.type);
        };
    }

    public int getOffset() {
        return this.info.offset;
    }

    public int getSize() {
        return this.info.size;
    }

    public Info getInfo() {
        return this.info;
    }

    public String toString() {
        return String.format("%s: %s offset: %d", this.info.type, this.info.name, this.info.offset);
    }

    public static Info createUniformInfo(String type, String name, int count) {
        return switch (type) {
            case "matrix4x4" -> new Info("mat4", name, 4, 16);
            case "float" -> {
                switch (count) {
                    case 4: {
                        yield new Info("vec4", name, 4, 4);
                    }
                    case 3: {
                        yield new Info("vec3", name, 4, 3);
                    }
                    case 2: {
                        yield new Info("vec2", name, 2, 2);
                    }
                    case 1: {
                        yield new Info("float", name, 1, 1);
                    }
                }
                throw new IllegalStateException("Unexpected value: " + count);
            }
            case "int" -> {
                switch (count) {
                    case 4: {
                        yield new Info("ivec4", name, 4, 4);
                    }
                    case 3: {
                        yield new Info("ivec3", name, 4, 3);
                    }
                    case 2: {
                        yield new Info("ivec2", name, 2, 2);
                    }
                    case 1: {
                        yield new Info("int", name, 1, 1);
                    }
                }
                throw new IllegalStateException("Unexpected value: " + count);
            }
            default -> throw new RuntimeException("not admitted type..");
        };
    }

    public static Info createUniformInfo(String type, String name) {
        return switch (type) {
            case "mat4" -> new Info(type, name, 4, 16);
            case "mat3" -> new Info(type, name, 4, 9);
            case "vec4" -> new Info(type, name, 4, 4);
            case "vec3", "ivec3" -> new Info(type, name, 4, 3);
            case "vec2", "ivec2" -> new Info(type, name, 2, 2);
            case "float", "int" -> new Info(type, name, 1, 1);
            default -> throw new RuntimeException("not admitted type: " + type);
        };
    }

    public static class Info {
        public final String type;
        public final String name;
        public final int align;
        public final int size;
        int offset;
        Supplier<MappedBuffer> bufferSupplier;
        Supplier<Integer> intSupplier;
        Supplier<Float> floatSupplier;

        Info(String type, String name, int align, int size) {
            this.type = type;
            this.name = name;
            this.align = align;
            this.size = size;
        }

        int getSizeBytes() {
            return 4 * this.size;
        }

        int computeAlignmentOffset(int builderOffset) {
            this.offset = builderOffset + (this.align - builderOffset % this.align) % this.align;
            return this.offset;
        }

        public void setupSupplier() {
            switch (this.type) {
                case "float": {
                    this.floatSupplier = (Supplier)Uniforms.vec1f_uniformMap.get((Object)this.name);
                    break;
                }
                case "int": {
                    this.intSupplier = (Supplier)Uniforms.vec1i_uniformMap.get((Object)this.name);
                    break;
                }
                default: {
                    this.bufferSupplier = Uniforms.getUniformSupplier(this.type, this.name);
                }
            }
        }

        public boolean hasSupplier() {
            return switch (this.type) {
                case "float" -> {
                    if (this.floatSupplier != null || this.bufferSupplier != null) {
                        yield true;
                    }
                    yield false;
                }
                case "int" -> {
                    if (this.intSupplier != null || this.bufferSupplier != null) {
                        yield true;
                    }
                    yield false;
                }
                default -> this.bufferSupplier != null;
            };
        }

        public void setBufferSupplier(Supplier<MappedBuffer> supplier) {
            this.bufferSupplier = supplier;
        }
    }
}

