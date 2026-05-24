/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.shaders.ShaderType
 *  net.minecraft.class_2960
 *  org.apache.commons.io.IOUtils
 */
package net.vulkanmod.render.shader;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.shaders.ShaderType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import net.minecraft.class_2960;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import org.apache.commons.io.IOUtils;

public abstract class ShaderLoadUtil {
    public static final String RESOURCES_PATH = SPIRVUtils.class.getResource("/assets/vulkanmod").toExternalForm();
    public static final String SHADERS_PATH = "%s/shaders/".formatted(RESOURCES_PATH);
    public static final Set<String> REMAPPED_SHADERS = Sets.newHashSet((Object[])new String[]{"core/screenquad.vsh", "core/rendertype_item_entity_translucent_cull.vsh", "core/animate_sprite.vsh", "core/animate_sprite_blit.fsh"});

    public static String resolveShaderPath(String path) {
        return ShaderLoadUtil.resolveShaderPath(SHADERS_PATH, path);
    }

    public static String resolveShaderPath(String shaderPath, String path) {
        return "%s%s".formatted(shaderPath, path);
    }

    public static void loadShaders(Pipeline.Builder pipelineBuilder, JsonObject config, String configName, String path) {
        String fragmentShader;
        String vertexShader = config.has("vertex") ? config.get("vertex").getAsString() : configName;
        String string = fragmentShader = config.has("fragment") ? config.get("fragment").getAsString() : configName;
        if (vertexShader == null) {
            vertexShader = configName;
        }
        if (fragmentShader == null) {
            fragmentShader = configName;
        }
        vertexShader = ShaderLoadUtil.removeNameSpace(vertexShader);
        fragmentShader = ShaderLoadUtil.removeNameSpace(fragmentShader);
        vertexShader = ShaderLoadUtil.getFileName(vertexShader);
        fragmentShader = ShaderLoadUtil.getFileName(fragmentShader);
        ShaderLoadUtil.loadShader(pipelineBuilder, configName, path, vertexShader, SPIRVUtils.ShaderKind.VERTEX_SHADER);
        ShaderLoadUtil.loadShader(pipelineBuilder, configName, path, fragmentShader, SPIRVUtils.ShaderKind.FRAGMENT_SHADER);
    }

    public static void loadShader(Pipeline.Builder pipelineBuilder, String configName, String path, SPIRVUtils.ShaderKind type) {
        String[] splitPath = ShaderLoadUtil.splitPath(path);
        String shaderName = splitPath[1];
        String subPath = splitPath[0];
        ShaderLoadUtil.loadShader(pipelineBuilder, configName, subPath, shaderName, type);
    }

    public static void loadShader(Pipeline.Builder pipelineBuilder, String configName, String path, String shaderName, SPIRVUtils.ShaderKind type) {
        String source = ShaderLoadUtil.getShaderSource(path, configName, shaderName, type);
        SPIRVUtils.SPIRV spirv = SPIRVUtils.compileShader(shaderName, source, type);
        switch (type) {
            case VERTEX_SHADER: {
                pipelineBuilder.setVertShaderSPIRV(spirv);
                break;
            }
            case FRAGMENT_SHADER: {
                pipelineBuilder.setFragShaderSPIRV(spirv);
            }
        }
    }

    public static String getConfigFilePath(String path, String rendertype) {
        Path filePath;
        String basePath = "%s/shaders/%s".formatted(RESOURCES_PATH, path);
        String configPath = "%s/%s/%s.json".formatted(basePath, rendertype, rendertype);
        try {
            filePath = FileSystems.getDefault().getPath(configPath, new String[0]);
            if (!Files.exists(filePath, new LinkOption[0])) {
                configPath = "%s/%s.json".formatted(basePath, rendertype);
                filePath = FileSystems.getDefault().getPath(configPath, new String[0]);
            }
            if (!Files.exists(filePath, new LinkOption[0])) {
                return null;
            }
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return filePath.toString();
    }

    public static JsonObject getJsonConfig(String path, String rendertype) {
        if (rendertype.contains(String.valueOf(':'))) {
            return null;
        }
        String basePath = path;
        String configPath = "%s/%s/%s.json".formatted(basePath, rendertype, rendertype);
        try {
            InputStream stream = ShaderLoadUtil.getInputStream(configPath);
            if (stream == null) {
                configPath = "%s/%s.json".formatted(basePath, rendertype);
                stream = ShaderLoadUtil.getInputStream(configPath);
            }
            if (stream == null) {
                return null;
            }
            JsonElement jsonElement = JsonParser.parseReader((Reader)new BufferedReader(new InputStreamReader(stream)));
            stream.close();
            return (JsonObject)jsonElement;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getShaderSource(class_2960 resourceLocation, ShaderType type) {
        String shaderExtension = switch (type) {
            default -> throw new MatchException(null, null);
            case ShaderType.VERTEX -> ".vsh";
            case ShaderType.FRAGMENT -> ".fsh";
        };
        String path = resourceLocation.method_12832();
        String[] splitPath = ShaderLoadUtil.splitPath(path);
        String shaderName = "%s%s".formatted(splitPath[1], shaderExtension);
        String shaderFile = "%s/shaders/%s/%s".formatted(RESOURCES_PATH, path, shaderName);
        try {
            InputStream stream = ShaderLoadUtil.getInputStream(shaderFile);
            if (stream == null) {
                shaderFile = "%s/shaders/%s%s".formatted(RESOURCES_PATH, path, shaderExtension);
                stream = ShaderLoadUtil.getInputStream(shaderFile);
            }
            if (stream == null) {
                return null;
            }
            String source = IOUtils.toString((Reader)new BufferedReader(new InputStreamReader(stream)));
            stream.close();
            return source;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getShaderSource(String path, ShaderType type) {
        String shaderExtension = switch (type) {
            default -> throw new MatchException(null, null);
            case ShaderType.VERTEX -> ".vsh";
            case ShaderType.FRAGMENT -> ".fsh";
        };
        String[] splitPath = ShaderLoadUtil.splitPath(path);
        String shaderName = "%s%s".formatted(splitPath[1], shaderExtension);
        String shaderFile = "%s/shaders/%s/%s".formatted(RESOURCES_PATH, path, shaderName);
        try {
            InputStream stream = ShaderLoadUtil.getInputStream(shaderFile);
            String source = IOUtils.toString((Reader)new BufferedReader(new InputStreamReader(stream)));
            stream.close();
            return source;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getShaderSource(String path, String configName, String shaderName, SPIRVUtils.ShaderKind type) {
        String shaderExtension = switch (type) {
            case SPIRVUtils.ShaderKind.VERTEX_SHADER -> ".vsh";
            case SPIRVUtils.ShaderKind.FRAGMENT_SHADER -> ".fsh";
            case SPIRVUtils.ShaderKind.COMPUTE_SHADER -> ".comp";
            default -> throw new UnsupportedOperationException("shader type %s unsupported");
        };
        String basePath = path;
        String shaderPath = "/%s/%s".formatted(configName, configName);
        String shaderFile = "%s%s%s".formatted(basePath, shaderPath, shaderExtension);
        try {
            InputStream stream = ShaderLoadUtil.getInputStream(shaderFile);
            if (stream == null) {
                shaderPath = "/%s".formatted(shaderName);
                shaderFile = "%s%s%s".formatted(basePath, shaderPath, shaderExtension);
                stream = ShaderLoadUtil.getInputStream(shaderFile);
            }
            if (stream == null) {
                shaderPath = "/%s/%s".formatted(configName, shaderName);
                shaderFile = "%s%s%s".formatted(basePath, shaderPath, shaderExtension);
                stream = ShaderLoadUtil.getInputStream(shaderFile);
            }
            if (stream == null) {
                shaderPath = "/%s/%s".formatted(shaderName, shaderName);
                shaderFile = "%s%s%s".formatted(basePath, shaderPath, shaderExtension);
                stream = ShaderLoadUtil.getInputStream(shaderFile);
            }
            if (stream == null) {
                return null;
            }
            String source = IOUtils.toString((Reader)new BufferedReader(new InputStreamReader(stream)));
            stream.close();
            return source;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileName(String path) {
        int idx = path.lastIndexOf(47);
        return idx > -1 ? path.substring(idx + 1) : path;
    }

    public static String removeNameSpace(String path) {
        int idx = path.indexOf(58);
        return idx > -1 ? path.substring(idx + 1) : path;
    }

    public static String[] splitPath(String path) {
        int idx = path.lastIndexOf(47);
        return new String[]{path.substring(0, idx), path.substring(idx + 1)};
    }

    public static InputStream getInputStream(String path) {
        try {
            Path path1 = Paths.get(new URI(path));
            if (!Files.exists(path1, new LinkOption[0])) {
                return null;
            }
            return Files.newInputStream(path1, new OpenOption[0]);
        }
        catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

