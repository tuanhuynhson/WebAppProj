package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.model.SeatStatus;
import com.example.webapp.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;

    public AdminController(ConcertLocationDao concertLocationDao, SeatDao seatDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
    }

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        Object role = session.getAttribute("currentUserRole");
        if (role != UserRole.ADMIN) {
            return "redirect:/login?error=login_required";
        }

        model.addAttribute("userFullName", session.getAttribute("currentUserFullName"));
        model.addAttribute("concertCount", concertLocationDao.count());
        model.addAttribute("availableSeatCount", seatDao.countByStatus(SeatStatus.AVAILABLE));
        return "admin";
    }
}
