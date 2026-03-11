package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentRequest {
    @SerializedName("amount")
    private Double amount;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("description")
    private String description;

    @SerializedName("paymentType")
    private String paymentType;

    @SerializedName("currency")
    private String currency;

    public CreatePaymentRequest(Double amount, String paymentMethod, String description, String paymentType) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.paymentType = paymentType;
        this.currency = "PLN";
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}