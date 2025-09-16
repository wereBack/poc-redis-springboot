package com.l10s.testredis.controllers;

import com.l10s.testredis.models.Reservation;
import com.l10s.testredis.services.ReservationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService)
    {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id){
        Optional<Reservation> reservation = reservationService.findById(id);
        return reservation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation() {
        Reservation reservation = reservationService.save();
        return ResponseEntity
                .created(URI.create("api/reservations/" + reservation.getId())) // header Location
                .body(reservation); // el body con la reserva creada
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getReservationStatus(@PathVariable Long id) {
        Map<String, Object> status = new HashMap<>();
        
        // Verificar si existe en BD
        Optional<Reservation> reservation = reservationService.findById(id);
        status.put("existsInDatabase", reservation.isPresent());
        
        // Verificar si existe en Redis (activa)
        boolean isActive = reservationService.isReservationActive(id);
        status.put("isActiveInRedis", isActive);
        
        // Obtener TTL restante
        long ttl = reservationService.getReservationTTL(id);
        status.put("ttlSeconds", ttl);

        reservation.ifPresent(value -> status.put("reservation", value));
        
        return ResponseEntity.ok(status);
    }
}
