package com.vendingmachine.repository;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "beverage_id", nullable = false)
    private Beverage beverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "order_code")
    private Long orderCode;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    protected Transaction() {}

    public Transaction(Beverage beverage, PaymentMethod paymentMethod) {
        this.beverage = beverage;
        this.paymentMethod = paymentMethod;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Beverage getBeverage() { return beverage; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Long getOrderCode() { return orderCode; }
    public void setOrderCode(Long orderCode) { this.orderCode = orderCode; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp != null
                ? timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
                : "";
    }

    /**
     * Amount được tính từ giá của Beverage.
     * Theo thiết kế UML: Transaction không lưu trực tiếp `amount/status`.
     */
    public double calculateAmount() {
        return beverage != null ? beverage.getPrice() : 0d;
    }

    public String getFormattedAmount() {
        return String.format("%,.0f", calculateAmount());
    }
}
