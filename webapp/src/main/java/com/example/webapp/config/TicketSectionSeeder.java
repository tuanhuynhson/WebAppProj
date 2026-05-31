package com.example.webapp.config;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.dao.SeatSectionDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatSection;
import com.example.webapp.model.SeatStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TicketSectionSeeder implements CommandLineRunner {

    private final ConcertLocationDao concertLocationDao;
    private final SeatSectionDao seatSectionDao;
    private final SeatDao seatDao;

    public TicketSectionSeeder(ConcertLocationDao concertLocationDao, SeatSectionDao seatSectionDao, SeatDao seatDao) {
        this.concertLocationDao = concertLocationDao;
        this.seatSectionDao = seatSectionDao;
        this.seatDao = seatDao;
    }

    @Override
    public void run(String... args) {
        List<ConcertLocation> locations = concertLocationDao.findAllByOrderByConcertDateAscIdAsc();
        for (int i = 0; i < locations.size(); i++) {
            ConcertLocation location = locations.get(i);
            List<SeatSection> sections = syncLayout(location.getId(), layoutFor(location.getId(), i));
            seedSeats(sections);
        }
    }

    private List<SeatSection> syncLayout(Long locationId, List<SeatSection> desiredLayout) {
        Map<String, SeatSection> existingByCode = seatSectionDao.findByLocationIdOrderByDisplayOrderAscIdAsc(locationId)
                .stream()
                .collect(Collectors.toMap(
                        SeatSection::getSectionCode,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        List<SeatSection> sectionsToSave = new ArrayList<>();
        for (SeatSection desired : desiredLayout) {
            SeatSection section = existingByCode.getOrDefault(desired.getSectionCode(), new SeatSection());
            copyLayout(desired, section);
            sectionsToSave.add(section);
        }

        return seatSectionDao.saveAll(sectionsToSave);
    }

    private void seedSeats(List<SeatSection> sections) {
        for (SeatSection section : sections) {
            int capacity = section.getCapacity() == null ? 0 : section.getCapacity();
            long existingSeatCount = seatDao.countBySectionId(section.getId());
            int missingSeatCount = (int) Math.max(0, capacity - existingSeatCount);
            if (missingSeatCount == 0) {
                continue;
            }

            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= missingSeatCount; i++) {
                Seat seat = new Seat();
                int seatNumber = (int) existingSeatCount + i;
                seat.setLocationId(section.getLocationId());
                seat.setSectionId(section.getId());
                seat.setRowLabel(section.getSectionCode());
                seat.setSeatNumber(String.valueOf(seatNumber));
                seat.setStatus(SeatStatus.AVAILABLE);
                seats.add(seat);
            }
            seatDao.saveAll(seats);
        }
    }

    private void copyLayout(SeatSection source, SeatSection target) {
        target.setLocationId(source.getLocationId());
        target.setSectionCode(source.getSectionCode());
        target.setSectionName(source.getSectionName());
        target.setTicketType(source.getTicketType());
        target.setPrice(source.getPrice());
        target.setSeatLocation(source.getSeatLocation());
        target.setGridRow(source.getGridRow());
        target.setGridColumn(source.getGridColumn());
        target.setRowSpan(source.getRowSpan());
        target.setColumnSpan(source.getColumnSpan());
        target.setDisplayOrder(source.getDisplayOrder());
        target.setCapacity(source.getCapacity());
    }

    private List<SeatSection> layoutFor(Long locationId, int index) {
        return switch (Math.floorMod(index, 5)) {
            case 1 -> frontSweepLayout(locationId);
            case 2 -> centerFloorLayout(locationId);
            case 3 -> sidePitLayout(locationId);
            case 4 -> splitBalconyLayout(locationId);
            default -> verticalStandingLayout(locationId);
        };
    }

    private List<SeatSection> verticalStandingLayout(Long locationId) {
        List<SeatSection> sections = new ArrayList<>();
        sections.add(section(locationId, "A1", "V.I.P", "220.00", "Front Left", 1, 1, 1, 1, 1, 80));
        sections.add(section(locationId, "A2", "STANDING", "180.00", "A2 + B2 Center Standing", 1, 2, 2, 1, 2, 320));
        sections.add(section(locationId, "A3", "V.I.P", "220.00", "Front Right", 1, 3, 1, 1, 3, 80));
        sections.add(section(locationId, "B1", "PLUS", "150.00", "Middle Left", 2, 1, 1, 1, 4, 120));
        sections.add(section(locationId, "B2", "PLUS", "150.00", "Raised Center", 4, 2, 1, 1, 5, 120));
        sections.add(section(locationId, "B3", "PLUS", "150.00", "Middle Right", 2, 3, 1, 1, 6, 120));
        sections.add(section(locationId, "C1", "ECO", "90.00", "Rear Left", 3, 1, 1, 1, 7, 160));
        sections.add(section(locationId, "C2", "ECO", "110.00", "Rear Center", 3, 2, 1, 1, 8, 180));
        sections.add(section(locationId, "C3", "ECO", "90.00", "Rear Right", 3, 3, 1, 1, 9, 160));
        return sections;
    }

    private List<SeatSection> frontSweepLayout(Long locationId) {
        List<SeatSection> sections = new ArrayList<>();
        sections.add(section(locationId, "A1", "V.I.P", "260.00", "Front Sweep Left", 1, 1, 1, 2, 1, 140));
        sections.add(section(locationId, "A2", "V.I.P", "220.00", "Front Right", 1, 3, 1, 1, 2, 80));
        sections.add(section(locationId, "A3", "PLUS", "155.00", "Side Box", 4, 1, 1, 1, 3, 70));
        sections.add(section(locationId, "B1", "PLUS", "150.00", "Middle Left", 2, 1, 1, 1, 4, 120));
        sections.add(section(locationId, "B2", "STANDING", "180.00", "Center Floor", 2, 2, 1, 1, 5, 220));
        sections.add(section(locationId, "B3", "PLUS", "150.00", "Middle Right", 2, 3, 1, 1, 6, 120));
        sections.add(section(locationId, "C1", "ECO", "90.00", "Rear Left", 3, 1, 1, 1, 7, 160));
        sections.add(section(locationId, "C2", "ECO", "120.00", "Wide Rear Center", 3, 2, 1, 2, 8, 240));
        sections.add(section(locationId, "C3", "ECO", "90.00", "Rear Corner", 4, 3, 1, 1, 9, 100));
        return sections;
    }

    private List<SeatSection> centerFloorLayout(Long locationId) {
        List<SeatSection> sections = new ArrayList<>();
        sections.add(section(locationId, "A1", "V.I.P", "240.00", "Front Left", 1, 1, 1, 1, 1, 80));
        sections.add(section(locationId, "A2", "V.I.P", "260.00", "Front Center", 1, 2, 1, 1, 2, 100));
        sections.add(section(locationId, "A3", "V.I.P", "240.00", "Front Right", 1, 3, 1, 1, 3, 80));
        sections.add(section(locationId, "B1", "PLUS", "145.00", "Middle Left", 3, 1, 1, 1, 4, 110));
        sections.add(section(locationId, "B2", "STANDING", "180.00", "Full Width Standing Floor", 2, 1, 1, 3, 5, 420));
        sections.add(section(locationId, "B3", "PLUS", "145.00", "Middle Right", 3, 3, 1, 1, 6, 110));
        sections.add(section(locationId, "C1", "ECO", "95.00", "Rear Left", 4, 1, 1, 1, 7, 150));
        sections.add(section(locationId, "C2", "ECO", "115.00", "Rear Center", 4, 2, 1, 1, 8, 170));
        sections.add(section(locationId, "C3", "ECO", "95.00", "Rear Right", 4, 3, 1, 1, 9, 150));
        return sections;
    }

    private List<SeatSection> sidePitLayout(Long locationId) {
        List<SeatSection> sections = new ArrayList<>();
        sections.add(section(locationId, "A1", "STANDING", "170.00", "Left Floor Pit", 1, 1, 3, 1, 1, 360));
        sections.add(section(locationId, "A2", "V.I.P", "250.00", "Front Center", 1, 2, 1, 1, 2, 100));
        sections.add(section(locationId, "A3", "V.I.P", "220.00", "Front Right", 1, 3, 1, 1, 3, 80));
        sections.add(section(locationId, "B1", "PLUS", "150.00", "Middle Center", 2, 2, 1, 1, 4, 120));
        sections.add(section(locationId, "B2", "PLUS", "145.00", "Middle Right", 2, 3, 1, 1, 5, 120));
        sections.add(section(locationId, "B3", "PLUS", "130.00", "Lower Middle", 3, 2, 1, 1, 6, 110));
        sections.add(section(locationId, "C1", "ECO", "100.00", "Lower Right", 3, 3, 1, 1, 7, 120));
        sections.add(section(locationId, "C2", "ECO", "105.00", "Rear Wide Left", 4, 1, 1, 2, 8, 220));
        sections.add(section(locationId, "C3", "ECO", "90.00", "Rear Right", 4, 3, 1, 1, 9, 130));
        return sections;
    }

    private List<SeatSection> splitBalconyLayout(Long locationId) {
        List<SeatSection> sections = new ArrayList<>();
        sections.add(section(locationId, "A1", "V.I.P", "230.00", "Front Left", 1, 1, 1, 1, 1, 80));
        sections.add(section(locationId, "A2", "V.I.P", "260.00", "Front Center", 1, 2, 1, 1, 2, 100));
        sections.add(section(locationId, "A3", "V.I.P", "230.00", "Front Right", 1, 3, 1, 1, 3, 80));
        sections.add(section(locationId, "B1", "PLUS", "150.00", "Left Orchestra", 2, 1, 2, 1, 4, 180));
        sections.add(section(locationId, "B2", "STANDING", "175.00", "Wide Center Floor", 2, 2, 1, 2, 5, 280));
        sections.add(section(locationId, "B3", "PLUS", "140.00", "Lower Left Box", 4, 1, 1, 1, 6, 80));
        sections.add(section(locationId, "C1", "ECO", "105.00", "Rear Center Left", 3, 2, 1, 1, 7, 140));
        sections.add(section(locationId, "C2", "ECO", "105.00", "Rear Center Right", 3, 3, 1, 1, 8, 140));
        sections.add(section(locationId, "C3", "ECO", "95.00", "Balcony Wide", 4, 2, 1, 2, 9, 220));
        return sections;
    }

    private SeatSection section(
            Long locationId,
            String code,
            String ticketType,
            String price,
            String seatLocation,
            int gridRow,
            int gridColumn,
            int rowSpan,
            int columnSpan,
            int displayOrder,
            int capacity
    ) {
        SeatSection section = new SeatSection();
        section.setLocationId(locationId);
        section.setSectionCode(code);
        section.setSectionName(code + " " + ticketType);
        section.setTicketType(ticketType);
        section.setPrice(new BigDecimal(price));
        section.setSeatLocation(seatLocation);
        section.setGridRow(gridRow);
        section.setGridColumn(gridColumn);
        section.setRowSpan(rowSpan);
        section.setColumnSpan(columnSpan);
        section.setDisplayOrder(displayOrder);
        section.setCapacity(capacity);
        return section;
    }
}
