package br.com.hfurlan.rinhabackend2026.bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FraudResponse {
    private boolean approved;
    @JsonProperty("fraud_score")
    private float fraudScore;
}