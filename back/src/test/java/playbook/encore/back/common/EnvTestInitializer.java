package playbook.encore.back.common;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnvTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        Path envFile = Paths.get(System.getProperty("user.dir"), ".env.test");
        Map<String, Object> props = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx < 1) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                props.put(key, value);
            }
        } catch (Exception e) {
            throw new IllegalStateException(".env.test 파일을 읽을 수 없습니다: " + envFile, e);
        }

        context.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("envTest", props));
    }
}
