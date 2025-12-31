package ore.guard.command;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ore.guard.OreGuard;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandRunner implements CommandExecutor, TabCompleter {

    @Getter
    private final String commandName;
    @Getter
    private final String permission;
    @Getter
    private final String description;
    @Getter
    private final String usage;
    @Getter
    private final boolean playerOnly;

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandRunner(String commandName, String permission, String description, String usage, boolean playerOnly) {
        this.commandName = commandName;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
        this.playerOnly = playerOnly;
    }

    public CommandRunner(String commandName, String permission, String description) {
        this(commandName, permission, description, "/" + commandName, false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            sendMessage(sender, "commands.no_permission");
            return true;
        }


        if (playerOnly && !(sender instanceof Player)) {
            sendMessage(sender, "commands.player_only");
            return true;
        }


        if (args.length > 0 && subCommands.containsKey(args[0].toLowerCase())) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());


            if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                sendMessage(sender, "commands.no_permission");
                return true;
            }


            return subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        }


        return execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();


        if (args.length == 1) {
            completions.addAll(subCommands.entrySet().stream()
                    .filter(entry -> entry.getValue().getPermission().isEmpty() ||
                            sender.hasPermission(entry.getValue().getPermission()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));


            completions.addAll(getDefaultTabComplete(sender, args));
        }

        else if (args.length > 1 && subCommands.containsKey(args[0].toLowerCase())) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());


            if (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())) {
                completions = subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        else {
            completions = getDefaultTabComplete(sender, args);
        }


        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .sorted()
                .collect(Collectors.toList());
    }


    public abstract boolean execute(CommandSender sender, String[] args);


    protected List<String> getDefaultTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    protected void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    protected void registerSubCommands(SubCommand... commands) {
        for (SubCommand command : commands) {
            registerSubCommand(command);
        }
    }


    protected void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(OreGuard.getLocalization().get(key));
    }

    protected void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(OreGuard.getLocalization().get(key, args));
    }

    protected void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public Map<String, SubCommand> getSubCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    protected Player getPlayer(CommandSender sender) {
        return isPlayer(sender) ? (Player) sender : null;
    }
}