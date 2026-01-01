package ore.guard.impl.listeners;

import ore.guard.OreGuard;
import ore.guard.player.OrePlayer;
import ore.guard.player.OrePlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    private final OrePlayerManager playerManager;

    public PlayerListener(OrePlayerManager playerManager) {
        this.playerManager = playerManager;
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

        // Выполняем все проверки
        OreGuard.getCheckManager().processChecks(orePlayer);
    }

    public OrePlayerManager getPlayerManager() {
        return playerManager;
    }
}