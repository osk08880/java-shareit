package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    @Transient
    private Booking lastBooking;

    @Transient
    private Booking nextBooking;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public void setLastBooking(List<Booking> bookings) {
        this.lastBooking = bookings.stream()
                .filter(b -> b.getItem().getId().equals(this.id) && b.getStatus().equals(BookingStatus.APPROVED))
                .max((b1, b2) -> b2.getEnd().compareTo(b1.getEnd()))
                .orElse(null);
    }

    public void setNextBooking(List<Booking> bookings) {
        this.nextBooking = bookings.stream()
                .filter(b -> b.getItem().getId().equals(this.id) && b.getStatus().equals(BookingStatus.APPROVED))
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                .orElse(null);
    }
}
