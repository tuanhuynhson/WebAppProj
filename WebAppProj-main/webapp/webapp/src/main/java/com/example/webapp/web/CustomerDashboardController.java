package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.SeatStatus;
import com.example.webapp.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class CustomerDashboardController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm");

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;

    public CustomerDashboardController(ConcertLocationDao concertLocationDao, SeatDao seatDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Object role = session.getAttribute("currentUserRole");
        if (role != UserRole.CUSTOMER) {
            return "redirect:/login?error=login_required";
        }

        model.addAttribute("userFullName", session.getAttribute("currentUserFullName"));

        List<ConcertLocationSummary> upcoming = concertLocationDao.findAllByOrderByConcertDateAscIdAsc()
                .stream()
                .map(this::toSummary)
                .toList();

        model.addAttribute("upcomingLocations", upcoming);
        model.addAttribute("availableSeatCount", seatDao.countByStatus(SeatStatus.AVAILABLE));
        return "customer-dashboard";
    }

    private ConcertLocationSummary toSummary(ConcertLocation location) {
        String concertDate = location.getConcertDate() == null
                ? "Date to be announced"
                : location.getConcertDate().format(DATE_FORMATTER);

        return new ConcertLocationSummary(
                location.getCity(),
                location.getVenueName(),
                concertDate,
                seatDao.countByLocationIdAndStatus(location.getId(), SeatStatus.AVAILABLE)
        );
    }

    public record ConcertLocationSummary(
            String city,
            String venueName,
            String concertDate,
            long availableSeats
    ) {
    }
}
