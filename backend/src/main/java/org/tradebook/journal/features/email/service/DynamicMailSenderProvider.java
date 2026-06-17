package org.tradebook.journal.features.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.tradebook.journal.features.sysconfig.service.ConfigurationService;

import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicMailSenderProvider {

    private final ConfigurationService configurationService;
    private JavaMailSenderImpl mailSender;
    private long lastUpdateTime = 0;

    /**
     * Gets or creates the JavaMailSender using the latest configurations.
     */
    public JavaMailSender getMailSender() {
        long currentConfigTime = configurationService.getLastUpdated();
        if (mailSender == null || lastUpdateTime < currentConfigTime) {
            log.info("Building new JavaMailSender due to configuration update.");
            mailSender = buildMailSender();
            lastUpdateTime = currentConfigTime;
        }
        return mailSender;
    }

    public void reloadMailSender() {
        log.info("Reloading JavaMailSender configurations...");
        this.mailSender = buildMailSender();
        this.lastUpdateTime = configurationService.getLastUpdated();
    }

    private JavaMailSenderImpl buildMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost(configurationService.getConfigValue("MAIL_HOST", "smtp.gmail.com"));
        sender.setPort(Integer.parseInt(configurationService.getConfigValue("MAIL_PORT", "587")));
        
        String username = configurationService.getConfigValue("MAIL_USERNAME", "");
        String password = configurationService.getConfigValue("MAIL_PASSWORD", "");
        
        if (!username.isEmpty()) {
            sender.setUsername(username);
        }
        if (!password.isEmpty()) {
            sender.setPassword(password);
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        return sender;
    }
}
