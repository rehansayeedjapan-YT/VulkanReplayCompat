/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.converter;

import net.vulkanmod.vulkan.shader.converter.GLSLParser;

public class Attribute {
    String ioType;
    String type;
    String id;
    int location;

    public Attribute(String ioType, String type, String id) {
        switch (ioType) {
            case "in": 
            case "out": {
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        this.ioType = ioType;
        this.type = type;
        this.id = id;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public GLSLParser.Node getNode() {
        return new GLSLParser.Node("attribute", "layout(location = %d) %s %s %s;\n".formatted(this.location, this.ioType, this.type, this.id));
    }
}

