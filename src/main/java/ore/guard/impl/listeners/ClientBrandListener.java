package ore.guard.impl.listeners;

import ore.guard.OreGuard;
import ore.guard.player.OrePlayer;
import ore.guard.player.OrePlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

/**
 * Обработчик PluginMessage для получения brand клиента
 */
public class ClientBrandListener implements Listener, PluginMessageListener {

    private final OrePlayerManager playerManager;

    public ClientBrandListener(OrePlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    // Handler for PlayerJoinEvent to register the plugin message channel
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Register the plugin message channel if needed
        // player.registerChannel("minecraft:brand");
        // player.registerChannel("MC|Brand");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("minecraft:brand") && !channel.equals("MC|Brand")) {
            return;
        }

        OrePlayer orePlayer = playerManager.getPlayer(player);
        if (orePlayer != null) {
            try {
                String brand;
                if (message.length > 0) {
                    // Пытаемся прочитать как VarInt
                    int length = message[0] & 0xFF;
                    if (length > 0 && length < message.length) {
                        brand = new String(message, 1, length, StandardCharsets.UTF_8);
                    } else {
                        // Если первый байт не длина, читаем всю строку
                        brand = new String(message, StandardCharsets.UTF_8);
                    }
                } else {
                    brand = "Unknown";
                }
                orePlayer.setClientBrand(brand);
                OreGuard.getOutput().fine("Player " + player.getName() + " brand: " + brand);
            } catch (Exception e) {
                OreGuard.getOutput().warning("Failed to parse brand for " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}