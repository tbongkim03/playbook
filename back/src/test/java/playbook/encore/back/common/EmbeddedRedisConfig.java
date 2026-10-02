package playbook.encore.back.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port:6381}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void start() throws IOException {
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @PreDestroy
    public void stop() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
