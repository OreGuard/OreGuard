package ore.guard.utils.localization;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ore.guard.OreGuard;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Localization {

    @Getter
    private FileConfiguration messages;
    private File messagesFile;

    public void setup() {
        if (!OreGuard.getInstance().getDataFolder().exists()) {
            OreGuard.getInstance().getDataFolder().mkdir();
        }

        messagesFile = new File(OreGuard.getInstance().getDataFolder(), "lang.yml");

        if (!messagesFile.exists()) {
            OreGuard.getInstance().saveResource("languages/ru.yml", false);
            File ruFile = new File(OreGuard.getInstance().getDataFolder(), "languages/ru.yml");
            if (ruFile.exists()) {
                ruFile.renameTo(messagesFile);
            }
        }

        reload();
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = OreGuard.getInstance().getResource("languages/ru.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultConfig);
        }
    }

    public String get(String path) {
        if (messages == null) {
            return path;
        }
        return messages.getString(path, path);
    }

    public String get(String path, Object... args) {
        String message = get(path);
        return String.format(message, args);
    }
}
