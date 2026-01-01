package ore.guard.check.impl;

import ore.guard.check.BaseCheck;
import ore.guard.check.Check;
import ore.guard.player.OrePlayer;

/**
 * Проверка на превышение скорости при крадении
 */
@Check(
        name = "SneakSpeed",
        description = "Обнаружено превышение скорости при крадении",
        setback = 5,
        experimental = false
)
public class SneakSpeedCheck extends BaseCheck {

    @Override
    public void process(OrePlayer player) {


        double currentSpeed = player.getHorizontalSpeed();
        double maxAllowed = player.getMaxSneakSpeed();

        if (currentSpeed > maxAllowed * 1.1) {
            flag(player, String.format("Скорость: %.4f > %.4f", currentSpeed, maxAllowed));
        }
    }
}

