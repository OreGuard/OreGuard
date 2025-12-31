package ore.guard.command.cmds;

import ore.guard.command.CommandRunner;
import ore.guard.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCMD extends CommandRunner {

    public HelpCMD() {
        super("oreguard", "oreguard.use", "Основная команда OreGuard", "/oreguard [subcommand]", false);


        registerSubCommands(
                new SubCommand("help", "oreguard.help", "Показать справку", "/oreguard help") {
                    @Override
                    public boolean execute(CommandSender sender, String[] args) {
                        showHelp(sender);
                        return true;
                    }

                    @Override
                    public List<String> onTabComplete(CommandSender sender, String[] args) {
                        return new ArrayList<>();
                    }
                },

                new SubCommand("reload", "oreguard.reload", "Перезагрузить плагин", "/oreguard reload") {
                    @Override
                    public boolean execute(CommandSender sender, String[] args) {
                        sendMessage(sender, "commands.reload.success");
                        return true;
                    }

                    @Override
                    public List<String> onTabComplete(CommandSender sender, String[] args) {
                        return new ArrayList<>();
                    }
                },

                new SubCommand("version", "oreguard.version", "Проверить версию", "/oreguard version") {
                    @Override
                    public boolean execute(CommandSender sender, String[] args) {
                        sendMessage(sender, "version.current", "1.0.0");
                        return true;
                    }

                    @Override
                    public List<String> onTabComplete(CommandSender sender, String[] args) {
                        return new ArrayList<>();
                    }
                }
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        if (getSubCommands().containsKey(subCommandName)) {
            SubCommand subCommand = getSubCommands().get(subCommandName);
            sendMessage(sender, "commands.help.specific",
                    subCommand.getName(),
                    subCommand.getUsage(),
                    subCommand.getDescription());
        } else {
            sendMessage(sender, "commands.help.not_found", subCommandName);
        }

        return true;
    }

    @Override
    protected List<String> getDefaultTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    private void showHelp(CommandSender sender) {
        sendMessage(sender, "commands.help.header");

        for (SubCommand subCommand : getSubCommands().values()) {
            if (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())) {
                sendMessage(sender, "commands.help.format",
                        subCommand.getUsage(),
                        subCommand.getDescription());
            }
        }

        sendMessage(sender, "commands.help.footer");
    }
}