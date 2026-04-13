package com.example.fit_lifegym.models;

import java.util.Date;

public class PromoCode {
    private String id;
    private String code;
    private String description;
    private String discountType; // PERCENTAGE, FIXED_AMOUNT
    private double discountValue;
    private double minPurchaseAmount;
    private int maxUses;
    private int currentUses;
    private Date validFrom;
    private Date validUntil;
    private boolean isActive;
    private String applicablePlans; // ALL, BASIC, PREMIUM, ELITE
    private Date createdAt;

    public PromoCode() {
        this.createdAt = new Date();
        this.isActive = true;
        this.currentUses = 0;
        this.applicablePlans = "ALL";
    }

    public PromoCode(String code, String discountType, double discountValue) {
        this.code = code.toUpperCase();
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.createdAt = new Date();
        this.isActive = true;
        this.currentUses = 0;
        this.applicablePlans = "ALL";
    }

    public double calculateDiscount(double originalAmount) {
        if (!isValid()) {
            return 0;
        }

        if (minPurchaseAmount > 0 && originalAmount < minPurchaseAmount) {
            return 0;
        }

        if ("PERCENTAGE".equals(discountType)) {
            return originalAmount * (discountValue / 100.0);
        } else if ("FIXED_AMOUNT".equals(discountType)) {
            return Math.min(discountValue, originalAmount);
        }

        return 0;
    }

    public boolean isValid() {
        if (!isActive) {
            return false;
        }

        Date now = new Date();
        if (validFrom != null && now.before(validFrom)) {
            return false;
        }

        if (validUntil != null && now.after(validUntil)) {
            return false;
        }

        if (maxUses > 0 && currentUses >= maxUses) {
            return false;
        }

        return true;
    }

    public void incrementUses() {
        this.currentUses++;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code.toUpperCase();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinPurchaseAmount() {
        return minPurchaseAmount;
    }

    public void setMinPurchaseAmount(double minPurchaseAmount) {
        this.minPurchaseAmount = minPurchaseAmount;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getApplicablePlans() {
        return applicablePlans;
    }

    public void setApplicablePlans(String applicablePlans) {
        this.applicablePlans = applicablePlans;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDiscountDisplay() {
        if ("PERCENTAGE".equals(discountType)) {
            return (int) discountValue + "% OFF";
        } else {
            return "$" + (int) discountValue + " OFF";
        }
    }
}
