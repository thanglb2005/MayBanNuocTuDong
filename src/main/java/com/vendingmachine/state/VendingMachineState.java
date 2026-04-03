package com.vendingmachine.state;

import com.vendingmachine.repository.PaymentMethod;

/**
 * State Pattern — Interface State (chuẩn học thuật).
 *
 * Các method KHÔNG nhận context qua parameter.
 * ConcreteState giữ reference -context qua setContext().
 * Chuyển trạng thái bằng context.changeState(newState).
 */
public interface VendingMachineState {

    void setContext(VendingMachine context);

    void selectProduct(String beverageId);

    void processPayment(PaymentMethod method);

    void dispense();

    String getStateName();
}
