package ore.guard.check;

import lombok.Getter;
import ore.guard.OreGuard;
import ore.guard.player.OrePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для управления всеми проверками
 */
public class CheckManager {

    @Getter
    private final List<BaseCheck> checks = new ArrayList<>();

    /**
     * Инициализация и регистрация всех проверок
     */
    public void initialize() {
        // Проверки регистрируются вручную через registerCheck()
        // Это можно сделать в OreGuard.onEnable()
    }

    /**
     * Регистрация проверки вручную
     */
    public void registerCheck(BaseCheck check) {
        checks.add(check);
        OreGuard.getOutput().info("Зарегистрирована проверка: " + check.getName() + " (" + check.getDescription() + ")");
    }

    /**
     * Выполнить все проверки для игрока
     */
    public void processChecks(OrePlayer player) {
        if (player.isExempt()) return;
        
        for (BaseCheck check : checks) {
            try {
                check.process(player);
            } catch (Exception e) {
                OreGuard.getOutput().warning("Ошибка в проверке " + check.getName() + " для игрока " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Получить проверку по имени
     */
    public BaseCheck getCheck(String name) {
        return checks.stream()
                .filter(check -> check.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

