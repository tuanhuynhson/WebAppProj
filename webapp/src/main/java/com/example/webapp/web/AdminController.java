package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatStatus;
import com.example.webapp.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;

    public AdminController(
            ConcertLocationDao concertLocationDao,
            SeatDao seatDao
    ) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
    }

    private boolean isAdmin(HttpSession session) {
        Object role =
                session.getAttribute(
                        "currentUserRole"
                );

        return role == UserRole.ADMIN;
    }

    @GetMapping
    public String adminDashboard(
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "userFullName",
                session.getAttribute(
                        "currentUserFullName"
                )
        );

        model.addAttribute(
                "concertCount",
                concertLocationDao.count()
        );

        model.addAttribute(
                "availableSeatCount",
                seatDao.countByStatus(
                        SeatStatus.AVAILABLE
                )
        );

        model.addAttribute(
                "bookedSeatCount",
                seatDao.countByStatus(
                        SeatStatus.BOOKED
                )
        );

        model.addAttribute(
                "heldSeatCount",
                seatDao.countByStatus(
                        SeatStatus.HELD
                )
        );

        return "admin";
    }

    @GetMapping("/concerts")
    public String manageConcerts(
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "concerts",
                concertLocationDao
                        .findAllByOrderByConcertDateAscIdAsc()
        );

        return "manage-concerts";
    }

    @PostMapping("/concerts/add")
    public String addConcert(
            @RequestParam String city,
            @RequestParam String venueName,
            @RequestParam String country,
            @RequestParam String address,
            @RequestParam String description
    ) {

        ConcertLocation concert =
                new ConcertLocation();

        concert.setCity(city);
        concert.setVenueName(venueName);
        concert.setCountry(country);
        concert.setAddress(address);
        concert.setDescription(description);
        concert.setConcertDate(
                LocalDateTime.now()
                        .plusDays(30)
        );

        concertLocationDao.save(concert);

        return "redirect:/admin/concerts";
    }

    @PostMapping("/concerts/delete")
    public String deleteConcert(
            @RequestParam Long id
    ) {

        concertLocationDao.deleteById(id);

        return "redirect:/admin/concerts";
    }

    @GetMapping("/seats")
    public String manageSeats(
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "seats",
                seatDao.findAllByOrderByIdAsc()
        );

        model.addAttribute(
                "statuses",
                SeatStatus.values()
        );

        return "manage-seats";
    }

    @PostMapping("/seats/update")
    public String updateSeatStatus(
            @RequestParam Long id,
            @RequestParam SeatStatus status
    ) {

        Seat seat =
                seatDao.findById(id)
                        .orElseThrow();

        seat.setStatus(status);

        seatDao.save(seat);

        return "redirect:/admin/seats";
    }
}