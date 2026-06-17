package org.tradebook.journal.features.sysconfig.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.sysconfig.entity.SystemConfiguration;
import org.tradebook.journal.features.sysconfig.repository.SystemConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService {
    private final SystemConfigurationRepository repository;
    
    // In-memory cache to avoid hitting the database for every config lookup
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    private long lastUpdated = System.currentTimeMillis();

    @PostConstruct
    public void loadConfigurations() {
        log.info("Loading system configurations from database into cache...");
        List<SystemConfiguration> configs = repository.findAll();
        configs.forEach(config -> configCache.put(config.getConfigKey(), config.getConfigValue()));
        lastUpdated = System.currentTimeMillis();
        log.info("Loaded {} configurations.", configs.size());
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Gets a configuration value by key.
     * @param key The configuration key (e.g. "MAIL_HOST")
     * @param defaultValue A default value if the key is not found in the DB
     * @return The configuration value
     */
    public String getConfigValue(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }

    /**
     * Updates a configuration and refreshes the cache.
     * @param key The configuration key
     * @param value The new value
     * @param description Optional description
     */
    public void updateConfiguration(String key, String value, String description) {
        SystemConfiguration config = repository.findById(key)
                .orElse(new SystemConfiguration(key, value, description, null));
        
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        
        repository.save(config);
        configCache.put(key, value);
        lastUpdated = System.currentTimeMillis();
        log.info("Updated configuration key: {}", key);
    }

    /**
     * Refreshes the entire configuration cache from the database.
     */
    public void refreshCache() {
        configCache.clear();
        loadConfigurations();
    }
}
