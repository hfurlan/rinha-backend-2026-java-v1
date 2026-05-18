package br.com.hfurlan.rinhabackend2026.bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FraudRequest {
    private String id;
    private FraudTransaction transaction;
    private FraudCustomer customer;
    private FraudMerchant merchant;
    private FraudTerminal terminal;
    @JsonProperty("last_transaction")
    private FraudLastTransaction lastTransaction;

    @Data
    public static class FraudTransaction {
        private float amount;
        private int installments;
        @JsonProperty("requested_at")
        private LocalDateTime requestedAt;
    }

    @Data
    public static class FraudCustomer {
        @JsonProperty("avg_amount")
        private float avgAmount;
        @JsonProperty("tx_count_24h")
        private int txCount24h;
        @JsonProperty("known_merchants")
        private String[] knownMerchants;
    }

    @Data
    public static class FraudMerchant {
        private String id;
        private Integer mcc;
        @JsonProperty("avg_amount")
        private float avgAmount;
    }

    @Data
    public static class FraudTerminal {
        @JsonProperty("is_online")
        private boolean isOnline;
        @JsonProperty("card_present")
        private boolean cardPresent;
        @JsonProperty("km_from_home")
        private float kmFromHome;
    }

    @Data
    public static class FraudLastTransaction {
        private LocalDateTime timestamp;
        @JsonProperty("km_from_current")
        private float kmFromCurrent;
    }
}