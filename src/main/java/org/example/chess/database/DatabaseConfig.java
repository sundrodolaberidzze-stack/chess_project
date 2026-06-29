package org.example.chess.database;

import java.io.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final String FILE_PATH = "db_config.properties";

    public static void saveConfig(String user, String password) {
        Properties props = new Properties();
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        try (OutputStream out = new FileOutputStream(FILE_PATH)) {
            props.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }
}
