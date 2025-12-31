package ore.guard.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления экземплярами OrePlayer
 */
public class OrePlayerManager implements Listener {

    private final Map<UUID, OrePlayer> players = new ConcurrentHashMap<>();

    /**
     * Получение экземпляра OrePlayer по UUID
     */
    public OrePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Получение экземпляра OrePlayer по Bukkit Player
     */
    public OrePlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    /**
     * Создание нового экземпляра OrePlayer
     */
    public OrePlayer createPlayer(Player player) {
        OrePlayer orePlayer = new OrePlayer(player);
        players.put(player.getUniqueId(), orePlayer);
        return orePlayer;
    }

    /**
     * Удаление экземпляра OrePlayer
     */
    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    /**
     * Обновление данных всех игроков
     */
    public void updateAllPlayers() {
        for (OrePlayer player : players.values()) {
            if (player.isOnline()) {
                player.updateFromBukkit();
            }
        }
    }

    /**
     * Получение всех онлайн игроков
     */
    public Map<UUID, OrePlayer> getAllPlayers() {
        return new ConcurrentHashMap<>(players);
    }

    // Обработчики событий

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removePlayer(player.getUniqueId());
    }
}