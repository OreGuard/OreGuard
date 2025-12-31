package ore.guard;

import lombok.Getter;
import ore.guard.command.cmds.HelpCMD;
import ore.guard.utils.localization.Localization;
import ore.guard.utils.version.UpdateUtil;
import org.bukkit.command.PluginCommand;
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

        registerCommands();
        UpdateUtil.getLatestVersion();
    }



    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        HelpCMD helpCommand = new HelpCMD();

        PluginCommand command = getCommand("oreguard");
        if (command != null) {
            command.setExecutor(helpCommand);
            command.setTabCompleter(helpCommand);
        }
    }
}
