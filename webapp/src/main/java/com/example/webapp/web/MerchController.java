package com.example.webapp.web;

import com.example.webapp.dao.MerchOrderDao;
import com.example.webapp.model.MerchOrder;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MerchController {

    private final MerchOrderDao merchOrderDao;

    public MerchController(MerchOrderDao merchOrderDao) {
        this.merchOrderDao = merchOrderDao;
    }

    @GetMapping("/merch")
    public String showMerchPage() {
        return "merch";
    }

    @PostMapping("/merch/payment")
    public String submitMerchPayment(
            @RequestParam(defaultValue = "") String customerName,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String deliveryAddress,
            @RequestParam(defaultValue = "") String cartSummary,
            @RequestParam(defaultValue = "0.00") String orderTotal,
            @RequestParam(defaultValue = "Payment") String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (cartSummary.isBlank() || "0.00".equals(orderTotal)) {
            redirectAttributes.addFlashAttribute("paymentStatus", "error");
            redirectAttributes.addFlashAttribute("paymentMessage", "Please add at least one merch item before checkout.");
            return "redirect:/merch#payment";
        }

        Long userId = currentUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("paymentStatus", "error");
            redirectAttributes.addFlashAttribute("paymentMessage", "Please log in before checking out merch.");
            return "redirect:/login?error=login_required";
        }

        double total = parseTotal(orderTotal);
        String displayName = customerName.isBlank() ? "Customer" : customerName.trim();
        String methodLabel = paymentMethod.isBlank() ? "selected payment method" : paymentMethod;

        MerchOrder order = new MerchOrder();
        order.setUserId(userId);
        order.setCustomerName(displayName);
        order.setEmail(email.isBlank() ? String.valueOf(session.getAttribute("currentUserEmail")) : email.trim());
        order.setAddress(deliveryAddress.trim());
        order.setCartSummary(cartSummary.trim());
        order.setTotalAmount(total);
        order.setPaymentMethod(methodLabel);
        order.setStatus("Paid");
        merchOrderDao.save(order);

        redirectAttributes.addFlashAttribute("paymentStatus", "success");
        redirectAttributes.addFlashAttribute(
                "paymentMessage",
                "Thanks, " + displayName + "! Your merch payment of $" + String.format("%.2f", total) + " is ready via " + methodLabel + ".");
        return "redirect:/merch#payment";
    }

    private Long currentUserId(HttpSession session) {
        Object userId = session.getAttribute("currentUserId");
        if (userId instanceof Long id) {
            return id;
        }
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private double parseTotal(String orderTotal) {
        try {
            return Double.parseDouble(orderTotal);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}