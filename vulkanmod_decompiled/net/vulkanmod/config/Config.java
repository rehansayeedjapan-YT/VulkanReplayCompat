/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 */
package net.vulkanmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;

public class Config {
    public VideoModeSet.VideoMode videoMode = VideoModeManager.getFirstAvailable().getVideoMode();
    public int windowMode = 0;
    public int advCulling = 2;
    public boolean indirectDraw = true;
    public boolean uniqueOpaqueLayer = true;
    public boolean entityCulling = true;
    public int device = -1;
    public int ambientOcclusion = 1;
    public int frameQueueSize = 2;
    public int builderThreads = 0;
    public boolean backFaceCulling = true;
    public boolean textureAnimations = true;
    private static Path CONFIG_PATH;
    private static final Gson GSON;

    public void write() {
        if (!Files.exists(CONFIG_PATH.getParent(), new LinkOption[0])) {
            try {
                Files.createDirectories(CONFIG_PATH, new FileAttribute[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(CONFIG_PATH, Collections.singleton(GSON.toJson((Object)this)), new OpenOption[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config load(Path path) {
        Config config;
        block8: {
            CONFIG_PATH = path;
            if (Files.exists(path, new LinkOption[0])) {
                try (FileReader fileReader = new FileReader(path.toFile());){
                    config = (Config)GSON.fromJson((Reader)fileReader, Config.class);
                    break block8;
                }
                catch (IOException exception) {
                    throw new RuntimeException(exception.getMessage());
                }
            }
            config = new Config();
            config.write();
        }
        return config;
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(new int[]{2}).create();
    }
}

