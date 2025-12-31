package ore.guard.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ore.guard.OreGuard;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Утилита для телепортации и сброса позиции игрока
 * Упрощенный аналог Grim SetbackTeleportUtil
 */
public class SetbackTeleportUtil {

    private final OrePlayer orePlayer;
    private final Player bukkitPlayer;

    // Очередь ожидающих телепортов
    private final Queue<TeleportData> pendingTeleports = new ConcurrentLinkedQueue<>();

    // Последняя известная безопасная позиция
    @Getter @Setter
    private SetbackPosition lastKnownGoodPosition;

    // Флаги состояния
    @Getter @Setter
    private boolean isSendingSetback = false;
    @Getter @Setter
    private boolean hasAcceptedSpawnTeleport = false;
    @Getter @Setter
    private boolean blockOffsets = false;

    // Счетчик телепортов
    private final AtomicInteger teleportIdCounter = new AtomicInteger(0);

    // Текущий необходимый сброс
    @Getter
    private SetBackData requiredSetBack = null;

    public SetbackTeleportUtil(OrePlayer orePlayer) {
        this.orePlayer = orePlayer;
        this.bukkitPlayer = orePlayer.getBukkitPlayer();
    }

    /**
     * Выполнить телепортацию с откатом
     */
    public boolean executeSetback() {
        if (isExempt()) return false;
        if (lastKnownGoodPosition == null) return false;
        if (isPendingSetback()) return true;

        // Создаем данные для телепортации
        Vector3d position = new Vector3d(
                lastKnownGoodPosition.getPosition().getX(),
                lastKnownGoodPosition.getPosition().getY(),
                lastKnownGoodPosition.getPosition().getZ()
        );

        Vector velocity = lastKnownGoodPosition.getVelocity().clone();

        // Симулируем физику на один тик вперед
        simulateNextTickPhysics(velocity);

        // Создаем данные телепортации
        TeleportData teleportData = new TeleportData(
                position,
                velocity,
                teleportIdCounter.incrementAndGet(),
                System.currentTimeMillis()
        );

        // Создаем данные сброса
        requiredSetBack = new SetBackData(
                teleportData,
                orePlayer.getYaw(),
                orePlayer.getPitch(),
                false
        );

        // Отправляем телепорт
        sendSetbackTeleport(teleportData);

        return true;
    }

    /**
     * Выполнить телепортацию на конкретную позицию
     */
    public boolean executeSetback(Location location) {
        if (isExempt()) return false;

        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
        Vector velocity = new Vector();

        TeleportData teleportData = new TeleportData(
                position,
                velocity,
                teleportIdCounter.incrementAndGet(),
                System.currentTimeMillis()
        );

        requiredSetBack = new SetBackData(
                teleportData,
                location.getYaw(),
                location.getPitch(),
                true
        );

        sendSetbackTeleport(teleportData);
        return true;
    }

    /**
     * Симуляция физики на один тик
     */
    private void simulateNextTickPhysics(Vector velocity) {
        // Простая симуляция гравитации и трения
        if (orePlayer.isOnGround()) {
            // Трение при ходьбе
            velocity.multiply(0.6);
        } else {
            // Гравитация
            velocity.setY(velocity.getY() - 0.08);
        }

        // Воздушное сопротивление
        velocity.multiply(0.98);
    }

    /**
     * Отправка телепортации с откатом
     */
    private void sendSetbackTeleport(TeleportData teleportData) {
        isSendingSetback = true;

        try {
            // Сохраняем телепорт в очередь
            pendingTeleports.add(teleportData);

            // Телепортируем игрока
            Location teleportLocation = new Location(
                    bukkitPlayer.getWorld(),
                    teleportData.getPosition().getX(),
                    teleportData.getPosition().getY(),
                    teleportData.getPosition().getZ(),
                    orePlayer.getYaw(),
                    orePlayer.getPitch()
            );

            // Выполняем телепорт асинхронно
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (bukkitPlayer.isOnline()) {
                        bukkitPlayer.teleport(teleportLocation);

                        // Применяем скорость если есть
                        if (teleportData.getVelocity().lengthSquared() > 0) {
                            bukkitPlayer.setVelocity(teleportData.getVelocity());
                        }
                    }
                }
            }.runTask(OreGuard.getInstance());

            // Обновляем последнюю известную позицию
            lastKnownGoodPosition = new SetbackPosition(
                    teleportData.getPosition(),
                    teleportData.getVelocity()
            );

        } finally {
            isSendingSetback = false;
        }
    }

    /**
     * Проверка принятия телепорта
     */
    public TeleportAcceptData checkTeleportQueue(double x, double y, double z) {
        TeleportAcceptData result = new TeleportAcceptData();

        TeleportData teleportData = pendingTeleports.peek();
        if (teleportData != null) {
            double deltaX = Math.abs(teleportData.getPosition().getX() - x);
            double deltaY = Math.abs(teleportData.getPosition().getY() - y);
            double deltaZ = Math.abs(teleportData.getPosition().getZ() - z);

            // Порог принятия телепорта (0.03 блока)
            double threshold = 0.03;

            if (deltaX <= threshold && deltaY <= threshold && deltaZ <= threshold) {
                // Игрок принял телепорт
                pendingTeleports.poll();
                hasAcceptedSpawnTeleport = true;
                blockOffsets = false;

                if (requiredSetBack != null && requiredSetBack.getTeleportData() == teleportData) {
                    result.setSetback(requiredSetBack);
                    requiredSetBack.setComplete(true);
                }

                result.setTeleportData(teleportData);
                result.setTeleportAccepted(true);

                // Логируем успешный телепорт
                OreGuard.getOutput().fine(
                        String.format("Player %s accepted teleport to %.2f, %.2f, %.2f",
                                orePlayer.getName(),
                                x, y, z
                        )
                );
            }
        }

        return result;
    }

    /**
     * Обновление последней безопасной позиции
     */
    public void updateLastKnownGoodPosition() {
        Location currentLocation = orePlayer.getLocation();
        lastKnownGoodPosition = new SetbackPosition(
                new Vector3d(
                        currentLocation.getX(),
                        currentLocation.getY(),
                        currentLocation.getZ()
                ),
                orePlayer.getVelocity()
        );
    }

    /**
     * Проверка нахождения в незагруженном чанке
     */
    public boolean isInUnloadedChunk() {
        if (bukkitPlayer == null) return true;

        Location location = bukkitPlayer.getLocation();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        return !bukkitPlayer.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    /**
     * Нужно ли блокировать движение
     */
    public boolean shouldBlockMovement() {
        return isInUnloadedChunk() ||
                blockOffsets ||
                (requiredSetBack != null && !requiredSetBack.isComplete());
    }

    /**
     * Освобожден ли игрок от телепортации
     */
    private boolean isExempt() {
        if (orePlayer.isExempt()) return true;
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return true;
        if (bukkitPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR) return true;

        return false;
    }

    /**
     * Есть ли ожидающий телепорт
     */
    private boolean isPendingSetback() {
        return requiredSetBack != null && !requiredSetBack.isComplete();
    }

    /**
     * Сброс всех данных
     */
    public void reset() {
        pendingTeleports.clear();
        lastKnownGoodPosition = null;
        requiredSetBack = null;
        isSendingSetback = false;
        hasAcceptedSpawnTeleport = false;
        blockOffsets = false;
    }

    /**
     * Получение информации о статусе
     */
    public String getStatus() {
        return String.format(
                "SetbackUtil[teleports=%d, hasBack=%s, sending=%s, blocked=%s]",
                pendingTeleports.size(),
                lastKnownGoodPosition != null,
                isSendingSetback,
                blockOffsets
        );
    }

    // Вложенные классы для хранения данных

    /**
     * Данные телепортации
     */
    @Getter
    public static class TeleportData {
        private final Vector3d position;
        private final Vector velocity;
        private final int teleportId;
        private final long timestamp;

        public TeleportData(Vector3d position, Vector velocity, int teleportId, long timestamp) {
            this.position = position;
            this.velocity = velocity;
            this.teleportId = teleportId;
            this.timestamp = timestamp;
        }
    }

    /**
     * Данные сброса позиции
     */
    @Getter @Setter
    public static class SetBackData {
        private final TeleportData teleportData;
        private final float yaw;
        private final float pitch;
        private final boolean forced;
        private boolean complete = false;

        public SetBackData(TeleportData teleportData, float yaw, float pitch, boolean forced) {
            this.teleportData = teleportData;
            this.yaw = yaw;
            this.pitch = pitch;
            this.forced = forced;
        }
    }

    /**
     * Позиция для отката
     */
    @Getter
    public static class SetbackPosition {
        private final Vector3d position;
        private final Vector velocity;

        public SetbackPosition(Vector3d position, Vector velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }

    /**
     * Результат проверки телепорта
     */
    @Getter @Setter
    public static class TeleportAcceptData {
        private TeleportData teleportData;
        private SetBackData setback;
        private boolean teleportAccepted = false;

        public boolean hasSetback() {
            return setback != null;
        }
    }

    /**
     * Простой класс Vector3d
     */
    @Getter
    public static class Vector3d {
        private final double x, y, z;

        public Vector3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector toBukkitVector() {
            return new Vector(x, y, z);
        }

        public double distance(Vector3d other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}