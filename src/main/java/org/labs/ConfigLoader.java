package org.labs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
  public static Properties loadProps() throws IOException {
    Properties props = new Properties();
        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("application.properties не найден в classpath");
            }
            props.load(input);
        }
        return props; 
  }
}
