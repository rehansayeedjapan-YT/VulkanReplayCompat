/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.NativeResource
 *  org.lwjgl.util.shaderc.Shaderc
 *  org.lwjgl.util.shaderc.ShadercIncludeResolveI
 *  org.lwjgl.util.shaderc.ShadercIncludeResult
 *  org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI
 *  org.lwjgl.vulkan.VK12
 */
package net.vulkanmod.vulkan.shader;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;
import org.lwjgl.vulkan.VK12;

public class SPIRVUtils {
    private static final boolean DEBUG = true;
    private static final boolean OPTIMIZATIONS = false;
    private static long compiler;
    private static long options;
    private static final ShaderIncluder SHADER_INCLUDER;
    private static final ShaderReleaser SHADER_RELEASER;
    private static final long pUserData = 0L;
    private static ObjectArrayList<String> includePaths;

    private static void initCompiler() {
        compiler = Shaderc.shaderc_compiler_initialize();
        if (compiler == 0L) {
            throw new RuntimeException("Failed to create shader compiler");
        }
        options = Shaderc.shaderc_compile_options_initialize();
        if (options == 0L) {
            throw new RuntimeException("Failed to create compiler options");
        }
        Shaderc.shaderc_compile_options_set_generate_debug_info((long)options);
        Shaderc.shaderc_compile_options_set_target_env((long)options, (int)0x402000, (int)VK12.VK_API_VERSION_1_2);
        Shaderc.shaderc_compile_options_set_include_callbacks((long)options, (ShadercIncludeResolveI)SHADER_INCLUDER, (ShadercIncludeResultReleaseI)SHADER_RELEASER, (long)0L);
        includePaths = new ObjectArrayList();
        SPIRVUtils.addIncludePath("/assets/vulkanmod/shaders/include/");
    }

    public static void addIncludePath(String path) {
        URL url = SPIRVUtils.class.getResource(path);
        if (url != null) {
            includePaths.add((Object)url.toExternalForm());
        }
    }

    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {
        if (source == null) {
            throw new NullPointerException("source for %s.%s is null".formatted(new Object[]{filename, shaderKind}));
        }
        long result = Shaderc.shaderc_compile_into_spv((long)compiler, (CharSequence)source, (int)shaderKind.kind, (CharSequence)filename, (CharSequence)"main", (long)options);
        if (result == 0L) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }
        if (Shaderc.shaderc_result_get_compilation_status((long)result) != 0) {
            String errorMessage = Shaderc.shaderc_result_get_error_message((long)result);
            throw new RuntimeException("Failed to compile shader %s into SPIR-V:\n\t%s".formatted(filename, errorMessage));
        }
        return new SPIRV(result, Shaderc.shaderc_result_get_bytes((long)result));
    }

    static {
        SHADER_INCLUDER = new ShaderIncluder();
        SHADER_RELEASER = new ShaderReleaser();
        SPIRVUtils.initCompiler();
    }

    private static class ShaderIncluder
    implements ShadercIncludeResolveI {
        private static final int MAX_PATH_LENGTH = 4096;

        private ShaderIncluder() {
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public long invoke(long user_data, long requested_source, int type, long requesting_source, long include_depth) {
            String requesting = MemoryUtil.memASCII((long)requesting_source);
            String requested = MemoryUtil.memASCII((long)requested_source);
            try (MemoryStack stack = MemoryStack.stackPush();){
                for (String includePath : includePaths) {
                    Path path = Paths.get(new URI(String.format("%s%s", includePath, requested)));
                    if (!Files.exists(path, new LinkOption[0])) continue;
                    byte[] bytes = Files.readAllBytes(path);
                    long l = ShadercIncludeResult.malloc((MemoryStack)stack).source_name(stack.ASCII((CharSequence)requested)).content(stack.bytes(bytes)).user_data(user_data).address();
                    return l;
                }
                throw new RuntimeException(String.format("%s: Unable to find %s in include paths", requesting, requested));
            }
            catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ShaderReleaser
    implements ShadercIncludeResultReleaseI {
        private ShaderReleaser() {
        }

        public void invoke(long user_data, long include_result) {
        }
    }

    public static enum ShaderKind {
        VERTEX_SHADER(0),
        GEOMETRY_SHADER(3),
        FRAGMENT_SHADER(1),
        COMPUTE_SHADER(2);

        private final int kind;

        private ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    public static final class SPIRV
    implements NativeResource {
        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return this.bytecode;
        }

        public void free() {
            this.bytecode = null;
        }
    }
}

