package com.example.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MerchController {

    @GetMapping("/merch")
    public String showMerchPage() {
        return "merch";
    }

    @PostMapping("/merch/payment")
    public String submitMerchPayment(
            @RequestParam(defaultValue = "") String customerName,
            @RequestParam(defaultValue = "") String cartSummary,
            @RequestParam(defaultValue = "0.00") String orderTotal,
            @RequestParam(defaultValue = "Payment") String paymentMethod,
            RedirectAttributes redirectAttributes) {

        if (cartSummary.isBlank() || "0.00".equals(orderTotal)) {
            redirectAttributes.addFlashAttribute("paymentStatus", "error");
            redirectAttributes.addFlashAttribute("paymentMessage", "Please add at least one merch item before checkout.");
            return "redirect:/merch#payment";
        }

        String displayName = customerName.isBlank() ? "Customer" : customerName.trim();
        String methodLabel = paymentMethod.isBlank() ? "selected payment method" : paymentMethod;

        redirectAttributes.addFlashAttribute("paymentStatus", "success");
        redirectAttributes.addFlashAttribute(
                "paymentMessage",
                "Thanks, " + displayName + "! Your merch payment of $" + orderTotal + " is ready via " + methodLabel + ".");
        return "redirect:/merch#payment";
    }
}
