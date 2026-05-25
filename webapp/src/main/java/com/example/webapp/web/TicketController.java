package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.model.ConcertLocation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class TicketController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm");

    private final ConcertLocationDao concertLocationDao;

    public TicketController(ConcertLocationDao concertLocationDao) {
        this.concertLocationDao = concertLocationDao;
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(required = false) Long locationId, Model model) {
        List<ConcertLocation> locations = concertLocationDao.findAllByOrderByConcertDateAscIdAsc();
        model.addAttribute("locations", locations);
        model.addAttribute("selectedId", locationId);
        model.addAttribute("formatDate", DATE_FORMATTER);
        return "tickets";
    }
}
