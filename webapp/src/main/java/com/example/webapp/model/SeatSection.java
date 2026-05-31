package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_sections")
public class SeatSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "section_code")
    private String sectionCode;

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "ticket_type")
    private String ticketType;

    private BigDecimal price;

    @Column(name = "seat_location")
    private String seatLocation;

    @Column(name = "grid_row")
    private Integer gridRow;

    @Column(name = "grid_column")
    private Integer gridColumn;

    @Column(name = "row_span")
    private Integer rowSpan;

    @Column(name = "column_span")
    private Integer columnSpan;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Integer capacity;

    public Long getId() {
        return id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getTicketType() {
        return ticketType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSeatLocation() {
        return seatLocation;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public Integer getGridColumn() {
        return gridColumn;
    }

    public Integer getRowSpan() {
        return rowSpan;
    }

    public Integer getColumnSpan() {
        return columnSpan;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSeatLocation(String seatLocation) {
        this.seatLocation = seatLocation;
    }

    public void setGridRow(Integer gridRow) {
        this.gridRow = gridRow;
    }

    public void setGridColumn(Integer gridColumn) {
        this.gridColumn = gridColumn;
    }

    public void setRowSpan(Integer rowSpan) {
        this.rowSpan = rowSpan;
    }

    public void setColumnSpan(Integer columnSpan) {
        this.columnSpan = columnSpan;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
