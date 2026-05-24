/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.converter;

import net.vulkanmod.vulkan.shader.converter.GLSLParser;

public class Sampler {
    final Type type;
    final String id;
    int binding;

    public Sampler(Type type, String id) {
        this.type = type;
        this.id = id;
    }

    public void setBinding(int binding) {
        this.binding = binding;
    }

    public GLSLParser.Node getNode() {
        return new GLSLParser.Node("sampler", "layout(binding = %d) uniform %s %s;\n".formatted(this.binding, this.type.name, this.id));
    }

    public static enum Type {
        SAMPLER_2D("sampler2D"),
        SAMPLER_CUBE("samplerCube"),
        I_SAMPLER_BUFFER("isamplerBuffer");

        public final String name;

        private Type(String name) {
            this.name = name;
        }
    }
}

