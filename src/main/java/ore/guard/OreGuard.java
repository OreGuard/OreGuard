package ore.guard;

import lombok.Getter;
import ore.guard.utils.localization.Localization;
import ore.guard.utils.version.UpdateUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OreGuard extends JavaPlugin {

    @Getter
    private static OreGuard instance;

    @Getter
    private static Localization localization;

    @Getter
    private static Logger output = Logger.getLogger("OreGuard");

    @Override
    public void onEnable() {

        instance = this;

        localization = new Localization();
        localization.setup();

        UpdateUtil.getLatestVersion();
    }

    @Override
    public void onDisable() {

    }
}
