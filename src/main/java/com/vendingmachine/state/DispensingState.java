package com.vendingmachine.state;

import com.vendingmachine.repository.Beverage;
import com.vendingmachine.repository.PaymentMethod;
import com.vendingmachine.repository.Transaction;

public class DispensingState implements VendingMachineState {

    private VendingMachine context;
    private Beverage selectedBeverage;
    private PaymentMethod paymentMethod;

    public DispensingState(Beverage selectedBeverage, PaymentMethod paymentMethod) {
        this.selectedBeverage = selectedBeverage;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public void setContext(VendingMachine context) {
        this.context = context;
    }

    @Override
    public void selectProduct(String beverageId) {
        context.addMessage("Đang xuất hàng, vui lòng chờ...");
    }

    @Override
    public void processPayment(PaymentMethod method) {
        context.addMessage("Đã thanh toán rồi. Đang xuất hàng...");
    }

    @Override
    public void dispense() {
        boolean reduced = context.getInventory().reduceStock(selectedBeverage.getId());

        if (reduced) {
            context.addMessage("Đang xuất sản phẩm: " + selectedBeverage.getDescription());
            context.addMessage("Sản phẩm đã ra! Cảm ơn bạn đã mua hàng.");

            Transaction transaction = new Transaction(selectedBeverage, paymentMethod);
            context.addTransaction(transaction);

            int remaining = context.getInventory().getStock(selectedBeverage.getId());
            if (remaining <= 2 && remaining > 0) {
                context.addMessage("Cảnh báo: \"" + selectedBeverage.getName()
                        + "\" sắp hết hàng! (Còn " + remaining + ")");
            }

            context.setSelectedBeverage(null);

            if (!context.getInventory().hasAnyStock()) {
                context.changeState(new OutOfStockState());
            } else {
                context.changeState(new ReadyState());
            }
        } else {
            context.addMessage("Không thể xuất hàng — sản phẩm đã hết.");
            context.setSelectedBeverage(null);
            context.changeState(new OutOfStockState());
        }
    }

    @Override
    public String getStateName() {
        return "DISPENSING";
    }
}
