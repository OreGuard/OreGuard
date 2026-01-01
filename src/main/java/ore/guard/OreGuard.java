package ore.guard;

import lombok.Getter;
import ore.guard.check.CheckManager;
import ore.guard.check.impl.SneakSpeedCheck;
import ore.guard.command.cmds.HelpCMD;
import ore.guard.command.cmds.InfoSubCommand;
import ore.guard.impl.listeners.ClientBrandListener;
import ore.guard.impl.listeners.PlayerListener;
import ore.guard.player.OrePlayerManager;
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

    @Getter
    private static CheckManager checkManager;
    
    @Getter
    private static OrePlayerManager playerManager;

    @Override
    public void onEnable() {

        instance = this;

        localization = new Localization();
        localization.setup();

        // Инициализация менеджера игроков
        playerManager = new OrePlayerManager();
        getServer().getPluginManager().registerEvents(playerManager, this);
        
        // Регистрация обработчика PluginMessage для brand
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", new ClientBrandListener(playerManager));


        // Инициализация системы проверок
        checkManager = new CheckManager();
        checkManager.initialize();
        checkManager.registerCheck(new SneakSpeedCheck());

        getServer().getPluginManager().registerEvents(new PlayerListener(playerManager), this);

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
            // Добавляем подкоманду info в HelpCMD
            helpCommand.registerSubCommand(new InfoSubCommand(playerManager));
            command.setExecutor(helpCommand);
            command.setTabCompleter(helpCommand);
        }
    }
}
