package ore.guard.check;

import lombok.Getter;
import ore.guard.OreGuard;
import ore.guard.player.OrePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Менеджер для отправки оповещений о нарушениях
 */
public class AlertManager {

    private static final String ALERT_PERMISSION = "ore.guard.alerts";

    /**
     * Отправить оповещение всем игрокам с правом
     * @param player Игрок, который нарушил
     * @param check Проверка, которая сработала
     * @param info Дополнительная информация
     */
    public static void sendAlert(OrePlayer player, BaseCheck check, String info) {
        String message = formatAlert(player, check, info);

        // Сохраняем оповещение в историю игрока
        player.addAlert(check.getName(), check.getDescription(), info);

        // Отправляем всем игрокам с правом
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(ALERT_PERMISSION)) {
                onlinePlayer.sendMessage(message);
            }
        }

        // Также логируем в консоль
        OreGuard.getOutput().warning(message);
    }

    /**
     * Форматирование сообщения об оповещении
     */
    private static String formatAlert(OrePlayer player, BaseCheck check, String info) {
        return String.format("§8[§cOreGuard§8] §7%s §7flagged §c%s §7(%s) §8- §7%s",
                player.getName(),
                check.getName(),
                check.getDescription(),
                info != null ? info : "");
    }

    /**
     * Получить список всех оповещений для игрока
     */
    public static List<OrePlayer.AlertData> getPlayerAlerts(OrePlayer player) {
        return player.getAlertHistory();
    }

    /**
     * Данные об оповещении
     */
    @Getter
    public static class AlertData {
        private final String checkName;
        private final String checkDescription;
        private final String info;
        private final long timestamp;

        public AlertData(String checkName, String checkDescription, String info, long timestamp) {
            this.checkName = checkName;
            this.checkDescription = checkDescription;
            this.info = info;
            this.timestamp = timestamp;
        }
    }
}

