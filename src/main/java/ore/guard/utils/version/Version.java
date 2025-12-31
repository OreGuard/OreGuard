package ore.guard.utils.version;


import lombok.experimental.UtilityClass;
import ore.guard.OreGuard;

@UtilityClass
public class Version {

    public String VERSION;

    static {
        VERSION = OreGuard.getInstance().getDescription().getVersion();
    }
}
