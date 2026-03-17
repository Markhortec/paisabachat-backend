package com.hamza.paisabachat.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String version;
    private FreeTier freeTier = new FreeTier();
    private PremiumTier premiumTier = new PremiumTier();
    private Cors cors = new Cors();

    public static class FreeTier {
        private int maxGoals = 3;
        public int getMaxGoals() { return maxGoals; }
        public void setMaxGoals(int maxGoals) { this.maxGoals = maxGoals; }
    }

    public static class PremiumTier {
        private String maxGoals = "unlimited";
        public String getMaxGoals() { return maxGoals; }
        public void setMaxGoals(String maxGoals) { this.maxGoals = maxGoals; }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        );
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    // ── Getters & Setters ──
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public FreeTier getFreeTier() { return freeTier; }
    public void setFreeTier(FreeTier freeTier) { this.freeTier = freeTier; }
    public PremiumTier getPremiumTier() { return premiumTier; }
    public void setPremiumTier(PremiumTier premiumTier) { this.premiumTier = premiumTier; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
}