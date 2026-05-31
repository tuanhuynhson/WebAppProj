package com.example.webapp.web;

import com.example.webapp.dao.ConcertLocationDao;
import com.example.webapp.dao.SeatDao;
import com.example.webapp.dao.SeatSectionDao;
import com.example.webapp.model.ConcertLocation;
import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatSection;
import com.example.webapp.model.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketControllerTests {

    private ConcertLocationDao concertLocationDao;
    private SeatDao seatDao;
    private SeatSectionDao seatSectionDao;
    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        concertLocationDao = mock(ConcertLocationDao.class);
        seatDao = mock(SeatDao.class);
        seatSectionDao = mock(SeatSectionDao.class);
        ticketController = new TicketController(concertLocationDao, seatDao, seatSectionDao);
    }

    @Test
    void ticketPageReadsSectionLayoutFromDatabase() {
        ConcertLocation location = location(10L);
        SeatSection section = section(44L, "B2", 2, 2, 2, 1);
        when(concertLocationDao.findAllByOrderByConcertDateAscIdAsc()).thenReturn(List.of(location));
        when(seatSectionDao.findByLocationIdOrderByDisplayOrderAscIdAsc(10L)).thenReturn(List.of(section));
        when(seatDao.countByLocationIdAndStatus(10L, SeatStatus.AVAILABLE)).thenReturn(72L);
        when(seatDao.countBySectionIdAndStatus(44L, SeatStatus.AVAILABLE)).thenReturn(18L);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = ticketController.tickets(null, model);

        assertThat(view).isEqualTo("tickets");
        assertThat(model.get("availableSeats")).isEqualTo(72L);
        assertThat(model.get("sections"))
                .asList()
                .hasSize(1);

        TicketController.TicketSectionView selectedSection =
                (TicketController.TicketSectionView) model.get("selectedSection");
        assertThat(selectedSection.code()).isEqualTo("B2");
        assertThat(selectedSection.ticketType()).isEqualTo("STANDING");
        assertThat(selectedSection.price()).isEqualTo("$180.00");
        assertThat(selectedSection.rowSpan()).isEqualTo(2);
        assertThat(selectedSection.columnSpan()).isEqualTo(1);
        assertThat(selectedSection.availabilityLabel()).isEqualTo("18 available / 220 capacity");
    }

    @Test
    void ticketPaymentReadsSelectionPricesFromDatabase() {
        ConcertLocation location = location(10L);
        SeatSection section = section(44L, "B2", 2, 2, 2, 1);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUserId", 7L);
        ExtendedModelMap model = new ExtendedModelMap();

        when(concertLocationDao.findById(10L)).thenReturn(Optional.of(location));
        when(seatSectionDao.findAllById(any())).thenReturn(List.of(section));

        String view = ticketController.ticketPayment(
                10L,
                "44:2",
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("ticket-payment");
        TicketController.TicketPaymentView payment = (TicketController.TicketPaymentView) model.get("payment");
        assertThat(payment.totalQuantity()).isEqualTo(2);
        assertThat(payment.total()).isEqualTo("$360.00");
        assertThat(payment.items()).hasSize(1);
    }

    @Test
    void ticketCheckoutBooksAvailableSeatsForCurrentUser() {
        ConcertLocation location = location(10L);
        SeatSection section = section(44L, "B2", 2, 2, 2, 1);
        Seat firstSeat = seat(1L);
        Seat secondSeat = seat(2L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUserId", 7L);

        when(concertLocationDao.findById(10L)).thenReturn(Optional.of(location));
        when(seatSectionDao.findAllById(any())).thenReturn(List.of(section));
        when(seatDao.findBySectionIdAndStatusOrderByIdAsc(eq(44L), eq(SeatStatus.AVAILABLE), any(Pageable.class)))
                .thenReturn(List.of(firstSeat, secondSeat));

        String view = ticketController.completeTicketPayment(
                10L,
                "44:2",
                "Credit Card",
                session,
                new ExtendedModelMap(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/dashboard");
        assertThat(firstSeat.getUserId()).isEqualTo(7L);
        assertThat(firstSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(secondSeat.getUserId()).isEqualTo(7L);
        assertThat(secondSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        verify(seatDao).saveAll(List.of(firstSeat, secondSeat));
    }

    private ConcertLocation location(Long id) {
        ConcertLocation location = new ConcertLocation();
        ReflectionTestUtils.setField(location, "id", id);
        ReflectionTestUtils.setField(location, "city", "Canada");
        ReflectionTestUtils.setField(location, "venueName", "Canada Grand Stadium");
        return location;
    }

    private SeatSection section(Long id, String code, int gridRow, int gridColumn, int rowSpan, int columnSpan) {
        SeatSection section = new SeatSection();
        ReflectionTestUtils.setField(section, "id", id);
        ReflectionTestUtils.setField(section, "locationId", 10L);
        ReflectionTestUtils.setField(section, "sectionCode", code);
        ReflectionTestUtils.setField(section, "ticketType", "STANDING");
        ReflectionTestUtils.setField(section, "price", new BigDecimal("180.00"));
        ReflectionTestUtils.setField(section, "seatLocation", "Center Floor");
        ReflectionTestUtils.setField(section, "gridRow", gridRow);
        ReflectionTestUtils.setField(section, "gridColumn", gridColumn);
        ReflectionTestUtils.setField(section, "rowSpan", rowSpan);
        ReflectionTestUtils.setField(section, "columnSpan", columnSpan);
        ReflectionTestUtils.setField(section, "displayOrder", 5);
        ReflectionTestUtils.setField(section, "capacity", 220);
        return section;
    }

    private Seat seat(Long id) {
        Seat seat = new Seat();
        ReflectionTestUtils.setField(seat, "id", id);
        seat.setSectionId(44L);
        seat.setStatus(SeatStatus.AVAILABLE);
        return seat;
    }
}
