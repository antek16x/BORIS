package org.boris.query.repository;

import org.boris.query.entity.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiclesRepository extends JpaRepository<Vehicles, Long> {

    boolean existsByVehicleReg(String vehicleReg);
    Optional<Vehicles> findByVehicleReg(String vehicleReg);
}
