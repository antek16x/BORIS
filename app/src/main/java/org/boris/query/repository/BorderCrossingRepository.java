package org.boris.query.repository;

import org.boris.query.entity.BorderCrossing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BorderCrossingRepository extends JpaRepository<BorderCrossing, Long> {
    List<BorderCrossing> findByVehicleRegAndTimestampBetween(String vehicleReg, Instant startingDate, Instant endDate);
}
