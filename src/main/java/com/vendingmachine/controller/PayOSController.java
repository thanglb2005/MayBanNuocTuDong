package com.vendingmachine.controller;

import com.vendingmachine.repository.InventoryItemRepository;
import com.vendingmachine.repository.Transaction;
import com.vendingmachine.repository.TransactionRepository;
import com.vendingmachine.service.PayOSService;
import com.vendingmachine.state.ReadyState;
import com.vendingmachine.state.VendingMachine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PayOSController {

    private final PayOSService payOSService;
    private final TransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public PayOSController(PayOSService payOSService,
                           TransactionRepository transactionRepository,
                           InventoryItemRepository inventoryItemRepository) {
        this.payOSService = payOSService;
        this.transactionRepository = transactionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @GetMapping("/payos/return")
    public String payosReturn(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String code,
                              @RequestParam(required = false) Long orderCode,
                              RedirectAttributes ra) {
        VendingMachine machine = VendingMachine.getInstance();

        if (orderCode == null) {
            machine.addMessage("Lỗi: Thiếu orderCode từ PayOS.");
            machine.setSelectedBeverage(null);
            machine.changeState(new ReadyState());
            ra.addFlashAttribute("flashMessages", machine.getMessages());
            return "redirect:/";
        }

        Transaction tx = transactionRepository.findByOrderCode(orderCode).orElse(null);

        if (tx != null && payOSService.isPaymentSuccess(status, code)) {
            machine.clearMessages();
            machine.addMessage("[PayOS] Thanh toán thành công qua PayOS!");
            machine.addMessage("Đang xuất sản phẩm: " + tx.getBeverage().getDescription());
            machine.addMessage("Sản phẩm đã ra! Cảm ơn bạn đã mua hàng.");

            machine.getInventory().reduceStock(tx.getBeverage().getId());
            inventoryItemRepository.findByBeverageId(tx.getBeverage().getId())
                    .ifPresent(item -> { item.reduce(); inventoryItemRepository.save(item); });

            machine.addTransaction(tx);

            int remaining = machine.getInventory().getStock(tx.getBeverage().getId());
            if (remaining <= 2 && remaining > 0) {
                machine.addMessage("Cảnh báo: \"" + tx.getBeverage().getName()
                        + "\" sắp hết hàng! (Còn " + remaining + ")");
            }

            machine.setSelectedBeverage(null);
            machine.changeState(new ReadyState());
        } else {
            machine.clearMessages();
            machine.addMessage("Thanh toán PayOS thất bại hoặc bị hủy.");
            machine.setSelectedBeverage(null);
            machine.changeState(new ReadyState());
        }

        ra.addFlashAttribute("flashMessages", machine.getMessages());
        return "redirect:/";
    }

    @GetMapping("/payos/cancel")
    public String payosCancel(@RequestParam(required = false) Long orderCode,
                              RedirectAttributes ra) {
        VendingMachine machine = VendingMachine.getInstance();

        if (orderCode != null) {
            // Không cập nhật status trong DB (Transaction không còn field status).
        }

        machine.clearMessages();
        machine.addMessage("Bạn đã hủy thanh toán PayOS. Quay lại màn hình chính.");
        machine.setSelectedBeverage(null);
        machine.changeState(new ReadyState());

        ra.addFlashAttribute("flashMessages", machine.getMessages());
        return "redirect:/";
    }
}
