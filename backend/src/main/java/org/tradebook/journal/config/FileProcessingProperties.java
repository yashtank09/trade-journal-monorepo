package org.tradebook.journal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.processing")
public class FileProcessingProperties {
    private Long intervalMs;
    private Integer batchSize;
}

