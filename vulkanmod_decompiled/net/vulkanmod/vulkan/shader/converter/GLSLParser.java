/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.vulkanmod.vulkan.shader.converter;

import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.shader.converter.Attribute;
import net.vulkanmod.vulkan.shader.converter.Lexer;
import net.vulkanmod.vulkan.shader.converter.Sampler;
import net.vulkanmod.vulkan.shader.converter.Token;
import net.vulkanmod.vulkan.shader.converter.UniformBlock;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.shader.layout.AlignedStruct;
import net.vulkanmod.vulkan.shader.layout.Uniform;

public class GLSLParser {
    private Lexer lexer;
    private List<Token> tokens;
    private int currentTokenIdx;
    private Token currentToken;
    private Stage stage;
    PreprocessorState preprocessorState = PreprocessorState.DEFAULT;
    State state = State.DEFAULT;
    LinkedList<Node> vsStream = new LinkedList();
    LinkedList<Node> fsStream = new LinkedList();
    Set<String> defines = new HashSet<String>();
    int currentUniformLocation = 0;
    List<UniformBlock> uniformBlocks = new ArrayList<UniformBlock>();
    Map<String, UniformBlock> uniformBlockMap = new HashMap<String, UniformBlock>();
    List<Sampler> samplers = new ArrayList<Sampler>();
    Map<String, Sampler> samplerMap = new HashMap<String, Sampler>();
    VertexFormat vertexFormat;
    int currentInAtt = 0;
    int currentOutAtt = 0;
    ArrayList<Attribute> vertInAttributes = new ArrayList();
    ArrayList<Attribute> vertOutAttributes = new ArrayList();
    ArrayList<Attribute> fragInAttributes = new ArrayList();
    ArrayList<Attribute> fragOutAttributes = new ArrayList();

    public void setVertexFormat(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
    }

    public void parse(Lexer lexer, Stage stage) {
        this.stage = stage;
        this.lexer = lexer;
        this.tokens = this.lexer.tokenize();
        this.currentTokenIdx = 0;
        this.currentInAtt = 0;
        this.currentOutAtt = 0;
        this.advanceToken();
        this.parseVersion();
        block20: while (this.currentToken.type != Token.TokenType.EOF) {
            switch (this.currentToken.type) {
                case PREPROCESSOR: {
                    this.parsePreprocessor();
                    break;
                }
                case COMMENT: {
                    this.appendToken(this.currentToken);
                    this.advanceToken();
                    continue block20;
                }
            }
            if (this.preprocessorState != PreprocessorState.IGNORE) {
                block4 : switch (this.currentToken.type) {
                    case PREPROCESSOR: {
                        this.parsePreprocessor();
                        break;
                    }
                    case IDENTIFIER: {
                        switch (this.currentToken.value) {
                            case "layout": {
                                this.parseUniformBlock();
                                break block4;
                            }
                            case "uniform": {
                                this.parseUniform();
                                break block4;
                            }
                            case "in": 
                            case "out": {
                                this.parseAttribute();
                                break block4;
                            }
                        }
                        this.appendToken(this.currentToken);
                        break;
                    }
                    case OPERATOR: {
                        this.appendToken(this.currentToken);
                        break;
                    }
                    default: {
                        this.appendToken(this.currentToken);
                        break;
                    }
                }
            } else {
                this.appendToken(this.currentToken);
            }
            this.advanceToken();
        }
    }

    private void parseVersion() {
        if (this.currentToken.type != Token.TokenType.PREPROCESSOR) {
            throw new IllegalStateException("First glsl line must contain #version");
        }
        this.advanceToken();
        if (!this.currentToken.value.startsWith("version")) {
            throw new IllegalStateException("First glsl line must contain #version");
        }
        this.advanceToken();
        while (!this.currentToken.value.contains("\n")) {
            this.advanceToken();
        }
        this.advanceToken();
        this.appendToken(new Token(Token.TokenType.PREPROCESSOR, "#version 450\n"));
    }

    private void parsePreprocessor() {
        int startTokenIdx = this.currentTokenIdx - 1;
        boolean appendTokens = true;
        this.advanceToken(true);
        switch (this.currentToken.value) {
            case "define": {
                this.advanceToken(true);
                this.defines.add(this.currentToken.value);
                break;
            }
            case "ifdef": {
                this.advanceToken(true);
                if (this.defines.contains(this.currentToken.value)) break;
                this.preprocessorState = PreprocessorState.IGNORE;
                break;
            }
            case "else": {
                if (this.preprocessorState != PreprocessorState.IGNORE) {
                    this.preprocessorState = PreprocessorState.IGNORE;
                    break;
                }
                this.preprocessorState = PreprocessorState.DEFAULT;
                break;
            }
            case "endif": {
                this.preprocessorState = PreprocessorState.DEFAULT;
                break;
            }
            case "line": {
                appendTokens = false;
            }
        }
        this.currentTokenIdx = startTokenIdx;
        ++this.currentTokenIdx;
        this.currentToken = this.tokens.get(startTokenIdx);
        do {
            if (appendTokens) {
                this.appendToken(new Token(Token.TokenType.PREPROCESSOR, this.currentToken.value));
            }
            this.advanceToken(false);
        } while (!this.currentToken.value.contains("\n"));
    }

    private void parseUniform() {
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
            throw new IllegalStateException();
        }
        switch (this.currentToken.value) {
            case "sampler2D": {
                this.parseSampler(Sampler.Type.SAMPLER_2D);
                break;
            }
            case "samplerCube": {
                this.parseSampler(Sampler.Type.SAMPLER_CUBE);
                break;
            }
            case "isamplerBuffer": {
                this.parseSampler(Sampler.Type.I_SAMPLER_BUFFER);
                break;
            }
            default: {
                throw new IllegalStateException("Unrecognized value: %s".formatted(this.currentToken.value));
            }
        }
    }

    private void parseSampler(Sampler.Type type) {
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
            throw new IllegalStateException();
        }
        String name = this.currentToken.value;
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.SEMICOLON) {
            throw new IllegalStateException();
        }
        Token next = this.tokens.get(this.currentTokenIdx);
        if (next.type == Token.TokenType.SPACING) {
            if (Objects.equals(next.value, "\n")) {
                ++this.currentTokenIdx;
            } else {
                int i = next.value.indexOf("\n");
                if (i >= 0) {
                    next.value = next.value.substring(i + 1);
                }
            }
        }
        Sampler sampler = new Sampler(type, name);
        if (this.samplerMap.get(name) != null) {
            sampler = this.samplerMap.get(name);
        } else {
            sampler.setBinding(this.currentUniformLocation++);
            this.samplerMap.put(name, sampler);
            this.samplers.add(sampler);
        }
        this.appendNode(sampler.getNode());
    }

    private void parseUniformBlock() {
        this.state = State.LAYOUT;
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.LEFT_PARENTHESIS) {
            throw new IllegalStateException();
        }
        do {
            this.advanceToken(true);
        } while (this.currentToken.type != Token.TokenType.RIGHT_PARENTHESIS);
        this.advanceToken(true);
        if (!Objects.equals(this.currentToken.value, "uniform")) {
            throw new IllegalStateException();
        }
        this.advanceToken(true);
        String name = this.currentToken.value;
        UniformBlock ub = new UniformBlock(name);
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.LEFT_BRACE) {
            throw new IllegalStateException();
        }
        this.advanceToken(true);
        while (this.currentToken.type != Token.TokenType.RIGHT_BRACE) {
            if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
                throw new IllegalStateException();
            }
            String fieldType = this.currentToken.value;
            this.advanceToken(true);
            if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
                throw new IllegalStateException();
            }
            String fieldName = this.currentToken.value;
            this.advanceToken(true);
            if (this.currentToken.type != Token.TokenType.SEMICOLON) {
                throw new IllegalStateException();
            }
            ub.addField(new UniformBlock.Field(fieldType, fieldName));
            this.advanceToken(true);
        }
        this.advanceToken(true);
        switch (this.currentToken.type) {
            case SEMICOLON: {
                break;
            }
            case IDENTIFIER: {
                ub.setAlias(this.currentToken.value);
                this.advanceToken(true);
                if (this.currentToken.type == Token.TokenType.SEMICOLON) break;
                throw new IllegalStateException();
            }
            default: {
                throw new IllegalStateException();
            }
        }
        Token next = this.tokens.get(this.currentTokenIdx);
        if (next.type == Token.TokenType.SPACING) {
            if (Objects.equals(next.value, "\n")) {
                ++this.currentTokenIdx;
            } else {
                int i = next.value.indexOf("\n");
                if (i >= 0) {
                    next.value = next.value.substring(i + 1);
                }
            }
        }
        if (this.uniformBlockMap.get(ub.name) != null) {
            ub = this.uniformBlockMap.get(ub.name);
        } else {
            ub.setBinding(this.currentUniformLocation++);
            this.uniformBlockMap.put(ub.name, ub);
            this.uniformBlocks.add(ub);
        }
        this.appendNode(ub.getNode());
    }

    private void parseAttribute() {
        this.state = State.ATTRIBUTE;
        String ioType = this.currentToken.value;
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
            throw new IllegalStateException();
        }
        String type = this.currentToken.value;
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.IDENTIFIER) {
            throw new IllegalStateException();
        }
        String id = this.currentToken.value;
        this.advanceToken(true);
        if (this.currentToken.type != Token.TokenType.SEMICOLON) {
            throw new IllegalStateException();
        }
        Token next = this.tokens.get(this.currentTokenIdx);
        if (next.type == Token.TokenType.SPACING) {
            if (Objects.equals(next.value, "\n")) {
                ++this.currentTokenIdx;
            } else {
                int i = next.value.indexOf("\n");
                if (i >= 0) {
                    next.value = next.value.substring(i + 1);
                }
            }
        }
        Attribute attribute = new Attribute(ioType, type, id);
        block0 : switch (this.stage.ordinal()) {
            case 0: {
                switch (attribute.ioType) {
                    case "in": {
                        int attributeLocation;
                        if (this.vertexFormat != null) {
                            List attributeNames = this.vertexFormat.getElementAttributeNames();
                            attributeLocation = attributeNames.indexOf(attribute.id);
                            if (attributeLocation == -1) {
                                Initializer.LOGGER.error("Element %s not found in elements %s".formatted(attribute.id, attributeNames));
                                attributeLocation = this.currentInAtt;
                            }
                            ++this.currentInAtt;
                        } else {
                            attributeLocation = this.currentInAtt++;
                        }
                        attribute.setLocation(attributeLocation);
                        this.vertInAttributes.add(attribute);
                        break block0;
                    }
                    case "out": {
                        attribute.setLocation(this.currentOutAtt++);
                        this.vertOutAttributes.add(attribute);
                        break block0;
                    }
                }
                throw new IllegalStateException();
            }
            case 1: {
                switch (attribute.ioType) {
                    case "in": {
                        Attribute vertAttribute = this.getVertAttribute(attribute);
                        if (vertAttribute != null) {
                            attribute.setLocation(vertAttribute.location);
                            this.fragInAttributes.add(attribute);
                            break block0;
                        }
                        return;
                    }
                    case "out": {
                        if (this.currentOutAtt > 0) {
                            throw new UnsupportedOperationException("Multiple outputs not currently supported.");
                        }
                        attribute.setLocation(this.currentOutAtt++);
                        this.fragOutAttributes.add(attribute);
                        break block0;
                    }
                }
                throw new IllegalStateException();
            }
        }
        this.appendNode(attribute.getNode());
    }

    private Attribute getVertAttribute(Attribute attribute) {
        Attribute vertAttribute = null;
        for (Attribute attribute1 : this.vertOutAttributes) {
            if (!Objects.equals(attribute1.id, attribute.id)) continue;
            vertAttribute = attribute1;
        }
        if (vertAttribute == null) {
            // empty if block
        }
        return vertAttribute;
    }

    private void advanceToken() {
        this.advanceToken(false);
    }

    private void advanceToken(boolean skipSpace) {
        this.currentToken = this.tokens.get(this.currentTokenIdx++);
        while (skipSpace && this.currentToken.type == Token.TokenType.SPACING) {
            this.currentToken = this.tokens.get(this.currentTokenIdx++);
        }
    }

    private Token nextToken(int i) {
        return this.tokens.get(this.currentTokenIdx + i);
    }

    private void appendToken(Token token) {
        this.appendNode(Node.fromToken(token));
    }

    private void appendNode(Node node) {
        switch (this.stage.ordinal()) {
            case 0: {
                this.vsStream.add(node);
                break;
            }
            case 1: {
                this.fsStream.add(node);
            }
        }
    }

    public String getOutput(Stage stage) {
        StringBuilder stringBuilder = new StringBuilder();
        LinkedList<Node> stream = switch (stage.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.vsStream;
            case 1 -> this.fsStream;
        };
        Node node = stream.getFirst();
        stringBuilder.append(node.value);
        stringBuilder.append("\n");
        switch (stage.ordinal()) {
            case 0: {
                stringBuilder.append("#define gl_VertexID gl_VertexIndex\n\n");
            }
        }
        stringBuilder.append("#define sampler sampler1\n\n");
        stringBuilder.append("#define sample sample1\n\n");
        for (int i = 1; i < stream.size(); ++i) {
            node = stream.get(i);
            stringBuilder.append(node.value);
        }
        return stringBuilder.toString();
    }

    public UBO[] createUBOs() {
        if (this.uniformBlockMap.isEmpty()) {
            return new UBO[0];
        }
        int uboCount = this.uniformBlockMap.size();
        UBO[] ubos = new UBO[uboCount];
        int i = 0;
        for (UniformBlock uniformBlock : this.uniformBlocks) {
            AlignedStruct.Builder builder = new AlignedStruct.Builder();
            for (UniformBlock.Field field : uniformBlock.fields) {
                String name = field.name;
                String type = field.type;
                Uniform.Info uniformInfo = Uniform.createUniformInfo(type, name);
                builder.addUniformInfo(uniformInfo);
            }
            ubos[i] = builder.buildUBO(uniformBlock.name, uniformBlock.binding, Integer.MAX_VALUE);
            ++i;
        }
        return ubos;
    }

    public List<ImageDescriptor> getSamplerList() {
        ObjectArrayList imageDescriptors = new ObjectArrayList();
        int imageIdx = 0;
        for (Sampler sampler : this.samplers) {
            int descriptorType = switch (sampler.type) {
                default -> throw new MatchException(null, null);
                case Sampler.Type.SAMPLER_2D, Sampler.Type.SAMPLER_CUBE -> 1;
                case Sampler.Type.I_SAMPLER_BUFFER -> 4;
            };
            imageDescriptors.add(new ImageDescriptor(sampler.binding, "sampler2D", sampler.id, imageIdx, descriptorType));
            ++imageIdx;
        }
        return imageDescriptors;
    }

    static enum PreprocessorState {
        IGNORE,
        DEFAULT;

    }

    static enum State {
        LAYOUT,
        UNIFORM,
        UNIFORM_BLOCK,
        ATTRIBUTE,
        DEFAULT;

    }

    public static enum Stage {
        VERTEX,
        FRAGMENT;

    }

    public static class Node {
        String type;
        String value;

        public Node(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public static Node fromToken(Token token) {
            return new Node("token:%s".formatted(new Object[]{token.type}), token.value);
        }

        public String toString() {
            return "Node{type='" + this.type + "', value='" + this.value + "'}";
        }
    }
}

