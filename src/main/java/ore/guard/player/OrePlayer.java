package ore.guard.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ore.guard.OreGuard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для управления данными игрока в OreGuard
 * Аналог GrimPlayer, но специализированный для защиты блоков
 */
@Getter
public class OrePlayer {

    private final UUID uuid;
    private final Player bukkitPlayer;

    // Текущее состояние игрока
    @Setter private double x, y, z;
    @Setter private float yaw, pitch;
    @Setter private double lastX, lastY, lastZ;
    @Setter private float lastYaw, lastPitch;
    @Setter private boolean onGround, lastOnGround;
    @Setter private boolean isSneaking, wasSneaking;
    @Setter private boolean isSprinting, lastSprinting;
    @Setter private boolean isFlying, wasFlying;
    @Setter private boolean isSwimming, wasSwimming;
    @Setter private boolean isGliding, wasGliding;
    @Setter private boolean isClimbing, wasClimbing;

    // Дополнительные данные
    @Setter private BoundingBox boundingBox;
    @Setter private Pose pose = Pose.STANDING;
    @Setter private Pose lastPose = Pose.STANDING;
    @Setter private float walkSpeed = 0.2f;
    @Setter private float flySpeed = 0.1f;
    @Setter private int foodLevel = 20;
    @Setter private float fallDistance = 0.0f;

    // Векторы движения
    @Setter private Vector velocity = new Vector();
    @Setter private Vector lastVelocity = new Vector();
    @Setter private Vector predictedMovement = new Vector();
    @Setter private Vector actualMovement = new Vector();

    // История действий
    private final Queue<BlockInteraction> blockInteractions = new LinkedList<>();
    private final Map<Block, Long> protectedBlocksPlaced = new ConcurrentHashMap<>();
    private final Map<Block, Long> protectedBlocksBroken = new ConcurrentHashMap<>();
    private final List<MovementData> movementHistory = new ArrayList<>();

    // Состояние проверок
    @Setter private boolean isViolatingSneakSpeed = false;
    @Setter private int sneakViolations = 0;
    @Setter private long lastViolationTime = 0;
    @Setter private boolean isExempt = false;
    @Setter private String exemptionReason = "";

    // Статистика
    @Setter private long joinTime = System.currentTimeMillis();
    @Setter private int totalBlocksProtected = 0;
    @Setter private int totalBlocksBroken = 0;
    @Setter private int violationsCount = 0;

    // Флаги состояния
    private boolean isInWater = false;
    private boolean isInLava = false;
    private boolean isInCobweb = false;
    private boolean isOnIce = false;
    private boolean isOnSoulSand = false;
    private boolean isOnHoneyBlock = false;
    private boolean hasSlowEffect = false;
    private int slowEffectAmplifier = 0;

    @Getter
    private SetbackTeleportUtil setbackTeleportUtil;

    public OrePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.bukkitPlayer = player;

        // Инициализация начальных значений
        Location location = player.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();

        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;

        this.isSneaking = player.isSneaking();
        this.isSprinting = player.isSprinting();
        this.isFlying = player.isFlying();
        this.isGliding = player.isGliding();
        this.onGround = player.isOnGround();

        // ВАЖНО: Инициализируем setbackTeleportUtil в конструкторе!
        this.setbackTeleportUtil = new SetbackTeleportUtil(this);

        updateBoundingBox();
    }

    /**
     * Обновление данных игрока из Bukkit Player
     */
    public void updateFromBukkit() {
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

        Location location = bukkitPlayer.getLocation();

        // Сохраняем старые значения
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.lastOnGround = onGround;
        this.wasSneaking = isSneaking;
        this.lastSprinting = isSprinting;
        this.wasFlying = isFlying;
        this.wasGliding = isGliding;
        this.wasSwimming = isSwimming;
        this.wasClimbing = isClimbing;
        this.lastPose = pose;
        this.lastVelocity = velocity.clone();

        // Обновляем текущие значения
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.onGround = bukkitPlayer.isOnGround();
        this.isSneaking = bukkitPlayer.isSneaking();
        this.isSprinting = bukkitPlayer.isSprinting();
        this.isFlying = bukkitPlayer.isFlying();
        this.isGliding = bukkitPlayer.isGliding();

        // УДАЛИТЬ ЭТУ СТРОКУ: this.setbackTeleportUtil = new SetbackTeleportUtil(this);

        // Проверяем дополнительные состояния
        checkEnvironmentalConditions();
        updateBoundingBox();

        // Сохраняем историю движения
        saveMovementHistory();
    }

    /**
     * Обновление данных с проверкой телепортации
     */
    public void updateFromBukkitWithTeleportCheck() {
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

        Location location = bukkitPlayer.getLocation();

        // Проверяем, принял ли игрок телепорт
        SetbackTeleportUtil.TeleportAcceptData teleportData =
                setbackTeleportUtil.checkTeleportQueue(
                        location.getX(),
                        location.getY(),
                        location.getZ()
                );

        if (teleportData.isTeleportAccepted()) {
            // Игрок принял телепорт, обновляем данные
            updateFromBukkit();

            if (teleportData.hasSetback()) {
                // Логируем успешный сброс
                OreGuard.getOutput().info(
                        String.format("Player %s accepted setback teleport", getName())
                );
            }
        } else if (setbackTeleportUtil.shouldBlockMovement()) {
            // Движение должно быть заблокировано
            // Можно добавить логику для обработки этого случая
        } else {
            // Обычное обновление данных
            updateFromBukkit();

            // Обновляем последнюю безопасную позицию
            setbackTeleportUtil.updateLastKnownGoodPosition();
        }
    }

    /**
     * Выполнить телепортацию с откатом при нарушении
     */
    public boolean executeViolationSetback() {
        if (isViolatingSneakSpeed()) {
            return setbackTeleportUtil.executeSetback();
        }
        return false;
    }

    /**
     * Телепортировать игрока на указанную позицию
     */
    public void teleportWithSetback(Location location) {
        setbackTeleportUtil.executeSetback(location);
    }

    /**
     * Обновление ограничивающей рамки игрока
     */
    private void updateBoundingBox() {
        // Размеры игрока в зависимости от позы
        double width, height;

        switch (pose) {
            case SNEAKING:
                width = 0.6;
                height = 1.65;
                break;
            case SWIMMING:
            case FALL_FLYING:
                width = 0.6;
                height = 0.6;
                break;
            case SLEEPING:
                width = 0.2;
                height = 0.2;
                break;
            case STANDING:
            default:
                width = 0.6;
                height = 1.8;
                break;
        }

        this.boundingBox = new BoundingBox(
                x - width / 2, y, z - width / 2,
                x + width / 2, y + height, z + width / 2
        );
    }

    /**
     * Проверка условий окружающей среды
     */
    private void checkEnvironmentalConditions() {
        Location loc = new Location(bukkitPlayer.getWorld(), x, y, z);
        Block blockUnder = loc.getBlock();
        Block blockAt = loc.getBlock();

        // Проверяем блок под ногами
        switch (blockUnder.getType()) {
            case WATER:
            case BUBBLE_COLUMN:
                isInWater = true;
                break;
            case LAVA:
                isInLava = true;
                break;
            case COBWEB:
                isInCobweb = true;
                break;
            case ICE:
            case PACKED_ICE:
            case BLUE_ICE:
            case FROSTED_ICE:
                isOnIce = true;
                break;
            case SOUL_SAND:
            case SOUL_SOIL:
                isOnSoulSand = true;
                break;
            case HONEY_BLOCK:
                isOnHoneyBlock = true;
                break;
            default:
                isInWater = false;
                isInLava = false;
                isInCobweb = false;
                isOnIce = false;
                isOnSoulSand = false;
                isOnHoneyBlock = false;
                break;
        }

        // Проверяем эффекты зелий
        hasSlowEffect = bukkitPlayer.hasPotionEffect(PotionEffectType.SLOW);
        if (hasSlowEffect) {
            slowEffectAmplifier = bukkitPlayer.getPotionEffect(PotionEffectType.SLOW).getAmplifier();
        }

        // Проверяем плавание/лазание
        isSwimming = bukkitPlayer.isSwimming();
        isClimbing = blockAt.getType().toString().contains("LADDER") ||
                blockAt.getType().toString().contains("VINE") ||
                blockAt.getType() == org.bukkit.Material.SCAFFOLDING;
    }

    /**
     * Сохранение истории движения
     */
    private void saveMovementHistory() {
        MovementData movement = new MovementData(
                System.currentTimeMillis(),
                new Vector(x - lastX, y - lastY, z - lastZ),
                new Vector(x, y, z),
                new Vector(lastX, lastY, lastZ),
                isSneaking,
                isSprinting,
                onGround,
                getMovementSpeed()
        );

        movementHistory.add(movement);

        // Ограничиваем размер истории
        if (movementHistory.size() > 100) {
            movementHistory.remove(0);
        }
    }

    /**
     * Получение текущей скорости движения
     */
    public double getMovementSpeed() {
        Vector movement = new Vector(x - lastX, y - lastY, z - lastZ);
        return movement.length();
    }

    /**
     * Получение скорости движения по горизонтали
     */
    public double getHorizontalSpeed() {
        double dx = x - lastX;
        double dz = z - lastZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Получение скорости движения по вертикали
     */
    public double getVerticalSpeed() {
        return Math.abs(y - lastY);
    }

    /**
     * Проверка, может ли игрок быстро красться
     */
    public boolean canFastSneak() {
        // Игрок не может быстро красться если:
        // 1. Имеет эффект замедления
        // 2. Стоит на замедляющих блоках
        // 3. Находится в воде/лавве
        // 4. Находится в паутине

        return !(hasSlowEffect ||
                isOnSoulSand ||
                isOnHoneyBlock ||
                isInWater ||
                isInLava ||
                isInCobweb);
    }

    /**
     * Получение максимально допустимой скорости при крадении
     */
    public double getMaxSneakSpeed() {
        double baseSpeed = 0.13; // Базовая скорость крадущегося

        // Модификаторы скорости
        if (isOnSoulSand || isOnHoneyBlock) {
            baseSpeed *= 0.4; // -60%
        }
        if (isInCobweb) {
            baseSpeed *= 0.25; // -75%
        }
        if (isInWater) {
            baseSpeed *= 0.2; // -80%
        }
        if (isInLava) {
            baseSpeed *= 0.1; // -90%
        }
        if (hasSlowEffect) {
            baseSpeed *= (1.0 - (slowEffectAmplifier + 1) * 0.15); // -15% за уровень
        }

        return baseSpeed;
    }

    /**
     * Добавление взаимодействия с блоком
     */
    public void addBlockInteraction(Block block, InteractionType type) {
        BlockInteraction interaction = new BlockInteraction(
                block,
                type,
                System.currentTimeMillis(),
                new Vector(x, y, z),
                isSneaking,
                isSprinting
        );

        blockInteractions.add(interaction);

        if (blockInteractions.size() > 50) {
            blockInteractions.poll();
        }

        // Обновляем статистику
        if (type == InteractionType.BREAK) {
            totalBlocksBroken++;
        } else if (type == InteractionType.PLACE) {
            totalBlocksProtected++;
        }
    }

    /**
     * Проверка, нарушает ли игрок скорость крадущегося
     */
    public boolean isSneakSpeedViolation() {
        if (!isSneaking || !isSneaking) return false;
        if (!canFastSneak()) return false;

        double currentSpeed = getHorizontalSpeed();
        double maxAllowed = getMaxSneakSpeed();

        return currentSpeed > maxAllowed * 1.1; // 10% запас
    }

    /**
     * Сброс нарушений
     */
    public void resetViolations() {
        sneakViolations = 0;
        isViolatingSneakSpeed = false;
        lastViolationTime = 0;
    }

    /**
     * Получение локации игрока
     */
    public Location getLocation() {
        return new Location(bukkitPlayer.getWorld(), x, y, z, yaw, pitch);
    }

    /**
     * Получение имени игрока
     */
    public String getName() {
        return bukkitPlayer != null ? bukkitPlayer.getName() : "Unknown";
    }

    /**
     * Проверка онлайн статуса
     */
    public boolean isOnline() {
        return bukkitPlayer != null && bukkitPlayer.isOnline();
    }

    /**
     * Отправка сообщения игроку
     */
    public void sendMessage(String message) {
        if (isOnline()) {
            bukkitPlayer.sendMessage(message);
        }
    }

    /**
     * Отправка переведенного сообщения
     */
    public void sendTranslatedMessage(String key, Object... args) {
        if (isOnline()) {
            String message = OreGuard.getLocalization().get(key, args);
            bukkitPlayer.sendMessage(message);
        }
    }

    /**
     * Получение времени игры
     */
    public long getPlayTime() {
        return System.currentTimeMillis() - joinTime;
    }

    // Вложенные классы для структуры данных

    /**
     * Типы поз игрока
     */
    public enum Pose {
        STANDING,
        SNEAKING,
        SWIMMING,
        FALL_FLYING,
        SLEEPING
    }

    /**
     * Типы взаимодействия с блоками
     */
    public enum InteractionType {
        BREAK,
        PLACE,
        INTERACT
    }

    /**
     * Данные о движении
     */
    public record MovementData(
            long timestamp,
            Vector movement,
            Vector currentPosition,
            Vector previousPosition,
            boolean sneaking,
            boolean sprinting,
            boolean onGround,
            double speed
    ) {}

    /**
     * Взаимодействие с блоком
     */
    public record BlockInteraction(
            Block block,
            InteractionType type,
            long timestamp,
            Vector playerPosition,
            boolean wasSneaking,
            boolean wasSprinting
    ) {}
}