package com.l10s.testredis.reservations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.l10s.testredis.redis.RedisService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
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

    /**
     * Procesa una reserva que ha expirado en Redis
     * @param reservationId ID de la reserva expirada
     */
    public void processExpiredReservation(Long reservationId) {
        try {
            logger.info("Procesando reserva expirada: {}", reservationId);

            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();

                handleExpiredReservation(reservation);

            } else {
                logger.info("Reserva {} no encontrada en BD, ya fue procesada o eliminada", reservationId);
            }

        } catch (Exception e) {
            logger.error("Error procesando reserva expirada: {}", reservationId, e);
        }
    }

    /**
     * Verifica si una reserva debería expirar basándose en su expiresAt
     * @param reservation La reserva a verificar
     * @return true si debería expirar, false si no
     */
    private boolean shouldExpireReservation(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = reservation.getExpiresAt();

        // Si ya pasó el tiempo de expiración, es válido expirar
        return now.isAfter(expiresAt) || now.isEqual(expiresAt);
    }

    /**
     * Maneja la lógica de negocio para una reserva expirada
     * @param reservation La reserva expirada
     */
    private void handleExpiredReservation(Reservation reservation) {
        try {
            logger.info("Manejando reserva expirada: ID={}, Status={}, ExpiresAt={}",
                    reservation.getId(), reservation.getStatus(), reservation.getExpiresAt());

            // Cambiando el estado de la reserva
            reservation.setStatus("EXPIRED");
            reservationRepository.save(reservation);

            logger.info("Reserva {} marcada como EXPIRED", reservation.getId());

            // Eliminar completamente de la BD
            // reservationRepository.delete(reservation);
            // logger.info("Reserva {} eliminada completamente", reservation.getId());

        } catch (Exception e) {
            logger.error("Error manejando reserva expirada: {}", reservation.getId(), e);
        }
    }

}
