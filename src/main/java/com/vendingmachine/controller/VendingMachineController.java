package com.vendingmachine.controller;

import com.vendingmachine.repository.*;
import com.vendingmachine.service.PayOSService;
import com.vendingmachine.state.ReadyState;
import com.vendingmachine.state.VendingMachine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VendingMachineController {

    private final TransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PayOSService payOSService;

    public VendingMachineController(TransactionRepository transactionRepository,
                                    InventoryItemRepository inventoryItemRepository,
                                    PayOSService payOSService) {
        this.transactionRepository = transactionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.payOSService = payOSService;
    }

    private VendingMachine getMachine() {
        return VendingMachine.getInstance();
    }

    @GetMapping("/")
    public String index(Model model) {
        VendingMachine machine = getMachine();

        model.addAttribute("items", machine.getInventory().getAllItems());
        model.addAttribute("state", machine.getState().getStateName());
        model.addAttribute("selectedBeverage", machine.getSelectedBeverage());
        model.addAttribute("transactions", machine.getTransactions());
        model.addAttribute("messages", machine.getMessages());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        machine.clearMessages();
        return "vending";
    }

    @PostMapping("/select")
    public String selectProduct(@RequestParam String beverageId,
                                RedirectAttributes ra) {
        VendingMachine machine = getMachine();

        machine.clearMessages();
        machine.selectProduct(beverageId);

        ra.addFlashAttribute("flashMessages", machine.getMessages());
        return "redirect:/";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam String method,
                      RedirectAttributes ra) {
        VendingMachine machine = getMachine();
        PaymentMethod paymentMethod = PaymentMethod.valueOf(method);

        machine.clearMessages();

        if (paymentMethod == PaymentMethod.PAYOS && machine.getSelectedBeverage() != null) {
            return handlePayOS(machine, ra);
        }

        machine.processPayment(paymentMethod);
        persistAfterCOD(machine);

        ra.addFlashAttribute("flashMessages", machine.getMessages());
        return "redirect:/";
    }

    @PostMapping("/cancel")
    public String cancel(RedirectAttributes ra) {
        VendingMachine machine = getMachine();

        machine.clearMessages();
        machine.setSelectedBeverage(null);
        machine.changeState(new ReadyState());
        machine.addMessage("Đã hủy lựa chọn. Quay lại màn hình chính.");

        ra.addFlashAttribute("flashMessages", machine.getMessages());
        return "redirect:/";
    }

    private String handlePayOS(VendingMachine machine, RedirectAttributes ra) {
        Beverage selected = machine.getSelectedBeverage();
        Transaction tx = new Transaction(selected, PaymentMethod.PAYOS);

        try {
            String checkoutUrl = payOSService.createPaymentUrl(tx);
            transactionRepository.save(tx);

            machine.addMessage("[PayOS] Đang chuyển hướng sang PayOS...");
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            machine.addMessage("Lỗi tạo thanh toán PayOS: " + e.getMessage());
            machine.addMessage("Vui lòng thử lại hoặc chọn phương thức khác.");
            ra.addFlashAttribute("flashMessages", machine.getMessages());
            return "redirect:/";
        }
    }

    private void persistAfterCOD(VendingMachine machine) {
        var txns = machine.getTransactions();
        if (!txns.isEmpty()) {
            Transaction latest = txns.get(txns.size() - 1);
            if (latest.getId() == null) {
                transactionRepository.save(latest);

                inventoryItemRepository.findByBeverageId(latest.getBeverage().getId())
                        .ifPresent(item -> {
                            item.reduce();
                            inventoryItemRepository.save(item);
                        });
            }
        }
    }
}
