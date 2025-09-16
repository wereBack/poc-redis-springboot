package com.l10s.testredis.repositories;

import com.l10s.testredis.models.Reservation;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{
}
