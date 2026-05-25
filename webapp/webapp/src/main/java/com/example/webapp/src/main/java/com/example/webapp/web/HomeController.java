package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.SeatStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HomeController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm");

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;

    public HomeController(ConcertLocationDao concertLocationDao, SeatDao seatDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ConcertLocationView> locations = concertLocationDao.findAllByOrderByConcertDateAscIdAsc()
                .stream()
                .map(this::toView)
                .toList();

        model.addAttribute("locations", locations);
        return "index";
    }

    private ConcertLocationView toView(ConcertLocation location) {
        long availableSeats = seatDao.countByLocationIdAndStatus(location.getId(), SeatStatus.AVAILABLE);
        String formattedDate = location.getConcertDate() == null
                ? "Date to be announced"
                : location.getConcertDate().format(DATE_FORMATTER);

        return new ConcertLocationView(
                location.getId(),
                location.getCity(),
                location.getVenueName(),
                location.getCountry(),
                location.getAddress(),
                formattedDate,
                location.getDescription(),
                availableSeats
        );
    }

    public record ConcertLocationView(
            Long id,
            String city,
            String venueName,
            String country,
            String address,
            String concertDate,
            String description,
            long availableSeats
    ) {
    }
}
