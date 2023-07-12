package org.boris.query.repository;

import org.boris.query.entity.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiclesRepository extends JpaRepository<Vehicles, Long> {

    boolean existsByVehicleReg(String vehicleReg);
}
