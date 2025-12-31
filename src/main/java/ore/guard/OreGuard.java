package ore.guard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OreGuard extends JavaPlugin {

    @Getter
    private static OreGuard instance;

    @Getter
    private static Logger logger = Logger.getLogger("OreGuard");

    @Override
    public void onEnable() {

        instance = this;


    }

    @Override
    public void onDisable() {

    }
}
