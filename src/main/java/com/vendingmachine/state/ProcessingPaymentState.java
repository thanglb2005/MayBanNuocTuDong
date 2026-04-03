package com.vendingmachine.state;

import com.vendingmachine.repository.Beverage;
import com.vendingmachine.repository.PaymentMethod;

public class ProcessingPaymentState implements VendingMachineState {

    private VendingMachine context;
    private Beverage selectedBeverage;

    public ProcessingPaymentState(Beverage selectedBeverage) {
        this.selectedBeverage = selectedBeverage;
    }

    @Override
    public void setContext(VendingMachine context) {
        this.context = context;
    }

    @Override
    public void selectProduct(String beverageId) {
        context.addMessage("Đã chọn sản phẩm rồi. Vui lòng thanh toán hoặc hủy.");
    }

    @Override
    public void processPayment(PaymentMethod method) {
        context.addMessage("Đang xử lý thanh toán bằng: " + method.getDisplayName() + "...");

        boolean success = simulatePayment(method, selectedBeverage.getPrice());

        if (success) {
            context.addMessage("Thanh toán thành công!");
            DispensingState dispensing = new DispensingState(selectedBeverage, method);
            context.changeState(dispensing);
            context.dispense();
        } else {
            context.addMessage("Thanh toán thất bại. Quay lại màn hình chính.");
            context.setSelectedBeverage(null);
            context.changeState(new ReadyState());
        }
    }

    @Override
    public void dispense() {
        context.addMessage("Chưa thanh toán. Không thể xuất hàng.");
    }

    @Override
    public String getStateName() {
        return "PROCESSING_PAYMENT";
    }

    private boolean simulatePayment(PaymentMethod method, double amount) {
        switch (method) {
            case COD:
                context.addMessage("[COD] Nhận tiền mặt: " + String.format("%,.0f", amount) + " VND.");
                return true;
            case PAYOS:
                context.addMessage("[PayOS] Chuyển khoản " + String.format("%,.0f", amount) + " VND — Xác nhận thành công.");
                return true;
            default:
                return false;
        }
    }
}
