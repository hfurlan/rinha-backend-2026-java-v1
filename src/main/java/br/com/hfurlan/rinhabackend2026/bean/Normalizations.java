package br.com.hfurlan.rinhabackend2026.bean;

public class Normalizations {
    private int maxAmount;
    private int maxInstallments;
    private int amountVsAvgRatio;
    private int maxMinutes;
    private int maxKm;
    private int maxTxCount24h;
    private int maxMerchantAvgAmount;

    public int getMaxAmount() {
        return maxAmount;
    }
    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }
    public int getMaxInstallments() {
        return maxInstallments;
    }
    public void setMaxInstallments(int maxInstallments) {
        this.maxInstallments = maxInstallments;
    }
    public int getAmountVsAvgRatio() {
        return amountVsAvgRatio;
    }
    public void setAmountVsAvgRatio(int amountVsAvgRatio) {
        this.amountVsAvgRatio = amountVsAvgRatio;
    }
    public int getMaxMinutes() {
        return maxMinutes;
    }
    public void setMaxMinutes(int maxMinutes) {
        this.maxMinutes = maxMinutes;
    }
    public int getMaxKm() {
        return maxKm;
    }
    public void setMaxKm(int maxKm) {
        this.maxKm = maxKm;
    }
    public int getMaxTxCount24h() {
        return maxTxCount24h;
    }
    public void setMaxTxCount24h(int maxTxCount24h) {
        this.maxTxCount24h = maxTxCount24h;
    }
    public int getMaxMerchantAvgAmount() {
        return maxMerchantAvgAmount;
    }
    public void setMaxMerchantAvgAmount(int maxMerchantAvgAmount) {
        this.maxMerchantAvgAmount = maxMerchantAvgAmount;
    }
}

