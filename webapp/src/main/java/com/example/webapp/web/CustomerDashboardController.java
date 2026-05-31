package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.MerchOrderDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.MerchOrder;
import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatStatus;
import com.example.webapp.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping({"/dashboard", "/customer/dashboard"})
public class CustomerDashboardController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm");

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;
    private final MerchOrderDao merchOrderDao;

    public CustomerDashboardController(ConcertLocationDao concertLocationDao, SeatDao seatDao, MerchOrderDao merchOrderDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
        this.merchOrderDao = merchOrderDao;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Object role = session.getAttribute("currentUserRole");
        if (role != UserRole.CUSTOMER) {
            return "redirect:/login?error=login_required";
        }

        Long userId = currentUserId(session);
        model.addAttribute("userFullName", session.getAttribute("currentUserFullName"));
        model.addAttribute("ownedTickets", userId == null ? List.of() : ownedTickets(userId));
        model.addAttribute("ownedMerch", userId == null ? List.of() : ownedMerch(userId));
        return "customer-dashboard";
    }

    private List<OwnedTicketView> ownedTickets(Long userId) {
        return seatDao.findByUserIdAndStatusOrderByIdAsc(userId, SeatStatus.BOOKED)
                .stream()
                .map(this::toTicketView)
                .toList();
    }

    private OwnedTicketView toTicketView(Seat seat) {
        ConcertLocation location = seat.getLocationId() == null
                ? null
                : concertLocationDao.findById(seat.getLocationId()).orElse(null);
        String city = location == null ? "Concert" : location.getCity();
        String venue = location == null ? "Venue TBA" : location.getVenueName();
        String date = location == null || location.getConcertDate() == null
                ? "Date TBA"
                : location.getConcertDate().format(DATE_FORMATTER);
        String seatLabel = ((seat.getRowLabel() == null || seat.getRowLabel().isBlank()) ? "Seat" : seat.getRowLabel())
                + " "
                + ((seat.getSeatNumber() == null || seat.getSeatNumber().isBlank()) ? seat.getId() : seat.getSeatNumber());
        return new OwnedTicketView(city, venue, date, seatLabel);
    }

    private List<OwnedMerchView> ownedMerch(Long userId) {
        return merchOrderDao.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMerchView)
                .toList();
    }

    private OwnedMerchView toMerchView(MerchOrder order) {
        String total = order.getTotalAmount() == null
                ? "Total TBA"
                : NumberFormat.getCurrencyInstance(Locale.US).format(order.getTotalAmount());
        return new OwnedMerchView(
                order.getCartSummary() == null || order.getCartSummary().isBlank() ? "Merch order" : order.getCartSummary(),
                total,
                order.getStatus() == null ? "Pending" : order.getStatus()
        );
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

    public record OwnedTicketView(String city, String venueName, String concertDate, String seatLabel) {
    }

    public record OwnedMerchView(String summary, String total, String status) {
    }
}