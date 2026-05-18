package br.com.hfurlan.rinhabackend2026.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private String fraudReferencesFilePath;
    private String mccRiskFilePath;
    private String normalizationFilePath;
    private boolean processNewFrauds;
}
