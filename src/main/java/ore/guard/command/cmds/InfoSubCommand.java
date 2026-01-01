package ore.guard.command.cmds;

import ore.guard.command.SubCommand;
import ore.guard.check.AlertManager;
import ore.guard.player.OrePlayer;
import ore.guard.player.OrePlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InfoSubCommand extends SubCommand {

    private final OrePlayerManager playerManager;

    public InfoSubCommand(OrePlayerManager playerManager) {
        super("info", "oreguard.info", "Информация об игроке", "/oreguard info <player>", new ArrayList<>());
        this.playerManager = playerManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /oreguard info <player>");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null) {
            sender.sendMessage("§cИгрок " + targetName + " не найден или не в сети!");
            return true;
        }

        OrePlayer orePlayer = playerManager.getPlayer(targetPlayer);
        if (orePlayer == null) {
            sender.sendMessage("§cДанные игрока " + targetName + " не найдены!");
            return true;
        }

        // Отображаем информацию
        displayPlayerInfo(sender, orePlayer);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    names.add(player.getName());
                }
            }
            return names;
        }
        return new ArrayList<>();
    }

    private void displayPlayerInfo(CommandSender sender, OrePlayer orePlayer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        
        sender.sendMessage("§6=== §eИнформация об игроке: §c" + orePlayer.getName() + " §6===");
        sender.sendMessage("§7Brand клиента: §f" + orePlayer.getClientBrand());
        sender.sendMessage("§7Время в игре: §f" + formatPlayTime(orePlayer.getPlayTime()));
        sender.sendMessage("§7Время подключения: §f" + dateFormat.format(new Date(orePlayer.getJoinTime())));
        
        List<ore.guard.player.OrePlayer.AlertData> alerts = AlertManager.getPlayerAlerts(orePlayer);
        sender.sendMessage("§7Всего оповещений: §f" + alerts.size());
        
        if (!alerts.isEmpty()) {
            sender.sendMessage("§6--- §eПоследние 10 оповещений §6---");
            int showCount = Math.min(10, alerts.size());
            for (int i = alerts.size() - showCount; i < alerts.size(); i++) {
                ore.guard.player.OrePlayer.AlertData alert = alerts.get(i);
                String time = dateFormat.format(new Date(alert.timestamp()));
                sender.sendMessage("§8[" + time + "] §c" + alert.checkName() + " §7- §f" + alert.info());
            }
        }
        
        sender.sendMessage("§6================================");
    }

    private String formatPlayTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds % 60);
        } else {
            return String.format("%dс", seconds);
        }
    }
}

