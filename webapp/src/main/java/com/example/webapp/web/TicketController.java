package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.dao.SeatSectionDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatSection;
import com.example.webapp.model.SeatStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Controller
public class TicketController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm");

    private final ConcertLocationDao concertLocationDao;
    private final SeatDao seatDao;
    private final SeatSectionDao seatSectionDao;

    public TicketController(ConcertLocationDao concertLocationDao, SeatDao seatDao, SeatSectionDao seatSectionDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatDao = seatDao;
        this.seatSectionDao = seatSectionDao;
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(required = false) Long locationId, Model model) {
        ConcertLocation location = resolveLocation(locationId);
        List<TicketSectionView> sections = location == null
                ? List.of()
                : seatSectionDao.findByLocationIdOrderByDisplayOrderAscIdAsc(location.getId())
                        .stream()
                        .map(this::toSectionView)
                        .toList();

        model.addAttribute("location", location == null ? null : toLocationView(location));
        model.addAttribute("availableSeats", location == null
                ? 0
                : seatDao.countByLocationIdAndStatus(location.getId(), SeatStatus.AVAILABLE));
        model.addAttribute("sections", sections);
        model.addAttribute("selectedSection", selectDefaultSection(sections));
        return "tickets";
    }

    @PostMapping("/tickets/payment")
    public String ticketPayment(
            @RequestParam Long locationId,
            @RequestParam(defaultValue = "") String selections,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUserId(session) == null) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in before buying tickets.");
            return "redirect:/login?error=login_required";
        }

        ConcertLocation location = concertLocationDao.findById(locationId).orElse(null);
        TicketPaymentView payment = buildPayment(location, selections);
        if (location == null || payment.items().isEmpty()) {
            redirectAttributes.addFlashAttribute("ticketMessage", "Please choose at least one ticket section.");
            return "redirect:/tickets?locationId=" + locationId;
        }

        model.addAttribute("location", toLocationView(location));
        model.addAttribute("payment", payment);
        model.addAttribute("selections", selections);
        return "ticket-payment";
    }

    @PostMapping("/tickets/checkout")
    public String completeTicketPayment(
            @RequestParam Long locationId,
            @RequestParam(defaultValue = "") String selections,
            @RequestParam(defaultValue = "Credit Card") String paymentMethod,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = currentUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in before buying tickets.");
            return "redirect:/login?error=login_required";
        }

        ConcertLocation location = concertLocationDao.findById(locationId).orElse(null);
        TicketPaymentView payment = buildPayment(location, selections);
        if (location == null || payment.items().isEmpty()) {
            redirectAttributes.addFlashAttribute("ticketMessage", "Please choose at least one ticket section.");
            return "redirect:/tickets?locationId=" + locationId;
        }

        for (TicketPaymentItem item : payment.items()) {
            List<Seat> seats = seatDao.findBySectionIdAndStatusOrderByIdAsc(
                    item.sectionId(),
                    SeatStatus.AVAILABLE,
                    PageRequest.of(0, item.quantity())
            );
            if (seats.size() < item.quantity()) {
                model.addAttribute("location", toLocationView(location));
                model.addAttribute("payment", payment);
                model.addAttribute("selections", selections);
                model.addAttribute("paymentStatus", "error");
                model.addAttribute("paymentMessage", item.code() + " does not have enough seats available anymore.");
                return "ticket-payment";
            }

            seats.forEach(seat -> {
                seat.setUserId(userId);
                seat.setStatus(SeatStatus.BOOKED);
            });
            seatDao.saveAll(seats);
        }

        redirectAttributes.addFlashAttribute(
                "ticketMessage",
                "Ticket payment completed via " + paymentMethod + "."
        );
        return "redirect:/dashboard";
    }

    private ConcertLocation resolveLocation(Long locationId) {
        if (locationId != null) {
            return concertLocationDao.findById(locationId).orElse(null);
        }

        return concertLocationDao.findAllByOrderByConcertDateAscIdAsc()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private TicketLocationView toLocationView(ConcertLocation location) {
        String formattedDate = location.getConcertDate() == null
                ? "Date to be announced"
                : location.getConcertDate().format(DATE_FORMATTER);

        return new TicketLocationView(
                location.getId(),
                location.getCity(),
                location.getVenueName(),
                location.getCountry(),
                location.getAddress(),
                formattedDate
        );
    }

    private TicketSectionView toSectionView(SeatSection section) {
        long availableSeats = seatDao.countBySectionIdAndStatus(section.getId(), SeatStatus.AVAILABLE);
        String availabilityLabel = availableSeats + " available";
        if (section.getCapacity() != null && section.getCapacity() > 0) {
            availabilityLabel += " / " + section.getCapacity() + " capacity";
        }

        return new TicketSectionView(
                section.getId(),
                section.getSectionCode(),
                blankFallback(section.getTicketType(), "SECTION"),
                formatPrice(section.getPrice()),
                section.getPrice() == null ? "0.00" : section.getPrice().toPlainString(),
                blankFallback(section.getSeatLocation(), "Location TBA"),
                availabilityLabel,
                sectionModifier(section.getTicketType()),
                valueOrDefault(section.getGridRow(), 1),
                valueOrDefault(section.getGridColumn(), 1),
                valueOrDefault(section.getRowSpan(), 1),
                valueOrDefault(section.getColumnSpan(), 1),
                availableSeats
        );
    }

    private TicketSectionView selectDefaultSection(List<TicketSectionView> sections) {
        return sections.stream()
                .filter(section -> "B2".equalsIgnoreCase(section.code()))
                .findFirst()
                .orElse(sections.isEmpty() ? null : sections.get(0));
    }

    private TicketPaymentView buildPayment(ConcertLocation location, String selections) {
        if (location == null) {
            return new TicketPaymentView(List.of(), "$0.00", 0);
        }

        Map<Long, Integer> quantities = parseSelections(selections);
        if (quantities.isEmpty()) {
            return new TicketPaymentView(List.of(), "$0.00", 0);
        }

        Map<Long, SeatSection> sectionsById = new LinkedHashMap<>();
        seatSectionDao.findAllById(quantities.keySet())
                .forEach(section -> {
                    if (Objects.equals(section.getLocationId(), location.getId())) {
                        sectionsById.put(section.getId(), section);
                    }
                });

        List<TicketPaymentItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            SeatSection section = sectionsById.get(entry.getKey());
            if (section == null || entry.getValue() < 1) {
                continue;
            }

            BigDecimal unitPrice = section.getPrice() == null ? BigDecimal.ZERO : section.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(lineTotal);
            totalQuantity += entry.getValue();
            items.add(new TicketPaymentItem(
                    section.getId(),
                    section.getSectionCode(),
                    blankFallback(section.getTicketType(), "SECTION"),
                    blankFallback(section.getSeatLocation(), "Location TBA"),
                    entry.getValue(),
                    formatPrice(unitPrice),
                    formatPrice(lineTotal)
            ));
        }

        return new TicketPaymentView(items, formatPrice(total), totalQuantity);
    }

    private Map<Long, Integer> parseSelections(String selections) {
        Map<Long, Integer> parsed = new LinkedHashMap<>();
        if (selections == null || selections.isBlank()) {
            return parsed;
        }

        for (String pair : selections.split(",")) {
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                continue;
            }

            try {
                Long sectionId = Long.valueOf(parts[0]);
                int quantity = Integer.parseInt(parts[1]);
                if (quantity > 0) {
                    parsed.merge(sectionId, quantity, Integer::sum);
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed browser input; totals are rebuilt from database-backed sections.
            }
        }

        return parsed;
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? "Price TBA" : NumberFormat.getCurrencyInstance(Locale.US).format(price);
    }

    private String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value == null || value < 1 ? fallback : value;
    }

    private String sectionModifier(String ticketType) {
        if (ticketType == null || ticketType.isBlank()) {
            return "section";
        }

        return ticketType.toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
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

    public record TicketLocationView(
            Long id,
            String city,
            String venueName,
            String country,
            String address,
            String concertDate
    ) {
    }

    public record TicketSectionView(
            Long id,
            String code,
            String ticketType,
            String price,
            String priceAmount,
            String seatLocation,
            String availabilityLabel,
            String modifier,
            int gridRow,
            int gridColumn,
            int rowSpan,
            int columnSpan,
            long availableSeats
    ) {
    }

    public record TicketPaymentView(
            List<TicketPaymentItem> items,
            String total,
            int totalQuantity
    ) {
    }

    public record TicketPaymentItem(
            Long sectionId,
            String code,
            String ticketType,
            String seatLocation,
            int quantity,
            String unitPrice,
            String lineTotal
    ) {
    }
}
