package ore.guard.utils.version;

import lombok.experimental.UtilityClass;
import ore.guard.OreGuard;

import java.net.MalformedURLException;
import java.net.URL;

@UtilityClass
public class UpdateUtil {

    public void getActualVersion()
    {
        try {
            URL url = new URL("https://raw.githubusercontent.com/aurux/oreguard/dist/ver.txt");

            

        }
        catch (MalformedURLException e) {

            OreGuard.getLogger().info(e.getMessage());

        }


    }

}
