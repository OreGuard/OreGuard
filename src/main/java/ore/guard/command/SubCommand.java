package ore.guard.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    private final String name;
    private final String permission;
    private final String description;
    private final String usage;
    private final List<String> aliases;

    public SubCommand(String name, String permission, String description, String usage, List<String> aliases) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    public SubCommand(String name, String permission, String description, String usage) {
        this(name, permission, description, usage, Collections.emptyList());
    }

    public SubCommand(String name, String description) {
        this(name, "", description, "");
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public abstract boolean execute(CommandSender sender, String[] args);
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);
}