package com.vendingmachine.repository;

public enum PaymentMethod {
    COD("Tiền mặt"),
    PAYOS("PayOS (Chuyển khoản)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
