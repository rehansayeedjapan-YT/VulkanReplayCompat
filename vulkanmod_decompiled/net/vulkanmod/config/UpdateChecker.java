/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.loader.api.SemanticVersion
 *  net.fabricmc.loader.api.Version
 *  net.fabricmc.loader.api.VersionParsingException
 *  net.fabricmc.loader.impl.util.version.VersionParser
 *  net.minecraft.class_155
 */
package net.vulkanmod.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.VersionParser;
import net.minecraft.class_155;
import net.vulkanmod.Initializer;

public abstract class UpdateChecker {
    private static boolean updateAvailable = false;

    public static void checkForUpdates() {
        CompletableFuture.supplyAsync(() -> {
            try {
                Object req = "https://api.modrinth.com/v2/project/vulkanmod/version?include_changelog=false";
                String mcVersion = class_155.method_16673().comp_4025();
                req = (String)req + "&game_versions=%s".formatted(mcVersion);
                URL url = new URL((String)req);
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                InputStream inputStream = http.getInputStream();
                JsonObject data = JsonParser.parseString((String)("{ versions: " + new String(inputStream.readAllBytes()) + "}")).getAsJsonObject();
                JsonArray versions = data.getAsJsonArray("versions");
                http.disconnect();
                String version = String.valueOf(versions.get(0).getAsJsonObject().get("version_number")).replace("\"", "");
                SemanticVersion currentVersion = VersionParser.parseSemantic((String)Initializer.getVersion());
                if (currentVersion.getPrereleaseKey().isPresent()) {
                    Initializer.LOGGER.info("Pre-release version, skipping update check.");
                    return null;
                }
                boolean bl = updateAvailable = currentVersion.compareTo((Object)Version.parse((String)version)) < 0;
                if (updateAvailable) {
                    Initializer.LOGGER.info("Update available!");
                }
            }
            catch (IOException e) {
                Initializer.LOGGER.info("Error occurred, skipping update check.");
            }
            catch (VersionParsingException e) {
                Initializer.LOGGER.info("Unable to parse version, skipping update check.");
            }
            return null;
        });
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }
}

