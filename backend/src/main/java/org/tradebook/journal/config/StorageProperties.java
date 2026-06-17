package org.tradebook.journal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {
    private String uploadDir;
    private String processedDir;
    private String errorDir;
    private List<String> supportedTypes;
}
