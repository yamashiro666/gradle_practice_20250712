package gradle_practice_20250712;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE_PATH = "src/main/resources/port_mapping.properties";

    public static Map<Integer, String> loadPortFileMap() {
        Map<Integer, String> portFileMap = new LinkedHashMap<>();
        Properties props = new Properties();

        try (InputStream is = new FileInputStream(CONFIG_FILE_PATH)) {
            props.load(is);
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key); // 例: "5001,FILE1.csv"
                String[] parts = value.split(",", 2);
                if (parts.length == 2) {
                    int port = Integer.parseInt(parts[0].trim());
                    String fileName = parts[1].trim();
                    portFileMap.put(port, fileName);
                }
            }
        } catch (IOException e) {
            System.err.println("設定ファイルの読み込みエラー: " + e.getMessage());
        }

        return portFileMap;
    }
}
