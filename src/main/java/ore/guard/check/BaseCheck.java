package ore.guard.check;

import lombok.Getter;
import ore.guard.OreGuard;
import ore.guard.player.OrePlayer;

/**
 * Базовый класс для всех проверок
 */
@Getter
public abstract class BaseCheck {

    private final String name;
    private final String description;
    private final int setback;
    private final boolean experimental;

    public BaseCheck() {
        Check annotation = this.getClass().getAnnotation(Check.class);
        if (annotation == null) {
            throw new IllegalStateException("Check класс " + this.getClass().getSimpleName() + " должен иметь аннотацию @Check");
        }
        this.name = annotation.name();
        this.description = annotation.description();
        this.setback = annotation.setback();
        this.experimental = annotation.experimental();
    }

    /**
     * Выполнить проверку
     * @param player Игрок для проверки
     */
    public abstract void process(OrePlayer player);

    /**
     * Флаг нарушения
     * @param player Игрок, который нарушил
     * @param info Дополнительная информация о нарушении
     */
    protected void flag(OrePlayer player, String info) {
        if (experimental) {
            // Экспериментальные проверки только логируют, но не флагают
            OreGuard.getOutput().info(String.format("[EXPERIMENTAL] %s флаг от %s: %s", name, player.getName(), info));
            return;
        }

        // Отправляем оповещение
        AlertManager.sendAlert(player, this, info);

        // Выполняем откат
        if (setback > 0) {
            player.flag(setback);
        }
    }
}

