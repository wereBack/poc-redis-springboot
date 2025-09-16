package com.l10s.testredis.services;

import com.l10s.testredis.models.Reservation;
import com.l10s.testredis.repositories.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RedisService redisService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, RedisService redisService) {
        this.reservationRepository = reservationRepository;
        this.redisService = redisService;
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation save() {
        Reservation reservation = new Reservation();
        reservationRepository.save(reservation);
        
        redisService.saveReservationWithTTL(
            reservation.getId().toString(), 
            reservation, 
            Duration.ofSeconds(30)
        );
        
        return reservation;
    }

    public boolean isReservationActive(Long id) {
        return redisService.existsReservation(id.toString());
    }

    public long getReservationTTL(Long id) {
        return redisService.getReservationTTL(id.toString());
    }
}
