package com.vericart.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal, dependency-free .env loader.
 *
 * Spring Boot does NOT read .env files by default; this loads the project
 * .env (kept out of git) and exposes each KEY=VALUE as a JVM system property
 * so the existing {@code ${...}} placeholders in application.yml resolve to the
 * real secrets. OS environment variables keep precedence (we never overwrite an
 * existing system property).
 */
@Slf4j
public final class DotEnvLoader {

    private DotEnvLoader() {}

    public static void load() {
        Path env = findEnvFile();
        if (env == null) {
            log.info("[DotEnv] No .env file found — relying on OS environment variables / application.yml defaults.");
            return;
        }
        Map<String, String> parsed = parse(env);
        int applied = 0;
        for (Map.Entry<String, String> e : parsed.entrySet()) {
            if (System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
                applied++;
            }
        }
        log.info("[DotEnv] Loaded {} variable(s) from {}", applied, env.toAbsolutePath());
    }

    private static Path findEnvFile() {
        // Candidate locations, in priority order.
        String[] candidates = { "backend/.env", ".env" };
        for (String c : candidates) {
            Path p = Paths.get(c);
            if (Files.exists(p)) return p;
        }
        // Walk upward from the working directory (covers IDE / jar launch dirs).
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++) {
            Path p = dir.resolve(".env");
            if (Files.exists(p)) return p;
            dir = dir.getParent();
        }
        return null;
    }

    private static Map<String, String> parse(Path path) {
        Map<String, String> out = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                // Allow optional "export " prefix (shell style).
                if (trimmed.startsWith("export ")) trimmed = trimmed.substring(7).trim();
                int eq = trimmed.indexOf('=');
                if (eq < 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                // Strip surrounding quotes.
                if (value.length() >= 2 &&
                        ((value.startsWith("\"") && value.endsWith("\"")) ||
                         (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!key.isEmpty()) out.put(key, value);
            }
        } catch (IOException e) {
            log.warn("[DotEnv] Failed to read {}: {}", path, e.getMessage());
        }
        return out;
    }
}
