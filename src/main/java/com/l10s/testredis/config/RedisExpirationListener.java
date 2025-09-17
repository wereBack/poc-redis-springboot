package com.l10s.testredis.config;

import com.l10s.testredis.reservations.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisExpirationListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisExpirationListener.class);
    
    @Autowired
    private ReservationService reservationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // El mensaje contiene la clave que expiró
            String expiredKey = new String(message.getBody());
            String patternStr = new String(pattern);
            
            logger.info("Evento Redis recibido - Patrón: {}, Clave: {}", patternStr, expiredKey);
            
            // Verificar si es una clave de reserva que expiró
            if (expiredKey.startsWith("reservation:")) {
                String reservationId = expiredKey.substring("reservation:".length());
                handleReservationExpiration(reservationId);
            }
            
        } catch (Exception e) {
            logger.error("Error procesando evento de expiración de Redis", e);
        }
    }
    
    private void handleReservationExpiration(String reservationId) {
        try {
            logger.info("Procesando expiración de reserva: {}", reservationId);
            
            // Convertir ID a Long
            Long id = Long.parseLong(reservationId);
            
            // Delegar el procesamiento al servicio especializado
            reservationService.processExpiredReservation(id);
            
        } catch (NumberFormatException e) {
            logger.error("Error parseando ID de reserva: {}", reservationId, e);
        } catch (Exception e) {
            logger.error("Error manejando expiración de reserva: {}", reservationId, e);
        }
    }
}
