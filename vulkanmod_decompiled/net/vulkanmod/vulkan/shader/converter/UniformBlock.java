/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.converter;

import java.util.ArrayList;
import java.util.List;
import net.vulkanmod.vulkan.shader.converter.GLSLParser;

public class UniformBlock {
    int binding;
    String name;
    String alias;
    List<Field> fields = new ArrayList<Field>();

    public UniformBlock(String name) {
        this.name = name;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public void setBinding(int binding) {
        this.binding = binding;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public GLSLParser.Node getNode() {
        StringBuilder sb = new StringBuilder();
        sb.append("layout(binding = %d) uniform %s {\n".formatted(this.binding, this.name));
        int fieldsSize = this.fields.size();
        for (int i = 0; i < fieldsSize; ++i) {
            Field field = this.fields.get(i);
            sb.append("\t%s %s;".formatted(field.type, field.name));
            if (i >= fieldsSize - 1) continue;
            sb.append("\n");
        }
        sb.append("\n}");
        if (this.alias != null) {
            sb.append(" %s ".formatted(this.alias));
        }
        sb.append(";\n");
        return new GLSLParser.Node("uniform_block", sb.toString());
    }

    public static class Field {
        final String type;
        final String name;

        public Field(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}

