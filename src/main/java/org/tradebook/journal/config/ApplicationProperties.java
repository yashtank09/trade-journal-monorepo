package org.tradebook.journal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tradebook.application")
public class ApplicationProperties {
    private String defaultCurrency;
    private String frontendUrl;
    private int passwordResetTokenExpiryMinutes = 15;
}
