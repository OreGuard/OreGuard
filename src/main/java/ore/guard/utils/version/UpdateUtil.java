package ore.guard.utils.version;

import lombok.experimental.UtilityClass;
import ore.guard.OreGuard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class UpdateUtil {

    public void getLatestVersion() {
        BufferedReader reader = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL("https://raw.githubusercontent.com/OreGuard/OreGuard/refs/heads/1.16.5/ver.txt");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "OreGuard-Proxy");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String version = reader.readLine().trim();

                String message = OreGuard.getLocalization().get("version.latest", version);
                OreGuard.getOutput().info(message);

                String currentVersionMessage = OreGuard.getLocalization().get(
                        "version.current",
                        Version.VERSION
                );
                OreGuard.getOutput().info(currentVersionMessage);

            } else {
                String errorMessage = OreGuard.getLocalization().get(
                        "version.failed",
                        connection.getResponseCode()
                );
                OreGuard.getOutput().warning(errorMessage);
            }

        } catch (IOException e) {
            String errorMessage = OreGuard.getLocalization().get("version.error", e.getMessage());
            OreGuard.getOutput().info(errorMessage);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}