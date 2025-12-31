package ore.guard.impl.listeners;

import ore.guard.player.OrePlayer;
import ore.guard.player.OrePlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ore.guard.OreGuard;

public class PlayerListener implements Listener {

    private final OrePlayerManager playerManager;

    public PlayerListener() {
        this.playerManager = new OrePlayerManager();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        OrePlayer orePlayer = playerManager.getPlayer(player);

        if (orePlayer == null) {
            orePlayer = playerManager.createPlayer(player);
        }

        // Обновляем данные с проверкой телепортации
        orePlayer.updateFromBukkitWithTeleportCheck();

        // Проверка скорости крадущегося
        if (orePlayer.isSneaking() && orePlayer.isOnGround()) {
            checkSneakSpeedViolation(orePlayer, event);
        }
    }

    private void checkSneakSpeedViolation(OrePlayer orePlayer, PlayerMoveEvent event) {
        if (orePlayer.canFastSneak()) {
            double currentSpeed = orePlayer.getHorizontalSpeed();
            double maxAllowed = orePlayer.getMaxSneakSpeed();

            if (currentSpeed > maxAllowed * 1.1) {
                orePlayer.setViolatingSneakSpeed(true);
                orePlayer.setSneakViolations(orePlayer.getSneakViolations() + 1);

                // Выполняем телепортацию с откатом
                boolean setbackExecuted = orePlayer.executeViolationSetback();

                if (setbackExecuted) {
                    event.setCancelled(true);
                    orePlayer.sendTranslatedMessage("messages.sneak_speed_violation");

                    OreGuard.getOutput().warning(
                            String.format("Player %s sneak speed violation: %.4f > %.4f",
                                    orePlayer.getName(), currentSpeed, maxAllowed)
                    );
                }
            }
        }
    }

    public OrePlayerManager getPlayerManager() {
        return playerManager;
    }
}