package net.cybercake.discordmusicbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.cybercake.discordmusicbot.utilities.StringUtils.removeNonAlphanumericSpaceCharacters;

@SuppressWarnings({"unused"})
public class GuildSettings implements Serializable {

    public static final @NotNull URL DEFAULT_RESOURCE = Objects.requireNonNull(Main.class.getClassLoader().getResource("servers/0.json"), "No resource found: servers/0.json (required for function)");
    public static final @NotNull Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final static List<GuildSettings> allSettings = new ArrayList<>();

    public static GuildSettings get(long guildId, boolean createIfNull) {
        return allSettings.stream().filter(guildSettings -> !guildSettings.isDefault).filter(guildSettings -> guildSettings.meta.guild == guildId).findFirst()
                .orElseGet(() -> {
                    if (createIfNull)
                        return create(guildId, Objects.requireNonNull(Main.JDA.getGuildById(guildId), "Failed to find guild via ID " + guildId).getName());
                    else return null;
                });
    }

    public static void write(GuildSettings settings) {
        File file = settings.file;
        try (FileWriter writer = new FileWriter(file)){
            String json = GSON.toJson(settings, GuildSettings.class);
            writer.write(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Write failed to file " + file.getAbsolutePath(), exception);
        }
    }

    static GuildSettings create(long guildId, String guildName, boolean isDefault) {
        try {
            String doctoredGuildName = removeNonAlphanumericSpaceCharacters(guildName);
            File settingsFile = createOrGetFileFor(guildId, doctoredGuildName, isDefault);
            GuildSettings settings = GSON.fromJson(new FileReader(settingsFile), GuildSettings.class);
            settings.file = settingsFile;
            settings.isDefault = isDefault;
            if(!isDefault) {
                settings.meta.guild = guildId;
                settings.meta.name = guildName;
            }
            write(settings);
            return settings;
        } catch (Exception exception) {
            throw new IllegalStateException("Settings creation failed - method: create(" + guildId + ", \"" + guildName + "\")"
                    + " || If you are loading for the first time, consider adding the default '0.json' to local path 'servers'"
                    , exception);
        }
    }

    public static GuildSettings create(long guildId, String guildName) {
        return create(guildId, guildName, false);
    }

    @NotNull
    private static File createOrGetFileFor(long guildId, String doctoredGuildName, boolean isDefault) throws IOException, URISyntaxException {
        File file = new File("servers", guildId + ".json");
        if(!file.exists()) {
            if(!file.getParentFile().exists() && !file.getParentFile().mkdir()) throw new FileSystemException(file.getAbsolutePath(), "[Directory] Does not exist", "Directory not created via mkdir() method.");
            if(!file.createNewFile()) throw new FileSystemException(file.getAbsolutePath(), "[File] Does not exist", "File not created via createNewFile() method.");
            File potentialReaderFile = isDefault ? new File(DEFAULT_RESOURCE.toURI()) : asDefault().file;
            if(isDefault) Log.info("Default settings found, resource located at " + DEFAULT_RESOURCE.getFile());
            try (FileWriter writer = new FileWriter(file);
                 FileReader reader = new FileReader(potentialReaderFile)
            ){
                int character;
                while((character = reader.read()) != -1)
                    writer.write((char) character);
            } catch (Exception exception) {
                throw new IOException("Failed to write defaults to file " + file.getAbsolutePath(), exception);
            }
        }
        return file;
    }

    public static GuildSettings asDefault() {
        return allSettings.stream().filter(guildSettings -> guildSettings.isDefault).findFirst().orElseThrow(() -> new IllegalStateException("No default JSON file found"));
    }

    private transient boolean expired = false;
    private transient boolean isDefault = false; // have to use 'isDefault' because 'default' is java keyword

    transient File file = null;

    public Meta meta;
    public Settings settings;

    public GuildSettings() {
        allSettings.add(this);
    }

    /**
     * Expires this instance to request a new refresh
     */
    public void expireAndReload(Guild guild) {
        write(this);

        this.expired = true;
        allSettings.remove(this);

        create(guild.getIdLong(), guild.getName());
    }

    public boolean isExpired() { return this.expired; }
    public boolean isDefault() { return this.isDefault; }

    public static class Meta {
        public Long guild;
        public String name;
    }

    public static class Settings {
        public Long djRole;
        public Channels channels;

        public static class Channels {
            public List<Long> allowed = new ArrayList<>();
            public List<Long> disallowed = new ArrayList<>();
        }
    }

}
