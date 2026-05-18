package br.com.hfurlan.rinhabackend2026.dto;

public record Fraud (
    float[] vector,
    boolean fraud) {
}