package com.example.fit_lifegym.models;

import java.util.Date;

public class Subscription {
    private String id;
    private String userId;
    private String planType; // BASIC, PREMIUM, ELITE
    private String status; // ACTIVE, CANCELLED, EXPIRED, PENDING
    private double price;
    private String billingCycle; // MONTHLY, YEARLY
    private Date startDate;
    private Date endDate;
    private Date nextBillingDate;
    private boolean autoRenew;
    private String stripeSubscriptionId;
    private String stripeCustomerId;
    private Date createdAt;
    private Date updatedAt;

    public Subscription() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.autoRenew = true;
        this.status = "PENDING";
    }

    public Subscription(String userId, String planType, double price, String billingCycle) {
        this.userId = userId;
        this.planType = planType;
        this.price = price;
        this.billingCycle = billingCycle;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.autoRenew = true;
        this.status = "PENDING";
    }

    // Plan details
    public static class Plan {
        public static final String BASIC = "BASIC";
        public static final String PREMIUM = "PREMIUM";
        public static final String ELITE = "ELITE";

        public static double getMonthlyPrice(String planType) {
            switch (planType) {
                case BASIC: return 29.99;
                case PREMIUM: return 49.99;
                case ELITE: return 79.99;
                default: return 0.0;
            }
        }

        public static double getYearlyPrice(String planType) {
            return getMonthlyPrice(planType) * 10; // 2 months free
        }

        public static String getDescription(String planType) {
            switch (planType) {
                case BASIC:
                    return "Access to gym facilities\n" +
                           "Basic workout tracking\n" +
                           "Nutrition logging";
                case PREMIUM:
                    return "Everything in Basic\n" +
                           "Unlimited class bookings\n" +
                           "Personal training sessions (2/month)\n" +
                           "Advanced analytics\n" +
                           "Video library access\n" +
                           "Meal planning";
                case ELITE:
                    return "Everything in Premium\n" +
                           "Unlimited personal training\n" +
                           "Nutrition consultation\n" +
                           "Priority booking\n" +
                           "AI-powered recommendations\n" +
                           "Custom workout plans\n" +
                           "Progress photos & reports";
                default:
                    return "";
            }
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(Date nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
        this.updatedAt = new Date();
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isExpired() {
        return "EXPIRED".equals(status) || 
               (endDate != null && endDate.before(new Date()));
    }
}
